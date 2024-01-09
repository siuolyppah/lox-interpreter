import arrow.core.None
import arrow.core.Option
import arrow.core.Some

class Scanner(private val source: String) {

    private val tokens: MutableList<Token> = ArrayList()

    //> scan-state
    private var current = 0 // not read yet, but will be read next
    private var start = 0   // beginning of the lexeme being scanned now
    private var line = 1    // line number of the lexeme being scanned now

    companion object {
        private val keywords: Map<String, TokenType> = mapOf(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "for" to TokenType.FOR,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType.RETURN,
            "super" to TokenType.SUPER,
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
        )
    }

    // if we consumed all the characters in the source
    private val isAtEnd: Boolean
        get() = current >= source.length

    fun scanTokens(): List<Token> {
        while (!isAtEnd) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", None, line))

        return tokens
    }

    private fun scanToken() {
        when (val readChar = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)

            //> two-char-tokens
            '!' -> addToken(
                if (match('=')) TokenType.BANG_EQUAL
                else TokenType.BANG
            )

            '=' -> addToken(
                if (match('=')) TokenType.EQUAL_EQUAL
                else TokenType.EQUAL
            )

            '<' -> addToken(
                if (match('=')) TokenType.LESS_EQUAL
                else TokenType.LESS
            )

            '>' -> addToken(
                if (match('=')) TokenType.GREATER_EQUAL
                else TokenType.GREATER
            )

            //> comment or slash
            '/' -> if (match('/')) {
                // a comment goes until the end of the line
                while (peek() != '\n' && !isAtEnd) advance()
            } else {
                addToken(TokenType.SLASH)
            }
            //< comment or slash
            //< two-char-tokens

            //> whitespace
            ' ', '\r', '\t' -> {
                // ignore whitespace
            }

            '\n' -> ++line
            //< whitespace

            //> string-starting quote
            '"' -> string()
            //< string-starting quote

            else -> {
                if (isDigit(readChar)) {
                    number()
                } else if (isAlpha(readChar)) {
                    identifier()
                } else {
                    Lox.error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun addToken(type: TokenType, literal: Option<Any> = None) {
        val lexeme = source.substring(start, current)
        tokens.add(Token(type, lexeme, literal, line))
    }

    private fun advance(): Char {
        return source[current++]
    }

    // if the current character matches the expected character, consume it and return true
    // like a conditional advance()
    private fun match(expected: Char): Boolean {
        if (isAtEnd) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd) return '\u0000'
        return source[current]
    }

    // look at the next 2 characters without consuming them
    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd) {
            if (peek() == '\n') ++line
            advance()
        }

        if (isAtEnd) {
            Lox.error(line, "Unterminated string.")
            return
        }

        // the closing char `"`
        advance()

        // trim the surrounding quotes
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, Some(value))
    }

    // kotlin has a built-in isDigit function, but it will allow non-ASCII digits
    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun number() {
        while (isDigit(peek())) advance()

        // look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the "."
            advance()
            while (isDigit(peek())) advance()
        }

        addToken(TokenType.NUMBER, Some(source.substring(start, current).toDouble()))
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }

    private fun isAlphaOrDigit(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    // any lexeme starting with a letter or underscore is an identifier,
    // this fun should be called only when we know that the current character is a letter or underscore
    private fun identifier() {
        while (isAlphaOrDigit(peek())) advance()

        // see if the identifier is a reserved keyword
        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER

        addToken(type)
    }
}