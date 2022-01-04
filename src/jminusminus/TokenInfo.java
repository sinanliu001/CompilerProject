// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * An enum of token kinds. Each entry in this enum represents the kind of a token along with its
 * image (string representation).
 */
enum TokenKind {
    // End of file.
    EOF(""),

    // Reserved words.
    ABSTRACT("abstract"), BOOLEAN("boolean"), BREAK("break"), CASE("case"), CATCH("catch"),
    CLASS("class"), CONTINUE("continue"), DEFLT("default"), DO("do"), DOUBLE("double"),
    ELSE("else"), EXTENDS("extends"), FINALLY("finally"), FOR("for"), CHAR("char"), IF("if"),
    IMPLEMENTS("implements"), IMPORT("import"), INSTANCEOF("instanceof"), INT("int"),
    INTERFACE("interface"), LONG("long"), NEW("new"), PACKAGE("package"), PRIVATE("private"),
    PROTECTED("protected"), PUBLIC("public"), RETURN("return"), STATIC("static"), SUPER("super"),
    SWITCH("switch"), THIS("this"), THROW("throw"), THROWS("throws"), TRY("try"), VOID("void"),
    WHILE("while"),

    // Operators.
    ALSHIFT("<<"), ALSHIFT_ASSIGN("<<="), AND("&"), AND_ASSIGN("&="), ARSHIFT(">>"),
    ARSHIFT_ASSIGN(">>="), ASSIGN("="), COLON(":"), DEC("--"), DIV("/"), DIV_ASSIGN("/="),
    EQUAL("=="), GE(">="), GT(">"), INC("++"), LAND("&&"), LE("<="), LNOT("!"), LOR("||"),
    LRSHIFT(">>>"), LRSHIFT_ASSIGN(">>>="), LT("<"), MINUS("-"), MINUS_ASSIGN("-="), NOT("~"),
    NOT_EQUAL("!="), OR("|"), OR_ASSIGN("|="), PLUS("+"), PLUS_ASSIGN("+="), QUESTION("?"),
    REM("%"), REM_ASSIGN("%="), STAR("*"), STAR_ASSIGN("*="), XOR("^"), XOR_ASSIGN("^="),

    // Separators.
    COMMA(","), DOT("."), LBRACK("["), LCURLY("{"), LPAREN("("), RBRACK("]"), RCURLY("}"),
    RPAREN(")"), SEMI(";"),

    // Identifiers.
    IDENTIFIER("<IDENTIFIER>"),

    // Literals.
    NULL("null"), FALSE("false"), TRUE("true"), INT_LITERAL("<INT_LITERAL>"),
    CHAR_LITERAL("<CHAR_LITERAL>"), STRING_LITERAL("<STRING_LITERAL>"),
    LONG_LITERAL("<LONG_LITERAL>"), DOUBLE_LITERAL("<DOUBLE_LITERAL>");

    // The token kind's string representation.
    private String image;

    /**
     * Constructs an instance of TokenKind given its string representation.
     *
     * @param image string representation of the token kind.
     */
    private TokenKind(String image) {
        this.image = image;
    }

    /**
     * Returns the token kind's string representation.
     *
     * @return the token kind's string representation.
     */
    public String tokenRep() {
        if (this == EOF) {
            return "<EOF>";
        }
        if (image.startsWith("<") && image.endsWith(">")) {
            return image;
        }
        return "\"" + image + "\"";
    }

    /**
     * Returns the token kind's image.
     *
     * @return the token kind's image.
     */
    public String image() {
        return image;
    }
}

/**
 * A representation of tokens returned by the Scanner method getNextToken(). A token has a kind
 * identifying what kind of token it is, an image for providing any semantic text, and the line in
 * which it occurred in the source file.
 */
public class TokenInfo {
    // Token kind.
    private TokenKind kind;

    // Semantic text (if any). For example, the identifier name when the token kind is IDENTIFIER
    // . For tokens without a semantic text, it is simply its string representation. For example,
    // "+=" when the token kind is PLUS_ASSIGN.
    private String image;

    // Line in which the token occurs in the source file.
    private int line;

    /**
     * Constructs a TokenInfo object given its kind, the semantic text forming the token, and its
     * line number.
     *
     * @param kind  the token's kind.
     * @param image the semantic text forming the token.
     * @param line  the line in which the token occurs in the source file.
     */
    public TokenInfo(TokenKind kind, String image, int line) {
        this.kind = kind;
        this.image = image;
        this.line = line;
    }

    /**
     * Constructs a TokenInfo object given its kind and its line number. Its image is simply the
     * token kind's string representation.
     *
     * @param kind the token's identifying number.
     * @param line the line in which the token occurs in the source file.
     */
    public TokenInfo(TokenKind kind, int line) {
        this(kind, kind.image(), line);
    }

    /**
     * Returns the token's kind.
     *
     * @return the token's kind.
     */
    public TokenKind kind() {
        return kind;
    }

    /**
     * Returns the line number associated with the token.
     *
     * @return the line number associated with the token.
     */
    public int line() {
        return line;
    }

    /**
     * Returns the token's string representation.
     *
     * @return the token's string representation.
     */
    public String tokenRep() {
        return kind.tokenRep();
    }

    /**
     * Returns the token's image.
     *
     * @return the token's image.
     */
    public String image() {
        return image;
    }
}
