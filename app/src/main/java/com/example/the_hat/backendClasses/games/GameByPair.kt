package com.example.the_hat.backendClasses.games

import com.example.the_hat.backendClasses.Player
import com.example.the_hat.backendClasses.dicts.Dict
import com.example.the_hat.backendClasses.dicts.Word

class GameByPair(players: MutableList<Player>, dict: Dict<Word>) :
    Game(players, dict) {
    override fun nextInd() {
        super.i += 2
        if (super.i == super.players.size) {
            super.i = 1
        }
        if (super.i == super.players.size + 1) {
            super.i = 0
        }
        super.j += 2
        if (super.j == super.players.size) {
            super.j = 1
        }
        if (super.j == super.players.size + 1) {
            super.j = 0
        }
    }

    override fun initInd() {
        super.i = -2
        super.j = -1
    }
}
