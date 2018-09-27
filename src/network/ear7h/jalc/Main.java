import java.util.Scanner;

import java.util.Queue;
import java.util.LinkedList;

import java.util.Map;
import java.util.HashMap;

public class Main {
	public static void main(String[] args) {
		System.out.println("welcome to jalc 0.0.0");
		Scanner kbd = new Scanner(System.in);
		Lexer lex = new Lexer();

		for (String exp = kbd.nextLine();
			!exp.equals("exit");
			exp = kbd.nextLine()) {
			try {
				lex.add(exp + "\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(lex.toString());
		}
		System.out.println("exiting");
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
		IDENT, NUMBER, STRING, SPACE, NEWLINE, BACKSLASH;
	}

	public final Type type;
	public final int line;
	public final int col;

	public final String literal;

	Token(Type t, int line, int col, String literal) {
		this.type = t;
		this.line = line;
		this.col = col;
		this.literal = literal;
	}

	public String toString() {
		return String.format("%d:%d %s %s;", this.line, this.col, this.type.toString(), this.literal);
	}
}

class SourceScanner{
	String data;
	int cursor = -1;
	int line = 0;
	int col = 0;
	boolean hadNewline = false;

	public SourceScanner(String src) {
		this.data = src;
	}

	public int getLn() {
		return this.line;
	}

	public int getCol() {
		return this.col;
	}

	public boolean scan() {
		this.cursor++;
		if (cursor >= this.data.length()) {
			return false;
		}
		char c = this.data.charAt(this.cursor);
		if (hadNewline) {
			this.line++;
			this.col = 0;
		} else {
			this.col++;
		}
		this.hadNewline = c == '\n';
		return true;
	}

	public char current() {
		return this.data.charAt(this.cursor);
	}

	public char next() {
		int ni = this.cursor + 1;
		if (ni >= this.data.length()) {
			return '\0';
		}
		return this.data.charAt(ni);
	}

	public boolean hasNext() {
		return this.cursor < this.data.length() - 1;
	}
}

// first pass over the source code
class Lexer {
	Queue<Token> tokens;
	Lexer() {
		this.tokens = new LinkedList<>();
	}

	public void add(String src) throws Exception {
		System.out.println(src + ";");
		System.out.println(src.charAt(src.length() - 1) == '\n');
		SourceScanner scanner = new SourceScanner(src);

		while (scanner.scan()){
			//curChar = scanner.next()) {
			System.out.println("char: " + scanner.current());

			// whitespace
			if (scanner.current() == ' ') {
				this.tokens.add(new Token(Token.Type.SPACE, scanner.getLn(), scanner.getCol(), " "));
			// newline
			} else if (scanner.current() == '\n') {
				this.tokens.add(new Token(Token.Type.NEWLINE, scanner.getLn(), scanner.getCol(), "\n"));
			// backslash
			} else if (scanner.current() == '\\') {
				this.tokens.add(new Token(Token.Type.BACKSLASH, scanner.getLn(), scanner.getCol(), "\\"));
			// numeric literal
			} else if (Character.isDigit(scanner.current())){
				String lit = "" + scanner.current();
				while (scanner.hasNext() && (
					Character.isDigit(scanner.next()) || 
					Character.isLetter(scanner.next()))) {
					scanner.scan();
					scanner.current();
				}

				this.tokens.add(new Token(Token.Type.NUMBER, scanner.getLn(), scanner.getCol(), lit));
			// identifier
			} else if (Character.isLetter(scanner.current())) {
				String lit = "" + scanner.current();
				while (scanner.hasNext() && (
					Character.isDigit(scanner.next()) || 
					Character.isLetter(scanner.next()))) {
					scanner.scan();
					lit += scanner.current();
				}

				this.tokens.add(new Token(Token.Type.IDENT, scanner.getLn(), scanner.getCol(), lit));
			} else {
				throw new Exception("unrecognized token: " + scanner.current());
			}
		}
	}

	public boolean hasToken() {
		return !this.tokens.empty();
	}

	public Token getToken() {
		return this.tokens.remove();
	}

	public String toString() {
		String ret = "";
		for (Token tk : this.tokens) {
			ret += tk.toString() + "\n";
		}

		return ret.substring(0, ret.length() - 1);
	}
}

class Proc {
	// a procedure is a list of statements
	Queue<Stmt> stmts;

	public void exec() {
		for (var stmt : this.stmts) {
			stmt.exec();
		}
	}
}

class Prog extends Proc {
	definition
}

abstract class Stmt {
	public enum Type {
		EXPR, // simplifies to single value `add 1 1` or `1 + 1`
		ASSIGN, // `{IDENT} = EXPR` or 
		DEC // `var {IDENT} {IDENT}` where the second IDENT refers to a type. Or `var {IDENT} = EXPR` ;
	}
	abstract public void exec();
}

class Parser {
	Lexer lex;

	public Parser(Lexer lex) {
		this.lex = lex;
	}

	public String parse() {
		Token curToken;
		while (this.lex.hasToken()) {
			// read entire statement
			Queue<Token> stmt = new LinkedList<>();

			while(this.lex.hasToken()) {
				Token tk = this.lex.getToken();
				if ()
			}

			switch (curToken.type) {
			}

		}
	}
}

abstract class Expression {
	Lexer lex;
	public Expression(Lexer lex) {
		this.lex = lex;
	}

	abstract public Value eval();
}

interface Value {
	//public String kind();
	public String type();
	public String toString();
}

class Heap {
	Map<String, Expression> symbols;
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