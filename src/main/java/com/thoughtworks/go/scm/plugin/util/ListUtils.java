package com.thoughtworks.go.scm.plugin.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ListUtils {
    public static boolean isEmpty(List list) {
        return list == null || list.isEmpty();
    }

    public static String join(Collection c) {
        return join(c, ", ");
    }

    public static String join(Collection c, String join) {
        StringBuffer sb = new StringBuffer();
        for (Iterator<Object> iter = c.iterator(); iter.hasNext(); ) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(join);
            }
        }
        return sb.toString();
    }

    public static String[] toArray(List<String> args) {
        return args.toArray(new String[args.size()]);
    }
}
