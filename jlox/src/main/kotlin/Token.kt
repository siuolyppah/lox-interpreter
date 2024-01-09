import arrow.core.Option


class Token
    (
    private val type: TokenType,
    private val lexeme: String,         // substring of the source code that the token represents
    private val literal: Option<Any>,   // literal value of the token
    private val line: Int
) {

    override fun toString(): String {
        return literal.fold(
            { "Token(type=$type, lexeme='$lexeme', literal=None, line=$line)" },
            { "Token(type=$type, lexeme='$lexeme', literal=$it, line=$line)" }
        )
    }
}