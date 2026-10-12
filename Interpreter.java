
package jreme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

public class Interpreter {

    public String code;

    public HashMap<String, SExpr> stack = new HashMap<>();

    public String dontEval = "literal print-literal if define else cond switch";

    public Interpreter (String code) {
        this.code = code;
        SExpr.stack = this.stack;
    }

    public void run() {
        for (SExpr s : extract(new SExpr(preprocess(code), Types.multiline))) {
            eval(s);
        }
    }

    public String preprocess(String code) {
        boolean string = false;
        String buf = "";
        for (int i = 0; i < code.length(); i ++) {
            if (i < code.length() - 1) {
                if (code.charAt(i) == '\\' && code.charAt(i + 1) == '"') {
                    buf += "\"";
                    i += 1;
                    continue;
                }
            }
            if (code.charAt(i) == '"') {
                if (i > 0) {
                    if (code.charAt(i - 1) == '\\') {
                        i ++;
                        continue;
                    }
                }
                string = !string;
                if (string) buf += "(literal ";
                if (!string) buf += ")";
            }
            else {
                buf += code.charAt(i) + "";
            }
        }
        return buf;
    }

    public ArrayList<SExpr> extract(SExpr expr) {
        String exp = expr.expr.trim();

        if (expr.type == Types.multiline) {
            ArrayList<SExpr> lines = new ArrayList<>();

            int parLevel = 0;
            String buf = "";
            for (int i = 0; i < exp.length(); i ++) {
                String c = exp.charAt(i) + "";
                if (c.equals("(")) parLevel += 1;
                if (c.equals(")")) parLevel -= 1;
                if (parLevel > 0) {
                    buf += c;
                }
                else if (parLevel == 0) {
                    if (!buf.trim().isEmpty()) {
                        lines.add(new SExpr(buf + ")", Types.expression));
                        buf = "";
                        i ++;
                    }
                }
            }
            return lines;
        }

        System.out.println("-->No Lines");

        return new ArrayList<SExpr>();
    }

    public SExpr eval(SExpr expr) {
        String exp = expr.expr.trim();

        if (expr.type == Types.expression) {
            if (exp.startsWith("(") && exp.endsWith(")")) {
                exp = exp.substring(1, exp.length() - 1).trim();
            }

            ArrayList<String> terms = splitTerms(exp);

            if (terms.isEmpty()) {
                return new SExpr("Empty Terms", Types.error);
            }

            String funcName = terms.get(0);

            if (Arrays.asList(dontEval.split(" ")).contains(funcName)) {
                return function(terms);
            }

            ArrayList<String> evaluatedTerms = new ArrayList<>();
            evaluatedTerms.add(terms.get(0));

            for (int i = 1; i < terms.size(); i++) {
                String term = terms.get(i);
                if (term.startsWith("(")) {
                    SExpr result = eval(new SExpr(term, Types.expression));
                    evaluatedTerms.add(result.expr);
                }
                else {
					if (stack.containsKey(term)) {
			            evaluatedTerms.add(stack.get(term).expr);
			        }
			        else {
			            evaluatedTerms.add(term);
			        }
                }
            }

            return function(evaluatedTerms);
        }

        return expr;
    }

    public ArrayList<String> splitTerms(String exp) {
        ArrayList<String> terms = new ArrayList<>();
        int parLevel = 0;
        String buf = "";

        for (int i = 0; i < exp.length(); i++) {
            char c = exp.charAt(i);

            if (c == '(') {
                parLevel += 1;
            }
            else if (c == ')') {
                parLevel -= 1;
            }

            if (c == ' ' && parLevel == 0) {
                if (buf.length() > 0) {
                    terms.add(buf.toString().trim());
                    buf = "";
                }
            }
            else if (c == '(' && parLevel == 1 && buf.length() > 0) {
                terms.add(buf.trim());
                buf = "";
                buf += c;
            }
            else {
                buf += c;
            }
        }

        if (buf.length() > 0) {
            terms.add(buf.trim());
        }

        return terms;
    }

