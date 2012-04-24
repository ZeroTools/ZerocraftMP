package net.cherrytools.language;

import java.util.LinkedList;
import java.util.List;

import net.cherrytools.language.ast.AddOpNode;
import net.cherrytools.language.ast.AndOpNode;
import net.cherrytools.language.ast.ArrayNode;
import net.cherrytools.language.ast.AssignNode;
import net.cherrytools.language.ast.BlockNode;
import net.cherrytools.language.ast.ClassNode;
import net.cherrytools.language.ast.ConcatOpNode;
import net.cherrytools.language.ast.DictionaryEntryNode;
import net.cherrytools.language.ast.DictionaryNode;
import net.cherrytools.language.ast.DivideOpNode;
import net.cherrytools.language.ast.EqualsOpNode;
import net.cherrytools.language.ast.FalseNode;
import net.cherrytools.language.ast.ForeachNode;
import net.cherrytools.language.ast.FunctionCallNode;
import net.cherrytools.language.ast.FunctionNode;
import net.cherrytools.language.ast.GreaterEqualOpNode;
import net.cherrytools.language.ast.GreaterThenOpNode;
import net.cherrytools.language.ast.Node;
import net.cherrytools.language.ast.IfNode;
import net.cherrytools.language.ast.LessEqualOpNode;
import net.cherrytools.language.ast.LessThenOpNode;
import net.cherrytools.language.ast.LookupNode;
import net.cherrytools.language.ast.ModOpNode;
import net.cherrytools.language.ast.MultiplyOpNode;
import net.cherrytools.language.ast.NegateOpNode;
import net.cherrytools.language.ast.NotEqualsOpNode;
import net.cherrytools.language.ast.NotOpNode;
import net.cherrytools.language.ast.NumberNode;
import net.cherrytools.language.ast.OrOpNode;
import net.cherrytools.language.ast.PowerOpNode;
import net.cherrytools.language.ast.ReturnNode;
import net.cherrytools.language.ast.RootNode;
import net.cherrytools.language.ast.StringNode;
import net.cherrytools.language.ast.SubtractOpNode;
import net.cherrytools.language.ast.TrueNode;
import net.cherrytools.language.ast.VariableNode;
import net.cherrytools.language.ast.WhileNode;

public class Parser {
    // Look ahead buffer for reading tokens from the lexer
    TokenBuffer lookAheadBuffer;

    public Parser(Lexer lexer) {
        lookAheadBuffer = new TokenBuffer(lexer, 2);
    }

    private TokenType lookAhead(int i) {
        if (lookAheadBuffer.isEmpty() || i > lookAheadBuffer.size()) {
            return null; // EOF
        }
        Token token = lookAheadBuffer.getToken(i - 1); // 1-based index
        return token.getType();
    }

    private Token match(TokenType tokenType) {
        Token token = lookAheadBuffer.readToken();
        if (token == null) {
            throw new ParserException("Expecting type " + tokenType + " but didn't get a token");
        }
        if (token.getType() != tokenType) {
            throw new ParserException("Expecting type " + tokenType + " but got " + token.getType(), token.getPosition());
        }
        return token;
    }

    public RootNode program() {
        List<Node> script = new LinkedList<Node>();
        while (lookAhead(1) != null) {
            script.add(statement());
        }
        return new RootNode(new SourcePosition(1, 1), script);
    }

    private BlockNode block() {
        // LBRACE! statement* RBRACE!
        Token lbrace = match(TokenType.LBRACE);
        List<Node> block = new LinkedList<Node>();
        while (lookAhead(1) != TokenType.RBRACE) {
            block.add(statement());
        }
        match(TokenType.RBRACE);
        return new BlockNode(lbrace.getPosition(), block);
    }

    private Node statement() {
        // | (ID LPAREN) => functionCall  END_STATEMENT
        // | VARIABLE! ASSIGN! expression END_STATEMENT
        // | RETURN expression END_STATEMENT
        // | IF | WHILE | FOR_EACH
        TokenType type = lookAhead(1);
        if (type == TokenType.VARIABLE && lookAhead(2) == TokenType.LPAREN) {
            Node funcCall = functionCall();
            match(TokenType.END_STATEMENT);
            return funcCall;
        } else if (type == TokenType.VARIABLE) {
            Node var = variable();
            SourcePosition pos = match(TokenType.ASSIGN).getPosition();
            Node value = expression();
            match(TokenType.END_STATEMENT);
            return new AssignNode(pos, var, value);
        } else if (type == TokenType.RETURN) {
            SourcePosition pos = match(TokenType.RETURN).getPosition();
            Node expression = expression();
            match(TokenType.END_STATEMENT);
            return new ReturnNode(pos, expression);
        } else if (type == TokenType.IF) {
            return _if();
        } else if (type == TokenType.WHILE) {
            return _while();
        }  else if (type == TokenType.CLASS) {
            return _class();
        } else if (type == TokenType.FOR_EACH) {
            return foreach();
        } else {
            // We only get here if there is token from the lexer
            // that is not handled by parser yet.
            throw new ParserException("Unknown token type " + type);
        }
    }

