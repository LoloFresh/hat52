package com.example.the_hat.backendClasses.dataClasses

import com.example.the_hat.backendClasses.dicts.TypeWord
import com.example.the_hat.backendClasses.dicts.Word

data class DataRound(val words: MutableList<Word>, val types: MutableList<TypeWord>)
