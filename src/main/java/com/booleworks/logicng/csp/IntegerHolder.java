package com.booleworks.logicng.csp;

public interface IntegerHolder extends Comparable<IntegerHolder> {
    IntegerDomain getDomain();

    @Override
    default int compareTo(final IntegerHolder o) {
        if (this == o) {
            return 0;
        }
        if (o == null) {
            return 1;
        }
        final int ub1 = getDomain().ub();
        final int ub2 = o.getDomain().ub();
        if (ub1 != ub2) {
            return ub1 < ub2 ? -1 : 1;
        }
        return this.toString().compareTo(o.toString());
    }
}
