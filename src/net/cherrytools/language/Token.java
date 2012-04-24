package net.cherrytools.language;

public class Token {
    private SourcePosition position;
    private TokenType type;
    private String text;

    public Token(SourcePosition position, TokenType type, String text) {
        this.position = position;
        this.type = type;
        this.text = text;
    }

    public SourcePosition getPosition() {
        return position;
    }

    public TokenType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Token))
            return false;
        Token other = (Token) obj;
        return this.type == other.type && this.text.equals(other.text) && this.position.equals(other.position);
    }

    @Override
    public String toString() {
        return type + ",'" + text + "'";
    }
}
