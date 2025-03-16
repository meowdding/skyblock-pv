package tech.thatgravyboat.skyblockpv.utils.expressions

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

object Expressions {

    fun parse(json: JsonElement): Expression = when (json) {
        is JsonArray -> {
            val elements = json.asJsonArray
            when (elements.size()) {
                1 -> parse(elements[0])
                2 -> {
                    require(elements[0].asString == "-") { "Expected '-' operator, got ${elements[0]}" }
                    TransformExpression(parse(elements[1])) { -it }
                }
                3 -> OpExpression(parse(elements[0]), parse(elements[2]), elements[1].asString[0])
                else -> throw IllegalArgumentException("Expected 1, 2 or 3 elements, got ${elements.size()}")
            }
        }
        is JsonObject -> when (json["function"].asString) {
            "floor" -> TransformExpression(parse(json["value"])) { floor(it) }
            "ceil" -> TransformExpression(parse(json["value"])) { ceil(it) }
            else -> throw IllegalArgumentException("Unsupported function: ${json["function"]}")
        }
        is JsonPrimitive -> {
            when {
                json.isNumber -> ConstExpression(json.asDouble)
                json.isString -> VarExpression(json.asString)
                else -> throw IllegalArgumentException("Unsupported primitive: $json")
            }
        }
        else -> throw IllegalArgumentException("Unsupported element: $json")
    }
}

data class OpExpression(val left: Expression, val right: Expression, val op: Char) : Expression {
    override fun evaluate(context: ExpressionContext): Double {
        return when (op) {
            '+' -> left.evaluate(context) + right.evaluate(context)
            '-' -> left.evaluate(context) - right.evaluate(context)
            '*' -> left.evaluate(context) * right.evaluate(context)
            '/' -> left.evaluate(context) / right.evaluate(context)
            '%' -> left.evaluate(context) % right.evaluate(context)
            '^' -> left.evaluate(context).pow(right.evaluate(context))
            else -> throw IllegalArgumentException("Unsupported operator: $op")
        }
    }
}

data class TransformExpression(val expression: Expression, val transform: (Double) -> Double) : Expression {
    override fun evaluate(context: ExpressionContext): Double {
        return transform(expression.evaluate(context))
    }
}

data class VarExpression(val name: String) : Expression {
    override fun evaluate(context: ExpressionContext): Double {
        return context.getVariable(name)
    }
}

data class ConstExpression(val value: Double) : Expression {
    override fun evaluate(context: ExpressionContext): Double {
        return value
    }
}
