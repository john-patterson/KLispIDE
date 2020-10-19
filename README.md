# Overview
KLisp is a simple Lisp-like language. The point of this language is for me to learn Kotlin, Gradle, IntelliJ IDEA, and 
show off some of my skills. One of my favorite projects for learning, since I love programming languages, is a language.
For example, here's [huskel](https://github.com/john-patterson/huskel), [lispjs](https://github.com/john-patterson/lispjs),
and [passcal](https://github.com/john-patterson/passcal).

I suggest you also read [LEARNINGS.md](LEARNINGS.md) to see the issues I ran into, how I pivoted, and what I picked up
fom the experience. This is, after all, the point of this code.

## Language Features

### Data Types

* __booleans__: This is true and false, represented by the reserved keywords `true` and `false`.
* __numbers__: Zero, negatives, floating-point, will all be represented using the usual syntax. There is no
differentiation between these types.
* __strings__: Strings start and end with " and cannot contain a double quote.
* __lists__: Lists can only be created using the _quote_ function, which returns the arguments as a list.
* __expressions__: Everything else. Expressions start with a symbol which evaluates to a function. The rest of the
body is passed as arguments to that function at interpretation time.
   - Example: `(function 1 2 3)`

### Special Language Constructs

* __let__: Let-binding is used to bind values to symbols within the scope of the body.
  - Example: `(let (a 1 b 2) (+ a b))`
* __fun__: Defines a function with the first argument being the list of arguments to that function, and the second argument being the return of the function.
  - Example: `(fun (a b) (+ a b))`
  - Note: All functions are anonymous. Use let-binding to assign them a symbol.
* __if__: If expected 3 values: (1) a boolean expression, (2) a value to return if (1) is true, and (3) a value to return if (1) is false.
 
 ### Predefined-Functions
 These are all functions which are built into the language runtime and cannot be redefined.
 * __+__: Binary addition
   - Example: `(+ 1 2) => 3` 
 * __-__: Binary subtraction
   - Example: `(- 1 2) => -1` 
 * __/__: Binary division
   - Example: `(/ 1 2) => 0.5` 
 * __*__: Binary multiplication
   - Example: `(* 1 2) => 2` 
 * __print__: Writes the value given to the console output.
   - Example: `(print "foo") => foo`


## Runtime Features
For reasons known to myself and those I send this code to, the compiler will be exposed by an API to network traffic.
There will also be a test service host which is similarly exposed by an API to the network.