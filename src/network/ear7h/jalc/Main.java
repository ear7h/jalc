import java.util.Scanner;

import java.util.Queue;
import java.util.LinkedList;

import java.util.Map;
import java.util.HashMap;

public class Main {
	public static void main(String[] args) {
		System.out.println("welcome to jalc 0.0.0");
		Scanner kbd = new Scanner(System.in);

		for (String[] exp = kbd.nextLine().split(" ");
			exp[0] != "exit";
			exp = kbd.nextLine().split(" ")) {

			try {
				double val = (new Expression(exp)).eval();
				System.out.println(val);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e);
			}
		}
	}
}


class Expression {
	int cursor = 0;
	String[] terms;
	boolean hasResult;
	double result;

	Expression(String[] args) {
		this.terms = args;
	}

	public double eval() throws Exception {
		if (this.hasResult) {
			return this.result;
		}

		double res;
		try {
			res = parseRight();
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new Exception("not enough arguments");
		}

		if (cursor != this.terms.length) {
			throw new Exception("too many arguments, cursor: " + Integer.toString(this.cursor));
		}

		return res;
	}

	double parseRight() throws Exception {
		String op = this.terms[this.cursor++];
		double left;
		try {
			left = Double.parseDouble(this.terms[this.cursor]);
			this.cursor++;
		} catch(NumberFormatException e) {
			left = parseRight();
		}

		double right;
		try {
			right = Double.parseDouble(this.terms[this.cursor]);
			this.cursor++;
		} catch(NumberFormatException e) {
			right = parseRight();
		}

		return evalBinOp(op, left, right);
	}

	static double evalBinOp(String op,double left, double right) throws Exception {
		switch (op) {
			case "add":
				return left + right;
			case "sub":
				return left - right;
			case "mul":
				return left * right;
			case "div":
				return left / right;
			default:
				throw new Exception("unknown operator " + op);
		}
	}
}

class Token {
	public enum Type {
		SYMBOL, NUMBER, STRING, NEWLINE;

		// String toString() {
		// 	switch (this) {
		// 		case SYMBOL:
		// 			return "symbol";
		// 		case NUMBER:
		// 			return "number";
		// 		case STRING:
		// 			return "string";
		// 		case NEWLINE:
		// 			return "newline";
		// 	}
		// }
	}

	Type type;
	int line;
	int col;

	String literal;

	Token(Type t, int line, int col, String literal) {
		this.type = t;
		this.line = line;
		this.col = col;
		this.literal = literal;
	}

	public String toString() {
		return String.format("%d:%d %s %s", this.line, this.col, this.type.toString(), this.literal);
	}
}

// first pass over the source code
class Lexer {
	Queue<Token> tokens;
	Lexer() {
		this.tokens = new LinkedList<>();
	}

	public void input(String src) throws Exception {
		String[] arr = src.split(" ");
		for (int i = 0; i < arr.length; i++) {

			if (arr[i].length() == 0) {
				continue;
			} else if (arr[i] == "\n") {
				this.tokens.add(new Token(Token.Type.NEWLINE, 0, 0, arr[i]));
			} else if (arr[i] == "\\") {
				if (i < arr.length - 1 && arr[i+1] == "\n") {
					i++;
				}
				continue;
			} else if (Character.isLetter(arr[i].charAt(0))) {
				this.tokens.add(new Token(Token.Type.SYMBOL, 0, 0, arr[i]));
			} else if (Character.isDigit(arr[i].charAt(0))) {
				this.tokens.add(new Token(Token.Type.NUMBER, 0, 0, arr[i]));
			} else{
				throw new Exception("unrecognized token: " + arr[i]);
			}

		}
	}

	public Token getToken() {
		return this.tokens.remove();
	}

	public String toString() {
		String ret = "";
		for (Token tk : this.tokens) {
			ret += tk.toString() + "\n";
		}

		return ret;
	}
}

interface Type {
	String name();
}

interface Value {
	void parse(Lexer l);
	String type();
	String toString();
}

abstract class Function {
	static Map<String, Function> funcs = new HashMap<>();
	public static void registerFunc(String name, Function fn) {
		Function.funcs.put(name, fn);
	}

	public static Function get(String name) {
		return funcs.get(name);
	}

	abstract Value parse(Lexer lex);
}