package com.example.the_hat.backendClasses.games

import com.example.the_hat.backendClasses.Player
import com.example.the_hat.backendClasses.dicts.Dict
import com.example.the_hat.backendClasses.dicts.Word

class GameEveryoneWithEveryone(players: MutableList<Player>, dict: Dict<Word>) :
    Game(players, dict) {
    override fun initInd() {
        super.i = -1
        super.j = 0
    }

    override fun nextInd() {
        super.i++
        super.j++
        super.j %= super.players.size
        if (super.i >= super.players.size) {
            super.i = 0
            super.j++
            super.j %= super.players.size
            if (super.i == super.j) {
                super.j++
                super.j %= super.players.size
            }
        }
    }
}
