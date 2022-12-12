package lab6

interface Token {
    fun accept(visitor: TokenVisitor)
}

enum class Brace(val symbol: String): Token {
    Left("("),
    Right(")");

    override fun accept(visitor: TokenVisitor) = visitor.visit(this)
    override fun toString() = symbol

    companion object {
        fun getBrace(symbol: String): Operation? {
            return Operation.values().find {
                it.symbol == symbol
            }
        }
    }
}


class NumberToken(val value: Double): Token {
    override fun accept(visitor: TokenVisitor) = visitor.visit(this)
    override fun toString() = value.toString()
}

enum class Operation(
    val calculator: (Double, Double) -> Double,
    val symbol: String,
    val priority: Int): Token {
    ADD( { a: Double, b: Double -> a + b }, "+", 1),
    SUB( { a: Double, b: Double -> a - b }, "-", 1),
    MUL( { a: Double, b: Double -> a * b }, "*", 2),
    DIV( { a: Double, b: Double -> a / b }, "/", 2);

    override fun accept(visitor: TokenVisitor) = visitor.visit(this)
    override fun toString() = symbol

    fun calculate(a: Double, b: Double) = calculator(a, b)

    companion object {
        fun getOperation(symbol: String): Operation? {
            return Operation.values().find {
                it.symbol == symbol
            }
        }
    }
}