package com.example.the_hat.backendClasses.dicts

import android.os.Build
import androidx.annotation.RequiresApi

class RandomList<E> {
    var list: MutableList<E> = ArrayList()
    var index = 0

    constructor(list: MutableList<E>) {
        this.list = list
        list.shuffle()
    }

    fun get(): E {
        return list[index]
    }

    fun size(): Int {
        return list.size
    }


    // ATTENTION Use this function only before using get
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun add(arrayWord: RandomList<E>) {
        for (i in 0 until arrayWord.size()) {
            list.add(arrayWord.list.get(i))
        }
        list.shuffle()
    }

    fun take(): E {
        val cur = list[index]
        index++
        return cur
    }

    override fun toString(): String {
        return "RandomList{" +
                "list=" + list +
                '}'
    }
}
