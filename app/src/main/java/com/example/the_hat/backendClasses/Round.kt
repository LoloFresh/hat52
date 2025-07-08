package com.example.the_hat.backendClasses

import com.example.the_hat.backendClasses.dataClasses.DataRound
import com.example.the_hat.backendClasses.dicts.Dict
import com.example.the_hat.backendClasses.dicts.TypeWord
import com.example.the_hat.backendClasses.dicts.Word

class Round(
    val dict: Dict<Word>,
    val explainer: Player,
    val guesser: Player
) {
    var skipped_id = -1
    val wordsInRound: MutableList<Word> = ArrayList()
    val typeOfWordsInRound: MutableList<TypeWord> = ArrayList()
    var currentWord: Word? = null

    fun next(): Word {
        val word = dict.take()
        currentWord = word
        wordsInRound.add(currentWord!!)
        typeOfWordsInRound.add(TypeWord.SKIP)
        return word
    }

    fun doneCur() {
        if (currentWord == null) {
            throw AssertionError("No current word found")
        }
        typeOfWordsInRound[typeOfWordsInRound.lastIndex] = TypeWord.DONE
        currentWord = null
    }

    fun doneSkip(): Boolean {
        if (skipped_id == -1) {
            return false
        }
        typeOfWordsInRound[skipped_id] = TypeWord.DONE
        skipped_id = -1
        return true
    }

    fun hasSkip(): Boolean {
        return skipped_id != -1
    }

    val skip: Word
        get() = wordsInRound[skipped_id]

    fun skip(): Boolean {
        if (currentWord == null) {
            throw AssertionError("No current word found")
        }
        if (skipped_id != -1) return false
        skipped_id = wordsInRound.lastIndex
        currentWord = null
        return true
    }

    fun fail() {
        if (currentWord == null) {
            throw AssertionError("No current word found")
        }
        currentWord = null
    }

    fun failSkipWord() {
        if (skipped_id == -1) {
            throw AssertionError("No skipped word")
        }
        skipped_id = -1
    }

    fun result(): DataRound {
        return DataRound(wordsInRound, typeOfWordsInRound)
    }

    override fun toString(): String {
        return "Round{" +
                "dict=" + dict +
                ", skipped_id=" + skipped_id +
                ", explainer=" + explainer +
                ", guesser=" + guesser +
                ", wordsInRound=" + wordsInRound +
                ", currentWord=" + currentWord +
                '}'
    }
}
