package com.example.the_hat.backendClasses.dicts

import android.os.Build
import androidx.annotation.RequiresApi

class Dict<E> {
    var unused: RandomList<E>
    val used: MutableList<E> = ArrayList()

    constructor(unused: MutableList<E>) {
        this.unused = RandomList(unused)
    }

    constructor(unused: RandomList<E>) {
        this.unused = unused
    }

    fun take(): E {
        val el = unused.take()
        used.add(el)
        return el
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun paste(dict: Dict<E>) {
        unused.add(dict.unused)
    }

    override fun toString(): String {
        return "Dict{" +
                "unused=" + unused +
                ", used=" + used +
                '}'
    }
}
