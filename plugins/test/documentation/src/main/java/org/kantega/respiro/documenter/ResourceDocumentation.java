package org.kantega.respiro.documenter;

import fj.Show;
import fj.data.List;
import fj.data.Stream;
import org.apache.commons.lang3.StringUtils;

import static fj.data.Stream.*;
import static org.kantega.respiro.documenter.Strings.mkString;
import static org.kantega.respiro.documenter.Strings.nl;

public class ResourceDocumentation {

    public static final Show<ResourceDocumentation> loggerShow =
      Show.show(rd ->
        Strings.nil.append(fromString("Resource")).append(nl)
          .append(fromString("Base path: ")).append(fromString(rd.path)).append(nl)
          .append(fromString("Roles allowed: ")).append(mkString(Show.stringShow, ",").show(rd.rolesAllowed))
          .append(fromString(rd.documentation)).append(nl)
          .append(nl)
          .append(mkString(MethodDocumentation.loggerShow, "\n").show(rd.methodDocs)).append(nl).append(nl).append(nl));

    public final String path;
    public final List<String> rolesAllowed;
    public final String documentation;
    public final List<MethodDocumentation> methodDocs;

    public ResourceDocumentation(
      String path,
      List<String> rolesAllowed,
      String documentation,
      List<MethodDocumentation> methodDocs) {
        this.path = path;
        this.rolesAllowed = rolesAllowed;
        this.documentation = StringUtils.remove(documentation, "*");
        this.methodDocs = methodDocs;
    }

    public ResourceDocumentation append(MethodDocumentation md) {
        return new ResourceDocumentation(path, rolesAllowed, documentation, methodDocs.snoc(md));
    }

    public ResourceDocumentation append(String roleAllowed) {
        return new ResourceDocumentation(path, rolesAllowed.cons(roleAllowed), documentation, methodDocs);
    }

    @Override
    public String toString() {
        return "ResourceDocumentation{" +
          "path='" + path + '\'' +
          ", rolesAllowed=" + rolesAllowed +
          ", documentation='" + documentation + '\'' +
          ", methodDocs=" + methodDocs +
          '}';
    }
}
