package com.elith.horizon;

abstract class Expr {
    static class Binary extends Expr {
        Binary(Expr left, Token op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        final Expr left;
        final Token op;
        final Expr right;
    }
}
