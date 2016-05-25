package org.kantega.respiro.documenter;

/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import fj.F;
import fj.F2;
import fj.data.List;
import fj.data.Option;
import fj.data.Stream;
import fj.function.Booleans;

import static fj.data.List.*;
import static fj.data.List.iterableList;
import static fj.data.Option.*;
import static fj.data.Option.none;
import static fj.data.Option.some;

/**
 *
 */
public class DocumentationExtractor {

    public static ExchangeLog exchanges = new ExchangeLog();

    public static Option<ResourceDocumentation> document(CompilationUnit root) {
        return fold(root.getChildrenNodes(), ClassOrInterfaceDeclaration.class, none(), (maybeDoc, decl) ->
          maybeDoc.orElse(() ->
            walk(decl, SingleMemberAnnotationExpr.class).exists(hasName("path")) ?
            some(walk(decl, MethodDeclaration.class).foldLeft((rDoc, mDelc) ->
                walk(mDelc, SingleMemberAnnotationExpr.class).exists(hasName("path"))||walk(mDelc,MarkerAnnotationExpr.class).exists(Booleans.or(hasName("get"),hasName("post"))) ?
                rDoc.append(
                  new MethodDocumentation(
                    ofType(mDelc.getChildrenNodes(), SingleMemberAnnotationExpr.class).filter(hasName("path")).headOption().map(a -> rDoc.path+a.getMemberValue().toString()).orSome(rDoc.path),
                    ofType(mDelc.getChildrenNodes(), MarkerAnnotationExpr.class).filter(Booleans.or(hasName("get"), hasName("post"))).headOption().map(a -> a.getName().getName()).orSome("GET"),
                    ofType(mDelc.getChildrenNodes(), SingleMemberAnnotationExpr.class).filter(hasName("rolesallowed")).headOption().map(a -> arrayList(a.getMemberValue().toString().split(","))).orSome(nil()),
                    mDelc.getComment() != null ? mDelc.getComment().getContent() : "",
                    ofType(decl.getChildrenNodes(), Parameter.class).map(Node::toString),
                    nil())) :
                rDoc,
              new ResourceDocumentation(
                ofType(decl.getChildrenNodes(), SingleMemberAnnotationExpr.class).filter(hasName("path")).headOption().map(a -> a.getMemberValue().toString()).orSome("/"),
                ofType(decl.getChildrenNodes(), SingleMemberAnnotationExpr.class).filter(hasName("rolesallowed")).headOption().map(a -> arrayList(a.getMemberValue().toString().split(","))).orSome(nil()),
                decl.getComment() == null ? "" : decl.getComment().getContent(),
                nil()))) :
            none()));

    }


    public static <T> F<Node, Option<T>> cast(Class<T> c) {
        return o -> (c.isInstance(o)) ? some(c.cast(o)) : none();
    }

    public static <A extends AnnotationExpr> F<A, Boolean> hasName(String name) {
        return annotationExpr -> annotationExpr.getName().getName().equalsIgnoreCase(name);
    }

    public static <A, B> B fold(Iterable<Node> nodes, Class<A> tpe, B zero, F2<B, A, B> folder) {
        return somes(iterableList(nodes).map(cast(tpe))).foldLeft(folder, zero);
    }


    public static <A> List<A> ofType(Iterable<Node> nodes, Class<A> tpe) {
        return somes(iterableList(nodes).map(cast(tpe)));
    }