    public SExpr function(ArrayList<String> t) {
        try {
            if (t.size() <= 0) {
                return new SExpr("Illegal argument count", Types.error);
            }

            SExpr ret = new SExpr("null", Types.error);

            if (stack.containsKey(t.get(0))) {
                SExpr uf = stack.get(t.get(0));
                HashMap<String, SExpr> savedStack = new HashMap<>();

                for (int i = 0; i < stack.get(t.get(0)).params.size(); i++) {
                    String pn = uf.params.get(i);
                    String argVal = t.get(i + 1);

                    if (stack.containsKey(pn)) {
                        savedStack.put(pn, stack.get(pn));
                    }

                    stack.put(pn, packageNumber(new SExpr(argVal, Types.string)));
                }

                //System.out.println(uf);

                ArrayList<SExpr> bodyStatements = extract(new SExpr(uf.b, Types.multiline));

                for (SExpr stmt : bodyStatements) {
                    ret = eval(stmt);
                }

                for (String pn : stack.get(t.get(0)).params) {
                    if (savedStack.containsKey(pn)) {
                        stack.put(pn, savedStack.get(pn));
                    } else {
                        stack.remove(pn);
                    }
                }
            }
            else{
                switch(t.get(0)) {
                    case "add":

                        if ((packageNumber(t.get(1)) + " " + packageNumber(t.get(2))).contains("DOUBLE")) {
                            ret = packageNumber(new SExpr("" + (
                                Double.parseDouble(t.get(1)) + Double.parseDouble(t.get(2))
                            ), Types.doub));
                        }
                        else {
                            ret = packageNumber(new SExpr("" + (
                                Integer.parseInt(t.get(1)) + Integer.parseInt(t.get(2))
                            ), Types.integer));
                        }

                        ret = packageNumber(ret);
                    break;


                    case "mult":

                        if ((packageNumber(t.get(1)) + " " + packageNumber(t.get(2))).contains("DOUBLE")) {
                            ret = packageNumber(new SExpr("" + (
                                Double.parseDouble(t.get(1)) * Double.parseDouble(t.get(2))
                            ), Types.doub));
                        }
                        else {
                            ret = packageNumber(new SExpr("" + (
                                Integer.parseInt(t.get(1)) * Integer.parseInt(t.get(2))
                            ), Types.integer));
                        }

                        ret = packageNumber(ret);
                    break;

                    case "print-stack":
                        for (String name : stack.keySet()) {
                            System.out.println(name + " :: " + stack.get(name));
                        }
                    break;

                    case "print":
                        String f = "";
                        for (int i = 1; i < t.size(); i ++) {
                            f += t.get(i) + " ";
                        }
                        System.out.println(f);
                        ret = success();
                    break;

                    case "if":
                        /*for (String __ : t) {
                            System.out.println("-->" + __);
                        }*/

                        if(eval(new SExpr(t.get(1), Types.expression)).getErrorCatch().trim().equals("true")) {
                            ret = eval(new SExpr(t.get(2), Types.expression));
                        }
                        else {
                            ret = eval(new SExpr(t.get(3), Types.expression));
                        }
                    break;

                    case "=":
                        boolean eq = Double.parseDouble(t.get(1)) == Double.parseDouble(t.get(2));
                        if (eq) ret = new SExpr("true", Types.bool);
                        else ret = new SExpr ("false", Types.bool);
                    break;

                    case "print-literal":
                        f = "";
                        for (int i = 1; i < t.size(); i ++) {
                            f += t.get(i) + " ";
                        }
                        System.out.println(f);
                        ret = success();
                    break;

                    case "literal":
                        f = "";
                        for (int i = 1; i < t.size(); i ++) {
                            f += t.get(i) + " ";
                        }
                        ret = new SExpr(f, Types.string);
                    break;

                    case "copy":
                        try {
                            stack.put(t.get(1), stack.get(t.get(0)));
                            ret = success();
                        }
                        catch(Exception g) {
                            ret = fail();
                        }
                    break;

                    case "define":

                        boolean func = false;

                        for (int i = 1; i < t.size(); i ++) {
                            if (t.get(i).contains("(")) {
                                func = true;
                                break;
                            }
                        }

                        if (func) {
                            String funcName = t.get(1);
                            String rawParams = t.get(2);

                            String bodyExpr = "";

                            for (int i = 3; i < t.size(); i ++) {
                                bodyExpr += t.get(i) + " ";
                            }

                            if (rawParams.startsWith("(") && rawParams.endsWith(")")) {
                                rawParams = rawParams.substring(1, rawParams.length() - 1).trim();
                            }

                            ArrayList<String> paramList = new ArrayList<>(Arrays.asList(rawParams.split(" ")));
                            stack.put(funcName, new SExpr(paramList, bodyExpr));

                            ret = success();
                        }
                        else {
                            String n = t.get(1);
                            String v = t.get(2);
                            stack.put(n, eval(packageNumber(new SExpr(v, Types.expression))));
                            ret = success();
                        }
                    break;

                    case "get-type":
                        String _t = t.get(1);

                        if (stack.containsKey(_t)) ret = new SExpr("SYMBOL", Types.string);
                        else ret = new SExpr(Types.toString(packageNumber(new SExpr(_t, Types.string)).type), Types.string);
                    break;

                    case "get":

                        String n_ = t.get(1);
                        ret = stack.get(n_);

                        if (ret == null) ret = new SExpr("Variable not found", Types.error);

                    break;
                }
            }

            if (ret.expr.equals("null")) ret = new SExpr("FunctionNoExecute: " + t.toString(), Types.error).errorCatch(t.get(0));

            return ret;

        }
        catch(Exception g) {
            return new SExpr(g.toString(), Types.error);
        }
    }

    public SExpr success() {
        return new SExpr("SUCCESS", Types.string);
    }

    public SExpr fail() {
        return new SExpr("FAIL", Types.string);
    }


    public String packageNumber(String g) {
        try {
            if (g.contains(".")) {
                Double.parseDouble(g);
                return "DOUBLE";
            }
            else {
                Integer.parseInt(g);
                return "INT";
            }
        }
        catch(Exception j) {
            return "DOUBLE";
        }
    }

    public SExpr packageNumber(SExpr g) {
        try {
            if (g.expr.contains(".")) {
                Double.parseDouble(g.expr);
                return g.setType(Types.doub);
            }
            else {
                Integer.parseInt(g.expr);
                return g.setType(Types.integer);
            }
        }
        catch(Exception j) {
            return g.setType(Types.string);
        }
    }

}



