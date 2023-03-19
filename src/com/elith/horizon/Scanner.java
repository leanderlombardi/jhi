package com.elith.horizon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elith.horizon.TokenType.*;
import static java.lang.Character.isDigit;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",      AND);
        keywords.put("class",    CLASS);
        keywords.put("else",     ELSE);
        keywords.put("false",    FALSE);
        keywords.put("for",      FOR);
        keywords.put("function", FUNCTION);
        keywords.put("if",       IF);
        keywords.put("nil",      NIL);
        keywords.put("or",       OR);
        keywords.put("return",   RETURN);
        keywords.put("super",    SUPER);
        keywords.put("self",     SELF);
        keywords.put("true",     TRUE);
        keywords.put("let",      LET);
        keywords.put("while",    WHILE);
    }

    Scanner(String source)  {
        this.source = source;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c == '_');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            horizon.error(line, "Unterminated string");
            return;
        }

        // The closing '"'
        advance();

        // Trim surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume '.'
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENT;

    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LPAREN); break;
            case ')': addToken(RPAREN); break;
            case '{': addToken(LBRACE); break;
            case '}': addToken(RBRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMIC); break;
            case '*': addToken(MUL); break;
            case '!':
                addToken(match('=') ? NEQ : BANG);
                break;
            case '=':
                addToken(match('=') ? EQEQ : EQ);
                break;
            case '<':
                addToken(match('=') ? GEQ : LT);
                break;
            case '>':
                addToken(match('?') ? GEQ : GT);
                break;
            case '/':
                if (match('/')) {
                    // Comment: expr* '//' ID*
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(DIV);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore WS
                break;
            case '\n':
                line++;
                break;

            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    horizon.error(line, "Unexpected character");
                }
                break;
        }
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
}
