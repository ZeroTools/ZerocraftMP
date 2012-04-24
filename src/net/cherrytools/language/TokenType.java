package net.cherrytools.language;

public enum TokenType {
    COMMENT, ASSIGN,
    PLUS, MINUS, MULTIPLY, DIVIDE, MOD, POWER, // Math operators
    LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET, COMMA, COLON, END_STATEMENT,
    CONCAT, // String operators
    NOT, AND, OR, // Boolean operators
    LESS_THEN, LESS_EQUAL, EQUAL, GREATER_EQUAL, GREATER_THEN, NOT_EQUAL, // Comparison operators
    NUMBER, STRING_LITERAL, TRUE, FALSE, // Constant types
    IF, ELSE, WHILE, FOR_EACH, AS, // Control structures
    VARIABLE, FUNCTION, RETURN, FOR, CLASS // Other keywords
    , POINT

}