    private Node _class() {
        SourcePosition pos = match(TokenType.CLASS).getPosition();
        Node test = stringExpression();
        Node loopBlock = block();
        return new ClassNode(pos, test, loopBlock);
	}

	private Node condition() {
        match(TokenType.LPAREN);
        Node test = booleanExpression();
        match(TokenType.RPAREN);
        return test;
    }

    private Node _if() {
        // IF! condition block else?
        SourcePosition pos = match(TokenType.IF).getPosition();
        Node test = condition();
        BlockNode thenBlock = block();
        Node elseBlock = null;
        if (lookAhead(1) == TokenType.ELSE) {
            elseBlock = _else();
        }
        return new IfNode(pos, test, thenBlock, elseBlock);
    }

    private Node _else() {
        // ELSE! (if | block)!
        match(TokenType.ELSE);
        if (lookAhead(1) == TokenType.IF) {
            return _if();
        } else {
            return block();
        }
    }

    private Node _while() {
        // WHILE! condition block
        SourcePosition pos = match(TokenType.WHILE).getPosition();
        Node test = condition();
        Node loopBlock = block();
        return new WhileNode(pos, test, loopBlock);
    }

    private Node foreach() {
        // FOREACH! LPAREN! VARIABLE! AS! VARIABLE! (^COLON VARIABLE!) RPAREN!
        // LBRACE! block RBRACE!
        SourcePosition pos = match(TokenType.FOR_EACH).getPosition();
        match(TokenType.LPAREN);
        Token t = match(TokenType.VARIABLE);
        VariableNode onEach = new VariableNode(t.getPosition(), t.getText());
        match(TokenType.AS);
        t = match(TokenType.VARIABLE);
        VariableNode value = new VariableNode(t.getPosition(), t.getText());
        Node as = value;
        if (lookAhead(1) == TokenType.COLON) {
            SourcePosition entryPos = match(TokenType.COLON).getPosition();
            VariableNode key = value;
            t = match(TokenType.VARIABLE);
            value = new VariableNode(t.getPosition(), t.getText());
            as = new DictionaryEntryNode(entryPos, key, value);
        }
        match(TokenType.RPAREN);
        Node loopBlock = block();
        return new ForeachNode(pos, onEach, as, loopBlock);
    }

    private Node array() {
        // LBRACKET! (expression (COMMA^ expression)*)? RBRACKET!
        SourcePosition pos = match(TokenType.LBRACKET).getPosition();
        List<Node> elements = new LinkedList<Node>();
        if (lookAhead(1) != TokenType.RBRACKET) {
            elements.add(expression());
            while (lookAhead(1) == TokenType.COMMA) {
                match(TokenType.COMMA);
                elements.add(expression());
            }
        }
        match(TokenType.RBRACKET);
        return new ArrayNode(pos, elements);
    }

    private DictionaryNode dictionary() {
        // LBRACE! (keyValue (COMMA^ keyValue)*)? RBRACE!
        SourcePosition pos = match(TokenType.LBRACE).getPosition();
        List<DictionaryEntryNode> elements = new LinkedList<DictionaryEntryNode>();
        if (lookAhead(1) != TokenType.RBRACE) {
            elements.add(keyValue());
            while (lookAhead(1) == TokenType.COMMA) {
                match(TokenType.COMMA);
                elements.add(keyValue());
            }
        }
        match(TokenType.RBRACE);
        return new DictionaryNode(pos, elements);
    }

    private DictionaryEntryNode keyValue() {
        // key COLON! expression
        Node key = key();
        SourcePosition pos = match(TokenType.POINT).getPosition();
        Node value = expression();
        return new DictionaryEntryNode(pos, key, value);
    }

    private Node key() {
        // STRING_LITERAL | NUMBER
        if (lookAhead(1) == TokenType.STRING_LITERAL) {
            Token t = match(TokenType.STRING_LITERAL);
            return new StringNode(t.getPosition(), t.getText());
        } else {
            Token t = match(TokenType.NUMBER);
            return new NumberNode(t.getPosition(), t.getText());
        }
    }

