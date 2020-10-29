# Entrypoint
## Main.kt
This is the first registered entrypoint in the gradle.build file. It is just a static main body which creates the
[TokenAgent](#TokenAgent), injects it into a newly registered [EditorController](#EditorController), and starts the
[EditorApp](#EditorApp).
## EditorApp
Small class linking the [EditorView](#EditorView) with the [EditorStyles](#EditorStyles). It also overrides stop due to
a glitch in TornadoFX not terminating the process on closing the final window.
# Styles
## EditorStyles
This is a typed style sheet. The only thing of note here is that this style sheet is if a new Token is added to the 
language and you want to color it, just make the name lowercase and put "class_" in front of it. For instance,
`TokenType.RIGHT_BRACKET` becomes `class_right_bracket`. This is automatically cause tokens of that type to take the 
desired styling.

You can find the logic that does in this in the [CodeAreaView](#CodeAreaView)'s `computeHighlighting` function.

# Views and Fragments
## CodeAreaView
This is the view where you type code. It also powers syntax highlighting and line numbers. The main component, the
`CodeArea`, is an open source JavaFX component: [RichTextFX](https://github.com/FXMisc/RichTextFX). Out of the box it
comes with editor lines, a way to debounce edits, and an easy system for styling.

Five hundred milliseconds after you finish typing, the controller gets the tokens of the text in the `CodeArea` from 
the language server. These tokens come with the type, text, and position of the token. Using that, a list of
`StyleSpans` is calculated with the appropriate class calculated from the token type
(see [#EditorStyles](#EditorStyles) for more information on styling).

## EditorView
This is the main window which the [CodeAreaView](#CodeAreaView), [ScopeInspectorView](#ScopeInspectorView), and 
[ResultsView](#ResultsView) sit on.

## ErrorFragment
The error fragment is a pop-up model box launched anytime there is an error. Unfortunately, the UI when there is an
error is quite bad and needs investment. You can see that an error occurred, but that is the limit of useful information.

## ResultsView
The results view shows the value of each line of code that was executed. Both this and the
[ScopeInspectorView](#ScopeInspectorView) are backed by the SimpleResult data items returned by the language server's
`/execute` route.
## ScopeInspectorView
The scope inspector view shows the state of the Scope after the last executed expression. This is primarily to debug
 function definition. Both this and the
[ResultsView](#ResultsView) are backed by the SimpleResult data items returned by the language server's
`/execute` route.
# Controllers
## EditorController
This controller serves to warp the calls to the [TokenAgent](#TokenAgent) so that all Views remain unit testable (though
they currently are not). It also binds new data and triggers updates in the [ResultsView](#ResultsView) and the
[ScopeInspectorView](#ScopeInspectorView).

## TokenAgent
This uses the [fuel library](https://github.com/kittinunf/fuel) to launch requests against the language server and
decode the results.
