package tech.thatgravyboat.skyblockpv.utils.expressions

interface Expression {

    fun evaluate(context: ExpressionContext): Double

    fun evaluate(variables: Map<String, Double>): Double = evaluate(ExpressionContext(variables))
}

class ExpressionContext(private val variables: Map<String, Double>) {

    fun getVariable(name: String): Double = variables[name] ?: error("Variable not found: $name")
}

