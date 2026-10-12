package jreme;

import java.util.HashMap;
import java.util.ArrayList;

public class SExpr {
    public String expr;
    public int type;
    public static HashMap<String, SExpr> stack;

    public ArrayList<String> params = new ArrayList<>();
    public String b;

    public String errorCatchValue = "";

    public SExpr (ArrayList<String> params, String b) {
        this.params = params;
        this.b = b;
        this.type = Types.function;
        this.expr = b;
    }

    public SExpr (String expr, int type) {
        this.expr = expr;
        this.type = type;
    }

    public static SExpr deduceType (SExpr expr) {
        
        return expr;
    }

    public String toString() {
        if (type == Types.function) {
            return "<Function: " + params.toString() + " ==> " + b.substring(0, b.length() - 1) + ">";
        }
        else {
            return "<" + Types.toString(type).toUpperCase() + ":" + expr + ">";
        }
    }

    public SExpr errorCatch(String e) {
        this.errorCatchValue = e;
        return this;
    }

    public String getErrorCatch() {
        return this.errorCatchValue.isEmpty() ? this.expr : this.errorCatchValue;
    }

    public String literalValue() {
        return expr;
    }

    public SExpr setType(int t) {
        this.type = t;
        return this;
    }

    public SExpr setExpr(String e) {
        this.expr = e;
        return this;
    }
}
