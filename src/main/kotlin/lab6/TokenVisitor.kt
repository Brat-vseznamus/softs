package lab6

interface TokenVisitor {
    fun visit(token: Brace)
    fun visit(token: NumberToken)
    fun visit(token: Operation)
}

class PrintVisitor: TokenVisitor {
    override fun visit(token: NumberToken)  = print("$token ")
    override fun visit(token: Brace)        = print("$token ")
    override fun visit(token: Operation)    = print("$token ")
}

class CalcVisitor: TokenVisitor {
    private val stack = ArrayDeque<Double>()

    override fun visit(token: NumberToken) = stack.addFirst(token.value)
    override fun visit(token: Brace) = throw IllegalArgumentException("Invalid position of brace \"$token\"")
    override fun visit(token: Operation) {
        if (stack.size < 2) {
            throw RuntimeException("Invalid position of operation \"$token\"")
        }
        val rhs = stack.removeFirst()
        val lhs = stack.removeFirst()
        stack.addFirst(token.calculate(lhs, rhs))
    }

    fun compute(): Double {
        if (stack.size != 1) {
            throw RuntimeException("Invalid state of stack (size(stack) = ${stack.size} != 1)")
        }
        return stack.removeFirst()
    }
}

class ParserVisitor: TokenVisitor {
    private val tokens = mutableListOf<Token>()
    private val stack  = ArrayDeque<Token>()

    override fun visit(token: NumberToken) {
        tokens.add(token)
    }

    override fun visit(token: Brace) {
        if (token == Brace.Left) {
            stack.addFirst(token)
        } else {
            tokens.addAll(stack.splitWhileTrue { it !is Brace })
            stack.removeFirstOrNull() ?: throw RuntimeException("Invalid expression")
        }
    }

    override fun visit(token: Operation) {
        tokens.addAll(stack.splitWhileTrue { it is Operation && it.priority >= token.priority })
        stack.addFirst(token)
    }

    fun getPostfixExpression(): List<Token> {
        tokens.addAll(stack)
        stack.clear()
        return tokens
    }
}


private fun <T> ArrayDeque<T>.splitWhileTrue(predicate: (T) -> Boolean): List<T> {
    val result = takeWhile(predicate)
    repeat(result.size) {
        removeFirst()
    }
    return result
}