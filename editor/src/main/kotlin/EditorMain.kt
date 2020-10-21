package com.statlesscoder.klisp.editor
import tornadofx.App
import tornadofx.FX
import tornadofx.launch
import tornadofx.setInScope


fun main(args: Array<String>) {
    val tokenSource: TokenSource = TokenAgent()
    setInScope(EditorController(tokenSource), FX.defaultScope, EditorController::class)
    launch<EditorApp>(args)
}

class EditorApp : App(EditorView::class, EditorStyles::class, FX.defaultScope)