    private FunctionNode function() {
        // FUNCTION! LPAREN! parameterList? RPAREN!
        // LBRACE! block() RBRACE!
        SourcePosition pos = match(TokenType.FUNCTION).getPosition();
        match(TokenType.LPAREN);
        List<Node> paramList = FunctionNode.NO_PARAMETERS;
        if (lookAhead(1) != TokenType.RPAREN) {
            paramList = parameterList();
        }
        match(TokenType.RPAREN);
        Node body = block();
        return new FunctionNode(pos, paramList, body);
    }

    private List<Node> parameterList() {
        // (parameter (COMMA! parameter)* )?
        List<Node> parameters = new LinkedList<Node>();
        parameters.add(parameter());
        while (lookAhead(1) == TokenType.COMMA) {
            match(TokenType.COMMA);
            parameters.add(parameter());
        }
        return parameters;
    }

    private Node parameter() {
        // variable (ASSIGN^ expression)?
        Token t = match(TokenType.VARIABLE);
        VariableNode var = new VariableNode(t.getPosition(), t.getText());
        if (lookAhead(1) == TokenType.ASSIGN) {
            SourcePosition pos = match(TokenType.ASSIGN).getPosition();
            Node e = expression();
            return new AssignNode(pos, var, e);
        }
        return var;
    }

    private Node expression() {
        TokenType type = lookAhead(1);
        if (type == TokenType.FUNCTION) {
            return function();
        } else if (type == TokenType.LBRACKET) {
            return array();
        } else if (type == TokenType.LBRACE) {
            return dictionary();
        } else {
            // An expression can result in a string, boolean or number
            return stringExpression();
        }
    }

    private Node sumExpression() {
        // term ((PLUS^|MINUS^) term)*
        Node termExpression = term();
        while (lookAhead(1) == TokenType.PLUS ||
                lookAhead(1) == TokenType.MINUS) {
            if (lookAhead(1) == TokenType.PLUS) {
                termExpression = new AddOpNode(match(TokenType.PLUS).getPosition(),
                    termExpression, term());
            } else if (lookAhead(1) == TokenType.MINUS) {
                termExpression = new SubtractOpNode(match(TokenType.MINUS).getPosition(),
                    termExpression, term());
            }
        }
        return termExpression;
    }

    private Node term() {
        // factor ((MUL^|DIV^|MOD^) factor)*
        Node factorExpression = factor();
        while (lookAhead(1) == TokenType.MULTIPLY ||
                lookAhead(1) == TokenType.DIVIDE ||
                lookAhead(1) == TokenType.MOD) {
            if (lookAhead(1) == TokenType.MULTIPLY) {
                factorExpression = new MultiplyOpNode(
                    match(TokenType.MULTIPLY).getPosition(),
                    factorExpression, factor());
            } else if (lookAhead(1) == TokenType.DIVIDE) {
                factorExpression = new DivideOpNode(
                    match(TokenType.DIVIDE).getPosition(),
                    factorExpression, factor());
            } else if (lookAhead(1) == TokenType.MOD) {
                factorExpression = new ModOpNode(
                    match(TokenType.MOD).getPosition(),
                    factorExpression, factor());
            }
        }
        return factorExpression;
    }

    private Node factor() {
        // signExpr (POW^ signExpr)*
        Node expression = signExpression();
        while (lookAhead(1) == TokenType.POWER) {
            expression = new PowerOpNode(match(TokenType.POWER).getPosition(),
                expression, signExpression());
        }
        return expression;
    }

    private Node signExpression() {
        // (MINUS^|PLUS^)? value
        Token signToken = null;
        if (lookAhead(1) == TokenType.MINUS) {
            signToken = match(TokenType.MINUS);
        } else if (lookAhead(1) == TokenType.PLUS) {
            match(TokenType.PLUS);
        }
        Node value = value();
        if (signToken != null) {
            return new NegateOpNode(signToken.getPosition(), value);
        } else {
            return value;
        }
    }

    private Node value() {
        // (ID LPAREN) => functionCall | atom
        if (lookAhead(1) == TokenType.VARIABLE && lookAhead(2) == TokenType.LPAREN) {
            return functionCall();
        } else {
            return atom();
        }
    }

    private Node functionCall() {
        // f:ID^ LPAREN! argumentList RPAREN!
        Token functionToken = match(TokenType.VARIABLE);
        String functionName = functionToken.getText();
        match(TokenType.LPAREN);
        List<Node> arguments = FunctionCallNode.NO_ARGUMENTS;
        if (lookAhead(1) != TokenType.RPAREN) {
            arguments = argumentList();
        }
        match(TokenType.RPAREN);
        return new FunctionCallNode(functionToken.getPosition(), functionName, arguments);
    }

