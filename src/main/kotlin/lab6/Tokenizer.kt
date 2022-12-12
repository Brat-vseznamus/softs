package lab6

import java.util.ArrayDeque
import java.util.Deque

interface Tokenizer {
    fun split(): List<Token>
}

class MathExpressionTokenizer(private val input: String) : Tokenizer {
    override fun split(): List<Token> {
        val result = mutableListOf<Token>()
        val deque = ArrayDeque<Char>()
        var curState: State = State.Start

        for (c in (input + "\n")) {
            var repeat = true
            while (repeat) {
                val (token, part2) = curState.move(c, deque)
                val (toContinue, newState) = part2
                if (token != null) {
                    result.add(token)
                }
                curState = newState
                repeat = !toContinue
            }
        }

        return result
    }
}

enum class State(val move: (c: Char, stack: Deque<Char>) -> Pair<Token?, Pair<Boolean, State>>) {
    Start(f@{ c, _ ->
        var curToken: Token? = null
        if (!c.isWhitespace()) {
            if (c.isDigit()) {
                // repeat handling
                return@f (null to (false to Number))
            }
            curToken = when (c) {
                '(' -> Brace.Left
                ')' -> Brace.Right
                '+' -> Operation.ADD
                '-' -> Operation.SUB
                '*' -> Operation.MUL
                '/' -> Operation.DIV
                else -> null
            }
            if (curToken == null) {
                return@f null to (true to Error)
            }
        }
        return@f curToken to (true to Start)
    }),
    Number(f@{ c, stack ->
        if (c.isDigit()) {
            stack.addLast(c)
            return@f null to (true to Number)
        } else {
            var number = 0
            while (!stack.isEmpty()) {
                number = number * 10 + (stack.pollFirst().code - '0'.code)
            }
            // repeat handling
            return@f NumberToken(number.toDouble()) to (false to Start)
        }
    }),
    End(f@{ c, stack ->
        stack.addLast(c)
        return@f null to (true to End)
    }),
    Error(f@{ c, stack ->
        stack.addLast(c)
        return@f null to (true to Error)
    });
}