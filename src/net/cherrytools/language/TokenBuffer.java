package net.cherrytools.language;

import java.util.LinkedList;

public class TokenBuffer {
    private LinkedList<Token> tokenQueue;
    private Lexer lexer;

    public TokenBuffer(Lexer lexer, int size) {
        this.lexer = lexer;
        tokenQueue = new LinkedList<Token>();

        // init queue
        for (int i = 0; i < size; i++) {
            Token token = next();
            if (token == null) {
                break;
            }
            tokenQueue.addLast(token);
        }
    }

    private Token next() {
        Token token = lexer.nexttoken();
        while (token != null && token.getType() == TokenType.COMMENT) {
            token = lexer.nexttoken();
        }
        return token;
    }

    public boolean isEmpty() {
        return tokenQueue.isEmpty();
    }

    public int size() {
        return tokenQueue.size();
    }

    public Token getToken(int i) {
        return tokenQueue.get(i);
    }

    /**
     * Read the next token from the lexer
     */
    public Token readToken() {
        if (tokenQueue.isEmpty()) {
            return null;
        }
        Token token = tokenQueue.removeFirst();

        // Add another token to the queue
        Token newToken = next();
        if (newToken != null) {
            tokenQueue.addLast(newToken);
        }
        return token;
    }
}
