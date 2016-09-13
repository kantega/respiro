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
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import fj.data.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.VetoCamelContextStartException;
import org.apache.camel.support.LifecycleStrategySupport;
import org.kantega.respiro.camel.CamelContextCustomizer;
import org.kantega.respiro.collector.CollectionListener;
import org.kantega.respiro.collector.Collector;
import org.kantega.respiro.documenter.flow.Model;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fj.data.List.iterableList;
import static fj.data.List.nil;
import static fj.data.Option.some;
import static org.apache.commons.lang3.StringUtils.substringBefore;

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

            logger.debug("**** Routes: ****");
            logger.debug(Strings.mkString(RouteDocumentation.loggerShow, "\n").showS(routeDocumentations));

            Path baseDirectory =
              getBasedir();

            Path sourceDir =
              baseDirectory.resolve("src/main/java");

            Path targetDirectory =
              baseDirectory.resolve("target/generated-documentation");

            Path targetPath =
              Files.createDirectories(targetDirectory).resolve("doc.json");


            Set<DependencyDocumentation> dependencies =
              Option.somes(
                log.asList()
                  .bind(info -> iterableList(info.getBackendMessages()))
                  .map(info -> some(new DependencyDocumentation(info.getProtocol(), substringBefore(info.getAddress(), "?"))))
                  .append(routeDocumentations
                    .bind(RouteDocumentation::collectLabels)
                    .map(DependencyDocumentation::fromRouteLabel)
                  ))
                .foldLeft(Set::insert, Set.empty(DependencyDocumentation.ord));


            logger.debug("**** Dependencies ****");
            logger.debug(Strings.mkString(DependencyDocumentation.loggerShow, "\n").showS(dependencies.toList()));

            List<Either<String, ResourceDocumentation>> docsAndDesc =
              buildDoc(sourceDir);

            List<ResourceDocumentation> resourceDocs =
              addRequestAndResponse(Either.rights(docsAndDesc), log);

            logger.debug("**** Api docs ****");
            logger.debug(Strings.mkString(ResourceDocumentation.loggerShow, "\n").showS(resourceDocs));

            String desc = Either.lefts(docsAndDesc).headOption().orSome("N/A");

            logger.debug("**** Plugin docs ****");
            logger.debug(desc);

            Model model = ModelBuilder.extractModel(routeDocumentations);
            Map<String, Object> json = ModelBuilder.toJson(model);
            PluginDocumentation doc = new PluginDocumentation(desc, dependencies.toList(), routeDocumentations, resourceDocs, json);


            mapper.writeValue(targetDirectory.resolve("doc.json").toFile(), doc);

            logger.info(String.format("Documenter plugin found %s resources, %s dependencies", resourceDocs.length(), dependencies.size()));

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

    private List<Either<String, ResourceDocumentation>> buildDoc(Path sourceDir) throws URISyntaxException, IOException {
        return
          iterableList(Files.walk(sourceDir)
            .filter(path -> path.toString().contains(".java"))
            .flatMap(path -> {
                try (FileInputStream in = new FileInputStream(path.toFile())) {
                    try {
                        CompilationUnit cu = JavaParser.parse(in);
                        Option<ResourceDocumentation> maybeDoc =
                          ResourceParser.parseResource(cu);

                        Option<String> maybeDesc =
                          ResourceParser.parsePlugin(cu);

                        return maybeDoc.<Stream<Either<String, ResourceDocumentation>>>option(
                          maybeDesc.option(
                            Stream.empty(),
                            desc -> Stream.of(Either.left(desc))),
                          doc -> Stream.of(Either.right(doc)));
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

    private static List<ResourceDocumentation> addRequestAndResponse(
      List<ResourceDocumentation> descs,
      ExchangeLog log) {
        return descs.map(rDoc -> {
            List<MethodDocumentation> mDocs =
              rDoc.methodDocs.map(mDoc -> {
                  List<ExchangeDocumentation> docs =
                    log.asList()
                      .filter(exinfo ->
                        Strings.normalizeUrl(substringBefore(exinfo.getInMessage().getAddress(), "?")).endsWith(mDoc.path) && exinfo.getInMessage().getMethod().equalsIgnoreCase(mDoc.method))
                      .map(exInfo -> {
                          RequestDocumentation requestDocumentation =
                            new RequestDocumentation(exInfo.getInMessage().getAddress(), exInfo.getInMessage().getHeaders(), exInfo.getInMessage().getPayload());

                          ResponseDocumentation responseDocumentation =
                            new ResponseDocumentation(exInfo.getOutMessage().getHeaders(), exInfo.getOutMessage().getPayload(), exInfo.getOutMessage().getResponseCode());

                          return new ExchangeDocumentation(requestDocumentation, responseDocumentation);
                      });
                  return mDoc.withRecorded(docs);
              });
            return new ResourceDocumentation(rDoc.path, rDoc.rolesAllowed, rDoc.documentation, mDocs);
        });
    }

    public static Path getBasedir() {
        return Paths.get(
          DocumenterPlugin.class.getResource("/").getFile().replaceFirst("^/(.:/)", "$1"),
          new String[0])
          .getParent().getParent().getParent().getParent().getParent().getParent().getParent();
    }
}
