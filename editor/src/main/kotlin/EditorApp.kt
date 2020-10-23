package com.statelesscoder.klisp.editor
import tornadofx.App
import tornadofx.FX
import tornadofx.launch
import tornadofx.setInScope


class EditorApp : App(EditorView::class, EditorStyles::class, FX.defaultScope)


