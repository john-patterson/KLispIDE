# Stages of Compilation
This compiler is broken into three phases:
* _Tokenization_: This is where text to converted from a series of chars to a the basic units of the language that we
care to recognize. You can see the mapping in the [TokenType](#TokenType) section.
* _Parsing_: The parser relates tokens to each other to form the structure of the language. In this case, the parser
is in charge of recognizing basic data like strings, numbers, and lists and it is in charge of structuring the more
complex [Expression](#Expression) form.
* _Execution_: This is where the language is interpreted and data is returned.

# Tokenizer Structure
The structure is basically one big `while` loop. All Tokens are separated by whitespace or parentheses. Each iteration
of the loop should fine one Token. In the case of strings and identifiers, the `pos` marker is advanced until the string
end or the next whitespace is found, respectively. 

## Tokens
A Token is simply a container for the [TokenType](#TokenType), the text of the token recognized, and the position at 
which the token was found.

## TokenType
These are the accepted tokens:
* `(` becomes `TokenType.LEFT_PARENS`
* `)` becomes `TokenType.RIGHT_PARENS`
* `[` becomes `TokenType.LEFT_BRACKET`
* `]` becomes `TokenType.RIGHT_BRACKET`
* `1`, `-1`, `-1.0`, and so on becomes `TokenType.NUMERIC`
* `true` and `false` becomes `TokenType.BOOLEAN`
* `if` becomes `TokenType.IF`
* `let` becomes `TokenType.LET`
* `fun` becomes `TokenType.FUN_SIDE_EFFECT`
* `"<anything>"` becomes `TokenType.STRING`
* anything else becomes `TokenType.IDENTIFIER`

# Parser Structure
This has a similar structure to the Tokenizer at first, only we are reading Tokens, not chars. The interesting part to
talk about here is the Expression parsing (`parseSingleExpression`) which forms the most basic structure of the
language.

First, the head is identified. It must be either an identifier or a keyword. All expressions in KLisp are function
invocation of the form `(<FunctionIdentifier> [...args])`. For special forms, such as `if`, `let`, and `fun`, the 
structure of the expression is validated here.

In the future, each of these special forms should be moved into classes that know how to validate and execute themselves.
This is tracked in [issue #7](https://github.com/john-patterson/KLispIDE/issues/7).

At the end of `parse`, `enrichExpression` is where a lot of the magic happens. This is where the special forms
[IfExpression](#IfExpression), [LetBinding](#LetBinding), [FunctionDefinition](#FunctionDefinition).

## ExpressionPart
ExpressionParts are the constituent members of an Expression.

They are:
* Symbol
* Expression
* Keyword
* UnrealizedList
* [KLValue](#KLValue)

## Expression
Expressions have a head and a tail. The head is the first ExpressionPart in the form, tail is all others. Expressions also have an `execute` method. For the base class, this is a simple call back into the Executor.

The more interesting cases are the sub-classes.

## Built-in Special Forms
These are all the Keyword forms.
* IfExpression
* FunctionDefinition
* LetBinding

## KLValue
These are the realized, final data types:
* KLLiteralValue
  - KLBool
  - KLNumber
  - KLString
* Function - _These classes contain a `run` function which binds the symbols in the params part of the form to the passed
in arguments in a new scope. The new scope, along with the body of the Function, is then passed back into the executor._
  - BuiltInFunction
      - PrintFunction
      - CarFunction
      - CdrFunction
      - ConsFunction
      - ArithmeticFunctions
        * AddFunctions
        * SubFunctions
        * MulFunctions
        * DivFunctions
      - BooleanFunction
        * AndFunction
        * OrFunction
        * NotFunction
      - EqualityFunction 
        * EqFunction
        * NeqFunction
  - UserDefinedFunction
* RealizedList

BuiltInFunction children are registered in all new [Scope](#Scope) objects in a special manner. The static method 
[getAllBuiltInFunctions](https://github.com/john-patterson/KLispIDE/blob/master/compiler/src/main/kotlin/BuiltInFunction.kt#L14)
uses reflection to load all `final` children in the object hierarchy of BuiltInFunction. It then creates an instances 
of each of these and adds an entry to the scope with the defined name.

This means, to add a new built-in function, you just have to subclass BuiltInFunction, supply the function name in the
`name` property of your implementation, and everything just works.

# Executor Structure
The Executor takes an Expression or an ExpressionPart. If it is an ExpressionPart, the appropriate execution method,
defined that ExpressionPart, is called.

If it is an `Expression`, then the head of the `Expression` is executed. Next each element in the tail is executed. Finally,
the head is asserted to be a `Function` and the run method is called. As covered in the [KLValue](#KLValue) section,
this run method is implemented by the individual `Function` child.
## Scope
The Scope object is a symbol table of Symbol names to KValues. All new Scopes are populated with built-ins, as explained in
the [KLValue](#KLValue) section explains.

New scopes are created in IfBindings and in Function execution, as well as at the root of execution.

## SimpleResult
This is a plain string version of the expression run, the result of the expression, and the symbol table. This is sent
back to the editor to print the in the ResultsView and the ScopeInspectorView.