    private List<Node> argumentList() {
        // (expression (COMMA! expression)* )?
        List<Node> arguments = new LinkedList<Node>();
        arguments.add(expression());
        while (lookAhead(1) == TokenType.COMMA) {
            match(TokenType.COMMA);
            arguments.add(expression());
        }
        return arguments;
    }

    private Node atom() {
        // NUMBER
        // | TRUE | FALSE
        // | LPAREN^ sumExpr RPAREN!
        // | variable
        TokenType type = lookAhead(1);
        if (type == TokenType.NUMBER) {
            Token t = match(TokenType.NUMBER);
            return new NumberNode(t.getPosition(), t.getText());
        } else if (type == TokenType.TRUE) {
            return new TrueNode(match(TokenType.TRUE).getPosition());
        } else if (type == TokenType.FALSE) {
            return new FalseNode(match(TokenType.FALSE).getPosition());
        } else if (type == TokenType.LPAREN) {
            match(TokenType.LPAREN);
            Node atom = expression();
            match(TokenType.RPAREN);
            return atom;
        } else {
            return variable();
        }
    }

    private Node variable() {
        Token t = match(TokenType.VARIABLE);
        Node varNode = new VariableNode(t.getPosition(), t.getText());
        if (lookAhead(1) == TokenType.LBRACKET) {
            SourcePosition pos = match(TokenType.LBRACKET).getPosition();
            Node key = expression();
            match(TokenType.RBRACKET);
            return new LookupNode(pos, (VariableNode) varNode, key);
        } else {
            return varNode;
        }
    }

    private Node booleanExpression() {
        // booleanTerm (OR^ booleanExpression)?
        Node boolTerm = booleanTerm();
        if (lookAhead(1) == TokenType.OR) {
            return new OrOpNode(match(TokenType.OR).getPosition(), boolTerm, booleanExpression());
        }
        return boolTerm;
    }

    private Node booleanTerm() {
        // booleanFactor (AND^ booleanTerm)?
        Node boolFactor = booleanFactor();
        if (lookAhead(1) == TokenType.AND) {
            return new AndOpNode(match(TokenType.AND).getPosition(), boolFactor, booleanTerm());
        }
        return boolFactor;
    }

    private Node booleanFactor() {
        // (NOT^)? relation
        if (lookAhead(1) == TokenType.NOT) {
            return new NotOpNode(match(TokenType.NOT).getPosition(), booleanRelation());
        }
        return booleanRelation();
    }

    private Node booleanRelation() {
        // sumExpr ((LE^ | LT^ | GE^ | GT^ | EQUAL^ | NOT_EQUAL^) sumExpr)?
        Node sumExpr = sumExpression();
        TokenType type = lookAhead(1);
        if (type == TokenType.LESS_EQUAL) {
            return new LessEqualOpNode(match(TokenType.LESS_EQUAL).getPosition(),
                sumExpr, sumExpression());
        } else if (type == TokenType.LESS_THEN) {
            return new LessThenOpNode(match(TokenType.LESS_THEN).getPosition(),
                sumExpr, sumExpression());
        } else if (type == TokenType.GREATER_EQUAL) {
            return new GreaterEqualOpNode(match(TokenType.GREATER_EQUAL).getPosition(),
                sumExpr, sumExpression());
        } else if (type == TokenType.GREATER_THEN) {
            return new GreaterThenOpNode(match(TokenType.GREATER_THEN).getPosition(),
                sumExpr, sumExpression());
        } else if (type == TokenType.EQUAL) {
            return new EqualsOpNode(match(TokenType.EQUAL).getPosition(),
                sumExpr, sumExpression());
        } else if (type == TokenType.NOT_EQUAL) {
            return new NotEqualsOpNode(match(TokenType.NOT_EQUAL).getPosition(),
                sumExpr, sumExpression());
        }
        return sumExpr;
    }

    private Node stringExpression() {
        // string (CONC^ stringExpr)?
        Node stringNode = string();
        if (lookAhead(1) == TokenType.CONCAT) {
            return new ConcatOpNode(match(TokenType.CONCAT).getPosition(),
                stringNode, stringExpression());
        }
        return stringNode;
    }

    private Node string() {
        // STRING_LITERAL | boolExpr
        if (lookAhead(1) == TokenType.STRING_LITERAL) {
            Token t = match(TokenType.STRING_LITERAL);
            return new StringNode(t.getPosition(), t.getText());
        } else {
            return booleanExpression();
        }
    }
}
