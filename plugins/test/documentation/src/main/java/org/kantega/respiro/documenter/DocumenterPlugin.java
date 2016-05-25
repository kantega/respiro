package org.kantega.respiro.documenter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import fj.data.List;
import fj.data.Option;
import fj.data.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.VetoCamelContextStartException;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.support.LifecycleStrategySupport;
import org.apache.commons.lang3.StringUtils;
import org.kantega.respiro.camel.CamelContextCustomizer;
import org.kantega.respiro.collector.CollectionListener;
import org.kantega.respiro.collector.Collector;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fj.data.List.*;
import static org.apache.commons.lang3.StringUtils.*;

@Plugin
public class DocumenterPlugin {

    final static Logger logger = LoggerFactory.getLogger(DocumenterPlugin.class);

    @Export
    final CamelContextCustomizer customizer;


    private final CollectionListener exchangesListener;

    private final ExchangeLog log =
      new ExchangeLog();

    private volatile List<RouteDocumentation> routeDocumentations =
      nil();

    private final ObjectMapper mapper;

    public DocumenterPlugin() {

        customizer = camelContext ->
          camelContext.addLifecycleStrategy(new LifecycleStrategySupport() {
              @Override
              public void onContextStart(CamelContext context) throws VetoCamelContextStartException {

                  routeDocumentations =
                    iterableList(context.getRouteDefinitions()).map(RouteDocumentation::fromRoute);
              }
          });

        exchangesListener = log::addExchange;
        Collector.addListener(exchangesListener);

        mapper = new ObjectMapper();
        SimpleModule fjModule = new SimpleModule();
        fjModule.addSerializer(List.class, new JsonSerializer<List>() {
            @Override
            public void serialize(
              List value,
              JsonGenerator gen,
              SerializerProvider serializers) throws IOException, JsonProcessingException {
                serializers.defaultSerializeValue(value.toJavaList(), gen);
            }
        });
        mapper.registerModule(fjModule);
    }


    public void logdocs() {
        try {
            logger.info("**** Routes: ****");
            logger.info(Strings.mkString(RouteDocumentation.loggerShow, "\n").showS(routeDocumentations));

            Path baseDirectory =
              getBasedir();

            Path sourceDir =
              baseDirectory.resolve("src/main/java");

            Path targetDirectory =
              baseDirectory.resolve("target/generated-documentation");

            Path targetPath =
              Files.createDirectories(targetDirectory).resolve("doc.json");


            Set<DependencyDocumentation> dependencies =
              log.asList()
                .bind(info -> iterableList(info.getBackendMessages()))
                .foldLeft(
                  (tree, info) -> tree.insert(new DependencyDocumentation(info.getProtocol(), substringBefore(info.getAddress(), "?"))),
                  Set.empty(DependencyDocumentation.ord)
                );

            logger.info("**** Dependencies ****");
            logger.info(Strings.mkString(DependencyDocumentation.loggerShow, "\n").showS(dependencies.toList()));

            List<ResourceDocumentation> resourceDocs =
              runDoc(sourceDir).map(rDoc -> {
                  List<MethodDocumentation> mDocs =
                    rDoc.methodDocs.map(mDoc -> {
                        List<ExchangeDocumentation> docs =
                          log.asList()
                            .filter(exinfo ->
                              substringBefore(exinfo.getInMessage().getAddress(), "?").endsWith(mDoc.path))
                            .map(exInfo -> {
                                RequestDocumentation requestDocumentation =
                                  new RequestDocumentation(exInfo.getInMessage().getHeaders(), exInfo.getInMessage().getPayload());

                                ResponseDocumentation responseDocumentation =
                                  new ResponseDocumentation(exInfo.getOutMessage().getHeaders(), exInfo.getOutMessage().getPayload(), exInfo.getOutMessage().getResponseCode());

                                return new ExchangeDocumentation(requestDocumentation, responseDocumentation);
                            });
                        return mDoc.withRecorded(docs);
                    });
                  return new ResourceDocumentation(rDoc.path, rDoc.rolesAllowed, rDoc.documentation, mDocs);
              });

            logger.info("**** Api docs ****");
            logger.info(Strings.mkString(ResourceDocumentation.loggerShow, "\n").showS(resourceDocs));

            Documentation doc = new Documentation(dependencies.toList(), routeDocumentations, resourceDocs);



            mapper.writeValue(targetPath.toFile(), doc);

        }
        catch (Throwable e) {
            logger.error("Fail!", e);
        }

    }

    @PreDestroy
    public void teardown() {
        Collector.removeListener(exchangesListener);
        logdocs();
    }

    private List<ResourceDocumentation> runDoc(Path sourceDir) throws URISyntaxException, IOException {
        return
          iterableList(Files.walk(sourceDir)
            .filter(path -> path.toString().contains(".java") && path.toString().contains("Resource"))
            .flatMap(path -> {
                try (FileInputStream in = new FileInputStream(path.toFile())) {
                    try {
                        CompilationUnit cu = JavaParser.parse(in);
                        Option<ResourceDocumentation> maybeDoc =
                          DocumentationExtractor.document(cu);
                        return maybeDoc.option(Stream.empty(), Stream::of);
                    }
                    catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList()));
    }

    public static Path getBasedir() {
        return Paths.get(DocumenterPlugin.class.getResource("/").getFile().replaceFirst("^/(.:/)", "$1"), new String[0]).getParent().getParent().getParent().getParent().getParent().getParent().getParent();
    }
}
