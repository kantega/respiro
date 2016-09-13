package org.kantega.respiro.documenter;

import fj.Show;
import fj.data.List;
import fj.data.Stream;
import org.apache.commons.lang3.StringUtils;

public class Strings {
    public static final Stream<Character> nil = Stream.fromString("");
    public static final Stream<Character> space = Stream.fromString(" ");
    public static final Stream<Character> lparen = Stream.fromString("(");
    public static final Stream<Character> rparen = Stream.fromString(")");
    public static final Stream<Character> nl = Stream.fromString("\n");
    public static final Stream<Character> indent = Stream.fromString("\t");
    public static final Stream<Character> rarrow = Stream.fromString("->");

    public static Stream<Character> toIndent(int i) {
        if (i == 0) { return Stream.fromString(""); }
        return indent.append(toIndent(i - 1));
    }

    public static <T> Show<List<T>>  mkString(Show<T> s,String separator){
        return Show.show(ts->{
            if(ts.isEmpty())
                return Stream.nil();

            T head = ts.head();
            List<T> tail = ts.tail();

            if(tail.isEmpty())
                return s.show(head);

            else
                return s.show(head).append(Stream.fromString(separator)).append(mkString(s,separator).show(tail));
        });
    }
    public static String normalizeUrl(String url) {
        return StringUtils.removeEnd(StringUtils.prependIfMissing(StringUtils.remove(url, "\""), "/"), "/");
    }

}
