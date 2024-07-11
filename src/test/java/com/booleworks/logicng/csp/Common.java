package com.booleworks.logicng.csp;

import java.util.List;
import java.util.TreeSet;

public class Common {
    public static <G extends Comparable<? extends G>> TreeSet<G> treeSetFrom(final G... elms) {
        final TreeSet<G> set = new TreeSet<>();
        set.addAll(List.of(elms));
        return set;
    }
}
