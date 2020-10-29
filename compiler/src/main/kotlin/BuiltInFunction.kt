package com.statelesscoder.klisp.compiler

import com.statelesscoder.klisp.compiler.exceptions.RuntimeException
import com.statelesscoder.klisp.compiler.types.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

sealed class BuiltInFunction : Function() {
    abstract val name: Symbol

    companion object {
        @JvmStatic
        fun getAllBuiltInFunctions(): List<BuiltInFunction> {
            // No program is complete without a *little* magic.
            // This will be used for registering all built-ins in new scopes.
            // This makes it easy to add new built-ins, as it is simply inheriting
            // from the BuiltInFunction object and then it will be reflected in all scopes.
            return getAllSealedSubclasses(BuiltInFunction::class)
                .filter { it.isFinal }
                .map { it.createInstance() }
                .map { it as BuiltInFunction }
        }

        @JvmStatic
        private fun getAllSealedSubclasses(clazz: KClass<*>): List<KClass<*>> {
            val subclasses = clazz.sealedSubclasses
            val restOfTheTree = subclasses.flatMap { getAllSealedSubclasses(it) }
            return subclasses + restOfTheTree
        }
    }
}

class CdrFunction : BuiltInFunction() {
    override val name: Symbol = Symbol("cdr")
    override fun run(executor: Executor, args: RealizedList, scope: Scope): KLValue {
        if (args.items.size != 1) {
            throw RuntimeException("CDR function expects 1 and only 1 list to be passed.")
        }

        val functionArgs = args.items.first()
        if (functionArgs is RealizedList && functionArgs.items.isNotEmpty()) {
            return RealizedList(functionArgs.items.drop(1))
        } else {
            throw RuntimeException("CDR does not support these args: $args")
        }
    }
}

class PrintFunction : BuiltInFunction() {
    override val name: Symbol = Symbol("print")
    override fun run(executor: Executor, args: RealizedList, scope: Scope): KLValue {
        if (!args.items.all { it is KLString }) {
            throw RuntimeException("Only strings are printable.")
        }

        val stringArgs = args.items.map { it as KLString }.map { it.text }
        val s = stringArgs.reduce() {acc, s -> "$acc $s" }
        print(s)
        return KLString(s)
    }
}

class CarFunction : BuiltInFunction() {
    override val name: Symbol = Symbol("car")
    override fun run(executor: Executor, args: RealizedList, scope: Scope): KLValue {
        if (args.items.size != 1) {
            throw RuntimeException("CAR function expects 1 and only 1 list to be passed.")
        }

        val functionArgs = args.items.first()
        if (functionArgs is RealizedList && functionArgs.items.isNotEmpty()) {
            return functionArgs.items[0]
        } else {
            throw RuntimeException("CAR does not support these args: $args")
        }
    }
}

class ConsFunction : BuiltInFunction() {
    override val name: Symbol = Symbol("cons")
    override fun run(executor: Executor, args: RealizedList, scope: Scope): KLValue {
        if (args.items.size != 2) {
            throw RuntimeException("CONS function expects 1 list and 1 data object.")
        }
        val list = args.items[0]
        val value = args.items[1]
        if (list is RealizedList) {
            return RealizedList(list.items + listOf(value))
        } else {
            throw RuntimeException("CONS function expects list as first argument.")
        }
    }
}

sealed class ArithmeticFunctions : BuiltInFunction() {
    internal abstract fun runOperation(nArgs: List<Float>): Float
    override fun run(executor: Executor, args: RealizedList, scope: Scope): KLValue {
        if (!args.items.all { it is KLNumber }) {
            throw RuntimeException("Only numeric types are compatible with *, +, /, and -.")
        }
        val nArgs = args.items.map { (it as KLNumber).value }
        return KLNumber(runOperation(nArgs))
    }
}

class AddFunctions() : ArithmeticFunctions() {
    override val name: Symbol = Symbol("+")
    override fun runOperation(nArgs: List<Float>): Float {
        return nArgs.reduce { acc, fl -> acc + fl }
    }
}

class SubFunctions() : ArithmeticFunctions() {
    override val name: Symbol = Symbol("-")
    override fun runOperation(nArgs: List<Float>): Float {
        return nArgs.reduce { acc, fl -> acc - fl }
    }
}

class MulFunctions() : ArithmeticFunctions() {
    override val name: Symbol = Symbol("*")
    override fun runOperation(nArgs: List<Float>): Float {
        return nArgs.reduce { acc, fl -> acc * fl }
    }
}

class DivFunctions() : ArithmeticFunctions() {
    override val name: Symbol = Symbol("/")
    override fun runOperation(nArgs: List<Float>): Float {
        return nArgs.reduce { acc, fl -> acc / fl }
    }
}

sealed class BooleanFunction : BuiltInFunction() {
    internal abstract fun runOperation(nArgs: List<Boolean>): Boolean
    override fun run(executor: Executor, args: RealizedList, scope: Scope): KLBool {
        if (!args.items.all { it is KLBool }) {
            throw RuntimeException("Only boolean types are compatible with and, or, and not.")
        }
        val bArgs = args.items.map { (it as KLBool).truth }
        return KLBool(runOperation(bArgs))
    }
}

class AndFunction() : BooleanFunction() {
    override val name: Symbol = Symbol("and")
    override fun runOperation(nArgs: List<Boolean>): Boolean {
        return nArgs.reduce { acc, fl -> acc && fl }
    }
}

class OrFunction() : BooleanFunction() {
    override val name: Symbol = Symbol("or")
    override fun runOperation(nArgs: List<Boolean>): Boolean {
        return nArgs.reduce { acc, fl -> acc || fl }
    }
}

class NotFunction() : BooleanFunction() {
    override val name: Symbol = Symbol("not")
    override fun runOperation(nArgs: List<Boolean>): Boolean {
        if (nArgs.size != 1) {
            throw RuntimeException("NOT only accepts one argument.")
        }
        return !nArgs.first()
    }
}

sealed class EqualityFunction : BuiltInFunction() {
    internal abstract fun runOperation(a: KLValue, b: KLValue): Boolean
    override fun run(executor: Executor, args: RealizedList, scope: Scope): KLBool {
        if (args.items.size != 2) {
            throw RuntimeException("EQ & NEQ only accept 2 arguments of the same type.")
        }
        return KLBool(runOperation(args.items[0], args.items[1]))
    }
}

class EqFunction() : EqualityFunction() {
    override val name: Symbol = Symbol("eq")
    override fun runOperation(a: KLValue, b: KLValue): Boolean {
        return a == b
    }
}

class NeqFunction() : EqualityFunction() {
    override val name: Symbol = Symbol("neq")
    override fun runOperation(a: KLValue, b: KLValue): Boolean {
        return a != b
    }
}