    public static <A> Stream<A> walk(Node node, Class<A> tpe) {
        return cast(tpe).f(node).toStream().append(() -> Stream.iterableStream(node.getChildrenNodes()).bind(n -> walk(n, tpe)));
    }


/*


    public static String print(RootDoc root) throws IOException {

        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);

        w.println("<html>");

        w.println("<head>");
        w.println("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css\" integrity=\"sha512-dTfge/zgoMYpP7QbHy4gWMEGsbsdZeCXz7irItjcC3sPUFtf0kuFbDz/ixG7ArTxmDjLXDmezHubeNikyKGVyQ==\" crossorigin=\"anonymous\">");
        w.println("</head>");
        w.println("<body>");
        w.println("<div class=container>");
        ClassDoc[] classes = root.classes();


        for (int i = 0; i < classes.length; ++i) {
            ClassDoc classDoc = classes[i];
            if (isDocumentationClass(classDoc)) {
                printTitles(w, classDoc, 1);
                w.println("<p>");
                w.println(classDoc.commentText());
                w.println("</p>");
            }
        }

        printTableOfContents(w, classes);

        for (int i = 0; i < classes.length; ++i) {
            ClassDoc classDoc = classes[i];

            if (isDocumentationClass(classDoc)) {
                continue;
            }
            printTitles(w, classDoc, 2);

            w.println("<p>");
            w.println(classDoc.commentText());
            w.println("</p>");

            for (MethodDoc methodDoc : classDoc.methods()) {

                w.println("<a name='" + anchorFor(methodDoc) + "'></a>");

                printTitles(w, methodDoc, 3);
                w.println("<p>");
                w.println(methodDoc.commentText());
                w.println("</p>");

                ExchangeInfo exchange = exchanges.getExchangeLog().get(0);//TODO For now
                if (exchange != null) {

                    w.println("<p><code>");
                    w.println(exchange.getInMessage().getMethod() + " " + exchange.getInMessage().getAddress());
                    w.println("</code></p>");
                    if (exchange.getInMessage().getHeaders() != null) {
                        w.println("<pre>");
                        w.println(exchange.getInMessage().getHeaders());
                        w.println("</pre>");
                    }
                    if (exchange.getInMessage().getPayload() != null) {
                        pre(w, exchange.getInMessage().getPayload());
                    }

                    if (exchange.getOutMessage().getHeaders() != null) {
                        w.println("<pre>");
                        w.print("<div class='label ");
                        ExchangeMessage.ResponseStatus code = exchange.getOutMessage().getResponseStatus();
                        if (code.equals(ExchangeMessage.ResponseStatus.SUCCESS)) {
                            w.print("label-success'");
                        } else if (code.equals(ExchangeMessage.ResponseStatus.WARNING)) {
                            w.print("label-warning'");
                        } else if (code.equals(ExchangeMessage.ResponseStatus.ERROR)) {
                            w.print("label-warning'");
                        } else {
                            w.print("label-default'");
                        }

                        w.print(">");

                        w.print(code);

                        w.println("</div>");
                        w.println(exchange.getOutMessage().getHeaders());
                        w.println("</pre>");
                    }
                    if (exchange.getOutMessage().getPayload() != null) {
                        pre(w, exchange.getOutMessage().getPayload());
                    }
                }
            }
        }

        w.println("</div>");
        w.println("</body>");

        w.println("</html>");

        w.flush();
        w.close();
        return sw.toString();
    }


    private static void printTableOfContents(PrintWriter w, ClassDoc[] classes) {
        w.println("<ul>");

        for (ClassDoc classDoc : classes) {
            if (!isDocumentationClass(classDoc)) {
                w.println("<li>");
                w.println("<a href='#" + classDoc.qualifiedName() + "'>");
                w.print(classDoc.tags("title")[0].text());
                w.println("</a>");

                w.println("<ul>");
                for (MethodDoc methodDoc : classDoc.methods()) {

                    String methodKey = anchorFor(methodDoc);
                    ExchangeInfo exchange = exchanges.getExchangeLog().get(0);//TODO for now
                    if (exchange != null) {
                        w.println("<li>");
                        w.println("<a href='#" + methodKey + "'>");
                        for (Tag title : methodDoc.tags("title")) {
                            w.print(title.text());
                        }
                        w.println("</a>");
                        w.println("</li>");
                    }

                }
                w.println("</ul>");
                w.println("</li>");
            }
        }
        w.println("</ul>");
    }

    private static boolean isDocumentationClass(ClassDoc classDoc) {
        return Arrays.asList(classDoc.annotations()).stream().anyMatch(desc -> desc.annotationType().name().contains("Path"));
    }


    private static void pre(PrintWriter w, String payload) {
        w.println("<pre>");
        w.print(payload.replace("<", "&lt;").replace(">", "&gt;"));
        w.println("</pre>");
    }

    private static void printTitles(PrintWriter w, Doc doc, int level) {
        Tag[] titles = doc.tags("title");
        for (Tag title : titles) {
            w.println("<h" + level + ">");
            w.print(title.text());
            w.println("</h" + level + ">");
        }
    }

    private static File getDestFile(String[][] options) {
        for (int i = 0; i < options.length; i++) {
            String[] option = options[i];
            if ("-dest".equals(option[0])) {
                return new File(option[1]);
            }
        }
        return null;
    }

    public static int optionLength(String option) {
        if (option.equals("-dest")) {
            return 2;
        }
        return 0;
    }

    private static String anchorFor(MethodDoc methodDoc){
        List<AnnotationDesc> annotations = Arrays.asList(methodDoc.annotations());
        String method = annotations.stream().filter(annotationDesc -> annotationDesc.toString().equals("Method")).findFirst().map(Object::toString).orElse("N/A");
        String path = annotations.stream().filter(annotationDesc -> annotationDesc.toString().equals("Path")).findFirst().map(Object::toString).orElse("N/A");
        return method+path;
    }
*/
}
