import lab6.*


fun main() {
    val input = readlnOrNull() ?: return

    // get tokens
    var tokens = MathExpressionTokenizer(input).split()

    println("Tokenizer result for original (infix) expression: $tokens")

    // parse expression
    val parserVisitor = ParserVisitor()

    tokens.forEach { it.accept(parserVisitor) }
    tokens = parserVisitor.getPostfixExpression()

    println("Change to postfix expression: $tokens")

    // print expression
    val printVisitor = PrintVisitor()

    print("Print by PrintVisitor: ")
    tokens.forEach { it.accept(printVisitor) }
    println()

    // compute expression
    val calculationVisitor = CalcVisitor()

    tokens.forEach { it.accept(calculationVisitor) }

    println("Compute expression result: ${calculationVisitor.compute()}")
}