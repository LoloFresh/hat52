package com.example.the_hat.backendClasses.games

import com.example.the_hat.backendClasses.Player
import com.example.the_hat.backendClasses.Round
import com.example.the_hat.backendClasses.dataClasses.DataRound
import com.example.the_hat.backendClasses.dataClasses.PlayedRound
import com.example.the_hat.backendClasses.dicts.Dict
import com.example.the_hat.backendClasses.dicts.TypeWord
import com.example.the_hat.backendClasses.dicts.Word

abstract class Game(var players: MutableList<Player>, var dict: Dict<Word>) {
    var i: Int = -1
    var j: Int = -1
    var ind: Int = -1
    val data: MutableList<PlayedRound> = ArrayList()
    var cur: Round? = null

    init {
        initInd()
    }


    abstract fun initInd()

    abstract fun nextInd()

    fun nextRound() {
        if (ind != -1) {
            data[ind] = PlayedRound(data[ind].p1, data[ind].p2, cur!!.result())
            ind = -1
            cur = Round(dict, players[i], players[j])
            return
        }
        if (cur != null) {
            data.add(PlayedRound(i, j, cur!!.result()))
        }
        nextInd()
        cur = Round(dict, players[i], players[j])
    }

    fun nextRound(nw: DataRound) {
        if (ind != -1) {
            data[ind] = PlayedRound(data[ind].p1, data[ind].p2, nw)
            ind = -1
            cur = Round(dict, players[i], players[j])
            return
        }
        data.add(PlayedRound(i, j, nw))
        nextInd()
        cur = Round(dict, players[i], players[j])
    }

    fun nextWord(): Word {
        return cur!!.next()
    }

    fun doneWord() {
        cur!!.doneCur()
    }

    val currentWord: Word
        get() = cur!!.currentWord!!

    fun skipWord(): Boolean {
        return cur!!.skip()
    }

    fun failWord() {
        cur!!.fail()
    }

    fun failSkipWord() {
        cur!!.failSkipWord()
    }

    fun doneSkipWord(): Boolean {
        return cur!!.doneSkip()
    }

    fun hasSkipWord(): Boolean {
        return cur!!.hasSkip()
    }

    val skipWord: String
        get() = cur!!.skip.value

    val playerNames: String
        get() = players[i].name + " -> " + players[j].name

    val currentWords: DataRound
        get() = cur!!.result()

    fun countOfWords(i: Int, j: Int): Int {
        return data
            .filter { it.p1 == i && it.p2 == j }
            .sumOf { pr ->
                pr.words.types.count { it == TypeWord.DONE }
            }
    }

    fun countOfRounds(i: Int, j: Int): Int {
        return data.count { pr -> pr.p1 == i && pr.p2 == j }
    }

    fun countOfRounds(): List<List<Int>> {
        return players.indices.map { i ->
            players.indices.map { j ->
                countOfRounds(i, j)
            }
        }
    }

    fun countOfWords(): List<List<Int>> {
        return players.indices.map { i ->
            players.indices.map { j -> countOfWords(i, j) }
        }
    }

    fun replay(ind: Int) {
        cur = Round(dict, players[data[ind].p1], players[data[ind].p2])
        this.ind = ind
        cur!!.next()
    }

    fun replayed(ind: Int, rnd: DataRound) {
        data[ind] = PlayedRound(data[ind].p1, data[ind].p2, rnd)
    }

    fun setBackRound() {
        cur = Round(dict, players[i], players[j])
    }

    fun setWordType(indexRound: Int, indexWord: Int, newType: Boolean) {
        if (newType) {
            data[indexRound].words.types[indexWord] = TypeWord.DONE
        } else {
            data[indexRound].words.types[indexWord] = TypeWord.SKIP
        }
    }


    override fun toString(): String {
        return "Game{" +
                "players=" + players +
                ", i=" + i +
                ", j=" + j +
                ", dict=" + dict +
                ", dt=" + data +
                ", cur=" + cur +
                '}'
    }
}