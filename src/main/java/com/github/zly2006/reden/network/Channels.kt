package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden

val TAG_BLOCK_POS = Reden.identifier("tag_block_pos")
fun register() {
    Hello.register()
    TagBlockPos.register()
    Undo.register()
}
