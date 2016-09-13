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
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import fj.F;
import fj.F2;
import fj.data.List;
import fj.data.Option;
import fj.data.Stream;
import fj.function.Booleans;
import org.apache.commons.lang3.StringUtils;

import static fj.data.List.*;
import static fj.data.List.iterableList;
import static fj.data.Option.*;
import static fj.data.Option.none;
import static fj.data.Option.some;
import static fj.function.Booleans.*;
import static org.kantega.respiro.documenter.Strings.normalizeUrl;

/**
 *
 */
public class ResourceParser {

    public static ExchangeLog exchanges = new ExchangeLog();

    public static Option<String> parsePlugin(CompilationUnit root) {
        return fold(root.getChildrenNodes(), ClassOrInterfaceDeclaration.class, none(), (maybeDoc, decl) ->
          maybeDoc.orElse(() ->
            walk(decl, MarkerAnnotationExpr.class).exists(hasName("plugin")) ?
            Option.fromNull(decl.getComment()).map(Comment::getContent) :
            none()
          ));
    }


    public static Option<ResourceDocumentation> parseResource(CompilationUnit root) {
        return fold(root.getChildrenNodes(), ClassOrInterfaceDeclaration.class, none(), (maybeDoc, decl) ->
          maybeDoc.orElse(() ->
            walk(decl, SingleMemberAnnotationExpr.class).exists(hasName("path")) ?
            some(walk(decl, MethodDeclaration.class).foldLeft((rDoc, mDelc) ->
                walk(mDelc, SingleMemberAnnotationExpr.class).exists(hasName("path")) || walk(mDelc, MarkerAnnotationExpr.class).exists(or(hasName("get"), hasName("post"))) ?
                rDoc.append(
                  new MethodDocumentation(
                    ofType(mDelc.getChildrenNodes(), SingleMemberAnnotationExpr.class).filter(hasName("path")).headOption().map(a -> rDoc.path + normalizeUrl(a.getMemberValue().toString())).orSome(rDoc.path),
                    ofType(mDelc.getChildrenNodes(), MarkerAnnotationExpr.class).filter(or(hasName("get"), hasName("post"))).headOption().map(a -> a.getName().getName()).orSome("GET"),
                    ofType(mDelc.getChildrenNodes(), SingleMemberAnnotationExpr.class).filter(hasName("rolesallowed")).headOption().map(a -> arrayList(a.getMemberValue().toString().split(","))).orSome(nil()),
                    mDelc.getComment() != null ? mDelc.getComment().getContent() : "",
                    iterableList(mDelc.getParameters()).map(Node::toString),
                    nil())) :
                rDoc,
              new ResourceDocumentation(
                ofType(decl.getChildrenNodes(), SingleMemberAnnotationExpr.class).filter(hasName("path")).headOption().map(a -> a.getMemberValue().toString()).map(Strings::normalizeUrl).orSome("/"),
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



}
