package com.example.the_hat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.DecimalFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.Orientation
import androidx.core.content.FileProvider
import com.example.the_hat.backendClasses.Player
import com.example.the_hat.backendClasses.dicts.Dict
import com.example.the_hat.backendClasses.dicts.RandomList
import com.example.the_hat.backendClasses.dicts.TypeWord.DONE
import com.example.the_hat.backendClasses.dicts.TypeWord.SKIP
import com.example.the_hat.backendClasses.dicts.Word
import com.example.the_hat.backendClasses.games.Game
import com.example.the_hat.backendClasses.games.GameByPair
import com.example.the_hat.backendClasses.games.GameEveryoneWithEveryone
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader


class MainActivity : ComponentActivity() {
    var players: MutableList<Player> = ArrayList()
    var configs: MutableMap<String, Int> = mutableMapOf(
        "teamMode" to 0,
        "swapPlayers" to -1,
        "time" to 30,
    )
    var dicts: MutableMap<String, Int> = mutableMapOf(
        "easy" to 0,
        "medium" to 0,
        "hard" to 0
    )
    var dict: Dict<Word> = Dict(mutableListOf())
    lateinit var game: Game


    private fun openDictFromAss(file: String): Dict<Word> {
        return Dict(
            assets.open(file).bufferedReader().readLines().map { Word(it) }
                .toMutableList())
    }

    var context = this

    fun copyMp3FromAssets(context: Context, assetFileName: String, targetFileName: String): File {
        val assetManager = context.assets
        val outFile = File(context.filesDir, targetFileName)

        if (!outFile.exists()) {
            assetManager.open(assetFileName).use { inputStream ->
                FileOutputStream(outFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        return outFile
    }
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        saveToFile(this, "easy_dict.txt", convertDictToText(openDictFromAss("dict_easy_sorted.txt")))
        saveToFile(this, "medium_dict.txt", convertDictToText(openDictFromAss("dict_mid_sorted.txt")))
        saveToFile(this, "hard_dict.txt", convertDictToText(openDictFromAss("dict_hard_sorted.txt")))
        saveToFile(this, "end_sound.mp3", copyMp3FromAssets(this, "end_sound.mp3", "end_sound.mp3"))
        saveToFile(this, "doneWord_sound.mp3", copyMp3FromAssets(this, "doneWord_sound.mp3", "doneWord_sound.mp3"))

        updateDicts()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.main)
        val but = findViewById<Button>(R.id.main_continue)

        if (dict.unused.size() != 0) {
            but.isEnabled = true
            but.visibility = View.VISIBLE
            Log.i("piska", "1")
        } else {
            but.isEnabled = false
            but.visibility = View.INVISIBLE
            Log.i("piska", "0")
        }

        Log.i("SwitchScreen", "To main")
    }

    fun goToMain(view: View) {
        setContentView(R.layout.main)
        val but = findViewById<Button>(R.id.main_continue)

        if (dict.unused.size() != 0) {
            but.isEnabled = true
            but.visibility = View.VISIBLE
            Log.i("piska", "1")
        } else {
            but.isEnabled = false
            but.visibility = View.INVISIBLE
            Log.i("piska", "0")
        }
    }

    fun goToApprove(view: View) {
        setContentView(R.layout.approve)
        Log.i("SwitchScreen", "To Aproov")
    }

    fun goToCreatePlayers(view: View) {
        setContentView(R.layout.create_players)
        var containerNames = findViewById<LinearLayout>(R.id.create_players_containerTexts)
        for (i in 0 until players.size) {
            addPlayer(view)
            var text: EditText =
                (containerNames.getChildAt(i) as LinearLayout).getChildAt(1) as EditText
            var name: Editable = Editable.Factory.getInstance().newEditable(players[i].name)
            text.text = name
        }
        Log.i("SwitchScreen", "To CreatePlayers")
    }

    @SuppressLint("MissingInflatedId", "ResourceType")
    fun goToSetting(view: View) {
        var containerNames = findViewById<LinearLayout>(R.id.create_players_containerTexts)
        players = ArrayList()
        for (i in 0 until containerNames.childCount) {
            var text: EditText =
                (containerNames.getChildAt(i) as LinearLayout).getChildAt(1) as EditText
            players.add(Player(text.text.toString()))
        }
        setContentView(R.layout.game_settings)
        var teamButton = findViewById<CheckBox>(R.id.game_settings_teamMode)
        teamButton.isChecked = configs["teamMode"] == 1
        containerNames.removeAllViews()

        var time = findViewById<TextView>(R.id.game_settings_time)
        time.text = configs["time"].toString()

        var ll = findViewById<LinearLayout>(R.id.game_setting_dict)

        ll.removeAllViews()

        for (key in dicts.keys) {
            var locLL = LinearLayout(this).apply {
                orientation = Orientation.Vertical.ordinal
                gravity = Gravity.CENTER
            }

            var name = TextView(this).apply {
                text = key
            }

            var but = CheckBox(this).apply {
                tag = key
                isChecked = dicts[key] == 1
            }

            but.setOnClickListener {
                dicts[but.tag.toString()] = if (but.isChecked) 1 else 0
            }
            locLL.addView(name)
            locLL.addView(but)
            ll.addView(locLL)
        }

        Log.i("SwitchScreen", "To Setting")
    }

    fun goToResults(view: View) {
        setContentView(R.layout.results)
        Log.i("SwitchScreen", "To Results")
        updateResultsWords(view)
    }

    fun goToGame(view: View) {
        setContentView(R.layout.game)
        updateGame(view)
    }

    fun goToHistory(view: View) {
        setContentView(R.layout.history)
        updateHistory(view)
    }

    fun goToReplay(view: View) {
        setContentView(R.layout.replay)
        updateReplay(view)
    }

    fun goToMenage(view: View) {
        setContentView(R.layout.menage_dict)
        var ll = findViewById<LinearLayout>(R.id.menage_dict_list)

        ll.removeAllViews()

        for (key in dicts.keys) {
            var locLL = LinearLayout(this).apply {
                orientation = Orientation.Vertical.ordinal
                gravity = Gravity.CENTER
            }

            var name = TextView(this).apply {
                text = key
                textSize = textSize * 5 / 4
            }

            var but = Button(this).apply {
                tag = key
                text = "X"
            }

            but.setOnClickListener {
                dicts.remove(key)
                deleteFile(this, key + "_dict.txt")
                goToMenage(view)
            }
            locLL.addView(name)
            locLL.addView(but)
            ll.addView(locLL)
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateReplay(view: View) {
        var text = findViewById<TextView>(R.id.replay_who_by_who)
        text.text = game.cur!!.explainer.name + " -> " + game.cur!!.guesser.name
    }

    @SuppressLint("SetTextI18n")
    private fun updateHistory(view: View) {
        var layout: LinearLayout = findViewById<LinearLayout>(R.id.history_scroll_ll)

        for (i in 0 until game.data.size) {
            var tempLL1 = LinearLayout(this)
            var textView = TextView(this).apply {
                text =
                    players[game.data[game.data.size - i - 1].p1].name + " ->" + players[game.data[game.data.size - i - 1].p2].name
                gravity = Gravity.CENTER
            }
            tempLL1.addView(textView)


            var tempLL2 = LinearLayout(this).apply {
                gravity = Gravity.RIGHT
            }
            var button = Button(this).apply {
                text = "Переиграть"
            }
            button.setOnClickListener {
                game.replay(game.data.size - i - 1)
                goToReplay(view)
            }
            tempLL2.addView(button)

            var tempLL3 = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }

            for (j in 0 until game.data[game.data.size - i - 1].words.words.size) {
                var ll3 = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                var word = TextView(this).apply {
                    text = game.data[game.data.size - i - 1].words.words[j].value
                }

                var check = CheckBox(this).apply {
                    id = 10000 * i + j
                    isChecked = game.data[game.data.size - i - 1].words.types[j] == DONE
                }

                check.setOnClickListener {
                    game.setWordType(
                        game.data.size - check.id / 10000 - 1,
                        check.id % 10000,
                        game.data[game.data.size - i - 1].words.types[j] == SKIP
                    )
                }

                ll3.addView(check)
                ll3.addView(word)

                tempLL3.addView(ll3)
            }


            layout.addView(tempLL1)
            layout.addView(tempLL2)
            layout.addView(tempLL3)
        }
    }

    fun updateResultsWords(view: View) {
        Log.d("Update", view.toString())
        val table: TableLayout = findViewById(R.id.results_all)
        table.removeAllViews()

        var row1: TableRow = TableRow(this)

        var text: TextView = TextView(this)
        text.text = ""
        row1.addView(text)

        for (i in 0 until players.size) {
            var text1: TextView = TextView(this)
            text1.text = players[i].name[0].toString()
            row1.addView(text1)
        }
        table.addView(row1)

        for (i in 0 until players.size) {
            var row2: TableRow = TableRow(this)
            var text1: TextView = TextView(this)
            text1.text = players[i].name[0].toString()
            row2.addView(text1)

            for (j in 0 until players.size) {
                var text2: TextView = TextView(this)
                if (i != j) {
                    text2.text = game.countOfWords(i, j).toString()
                }
                row2.addView(text2)
            }
            table.addView(row2)
        }
    }

    fun updateResultsTimes(view: View) {
        Log.d("Update", view.toString())
        val table: TableLayout = findViewById(R.id.results_all)
        table.removeAllViews()

        var row1: TableRow = TableRow(this)

        var text: TextView = TextView(this)
        text.text = ""
        row1.addView(text)

        for (i in 0 until players.size) {
            var text1: TextView = TextView(this)
            text1.text = players[i].name[0].toString()
            row1.addView(text1)
        }
        table.addView(row1)

        for (i in 0 until players.size) {
            var row2: TableRow = TableRow(this)
            var text1: TextView = TextView(this)
            text1.text = players[i].name[0].toString()
            row2.addView(text1)

            for (j in 0 until players.size) {
                var text2: TextView = TextView(this)
                if (i != j) {
                    if (game.countOfRounds(i, j) == 0) {
                        text2.text = "0"
                    } else {
                        text2.text = DecimalFormat("#0.00").format(
                            (game.countOfRounds(
                                i,
                                j
                            ) * configs["time"]!!.toFloat()) / game.countOfWords(i, j)
                        )
                    }
                }
                row2.addView(text2)
            }
            table.addView(row2)
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun updateGame(view: View) {
        var text: TextView = findViewById<TextView>(R.id.game_whoByWho)
        text.text = game.playerNames
//        var switch: Switch = findViewById<Switch>(R.id.game_out_allow)
//        switch.isChecked = false
    }

    fun checkRes(view: View) {
        var switch = findViewById<Switch>(R.id.history_mode)

        if (switch.isChecked) {
            updateResultsTimes(view)
        } else {
            updateResultsWords(view)
        }
    }

    @SuppressLint("WrongViewCast")
    fun approving(view: View) {
        var ll = findViewById<ScrollView>(R.id.aproov_ll).getChildAt(0) as LinearLayout

        ll.removeAllViews()

        var arrayWords = game.cur!!.wordsInRound
        var arrayWordsType = game.cur!!.typeOfWordsInRound

        for (i in 0 until arrayWords.size) {
            var linearLayout = LinearLayout(this)
            linearLayout.addView(CheckBox(this).apply { isChecked = arrayWordsType[i] == DONE })
            linearLayout.addView(TextView(this).apply { text = arrayWords[i].value })
            ll.addView(linearLayout)
        }
    }


    fun approve(view: View) {
        var ll = findViewById<ScrollView>(R.id.aproov_ll).getChildAt(0) as LinearLayout
        var words = game.cur

        for (i in 0 until words!!.wordsInRound.size) {
            var linearLayout: LinearLayout = ll.getChildAt(i) as LinearLayout
            var v: CheckBox = linearLayout.getChildAt(0) as CheckBox
            if (v.isChecked) {
                words.typeOfWordsInRound[i] = DONE
                game.cur!!.typeOfWordsInRound[i] = DONE
            } else {
                words.typeOfWordsInRound[i] = SKIP
                game.cur!!.typeOfWordsInRound[i] = SKIP

            }
        }
        game.nextRound(words.result())
        game.nextWord()
        goToGame(view)
    }

    @SuppressLint("ResourceAsColor")
    fun addPlayer(view: View) {
        var containerNames = findViewById<LinearLayout>(R.id.create_players_containerTexts)

        if (containerNames.childCount > 12) {
            Toast.makeText(this, "Максимум 13 игроков", Toast.LENGTH_SHORT).show()
            return
        }

        var horizontalL = LinearLayout(this).apply {
            id = 900000 + containerNames.childCount
            orientation = LinearLayout.HORIZONTAL
        }

        val button1 = Button(this).apply {
            id = 900000 + containerNames.childCount
            height = 125
            text = "X"
        }

        val button2 = Button(this).apply {
            id = 900000 + containerNames.childCount
            text = "⇵"
            height = 125
        }

        val textView = EditText(this).apply {
            height = 125
            minWidth =
                (resources.displayMetrics.widthPixels / resources.displayMetrics.density).toInt() * 3 / 2
            gravity = Gravity.CENTER_HORIZONTAL
            isSingleLine = true
            maxWidth =
                (resources.displayMetrics.widthPixels / resources.displayMetrics.density).toInt() * 3 / 2
        }

        button1.setOnClickListener {
            containerNames.removeViewAt(button1.id - 900000)
            for (i in 0 until containerNames.childCount) {
                var ll: LinearLayout = containerNames.getChildAt(i) as LinearLayout
                ll.id = 900000 + i
                ll.getChildAt(0).id = 900000 + i
                ll.getChildAt(2).id = 900000 + i
            }
        }

        button2.setBackgroundColor(Color.parseColor("#D5D6D6"))

        button2.setOnClickListener {
            if (configs["swapPlayers"] != -1) {
                for (i in 0 until containerNames.childCount) {
                    var ll: LinearLayout = containerNames.getChildAt(i) as LinearLayout
                    ll.getChildAt(0).setBackgroundColor(Color.parseColor("#D5D6D6"))
                }
                if (configs["swapPlayers"] != button2.id) {
                    val number: Int = configs["swapPlayers"]!!.toInt()
                    var ll1: LinearLayout =
                        containerNames.getChildAt(button2.id - 900000) as LinearLayout
                    var ll2: LinearLayout = containerNames.getChildAt(number) as LinearLayout
                    var text1: EditText = ll1.getChildAt(1) as EditText
                    var text2: EditText = ll2.getChildAt(1) as EditText
                    var swapText = text2.text

                    text2.text = text1.text
                    text1.text = swapText
                }
                configs["swapPlayers"] = -1
            } else {
                configs["swapPlayers"] = button2.id - 900000
                button2.setBackgroundColor(Color.parseColor("#ffdd00"))
            }
        }

        horizontalL.addView(button2)
        horizontalL.addView(textView)
        horizontalL.addView(button1)
        containerNames.addView(horizontalL)
    }


    fun timeMinus(view: View) {
        if (configs["time"] != 5) {
            configs["time"] = (configs["time"])!!.minus(5)
            var text = findViewById<TextView>(R.id.game_settings_time)
            text.text = configs["time"].toString()
        }
    }

    fun timePlus(view: View) {
        configs["time"] = (configs["time"])!!.plus(5)
        var text = findViewById<TextView>(R.id.game_settings_time)
        text.text = configs["time"].toString()
    }

    fun switchTeamMode(view: View) {
        if (configs["teamMode"] == 1) {
            configs["teamMode"] = 0
        } else {
            configs["teamMode"] = 1
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @SuppressLint("MissingInflatedId")
    fun finishCreating(view: View) {
        val namesLayout: LinearLayout = findViewById(R.id.create_players_containerTexts)
        if (namesLayout.childCount < 2) {
            Toast.makeText(this, "Минимум игроков 2", Toast.LENGTH_SHORT).show()
            return
        }
        if (configs["teamMode"] == 1 && namesLayout.childCount % 2 == 1) {
            Toast.makeText(
                this, "В командном режиме нужно чётное число игроков", Toast.LENGTH_SHORT
            ).show()
            return
        }
        for (i in 0 until namesLayout.childCount) {
            val nameText: EditText =
                (namesLayout.getChildAt(i) as LinearLayout).getChildAt(1) as EditText
            var name: String = nameText.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "У одного из игроков пустое имя", Toast.LENGTH_SHORT).show()
                return
            }
        }

        for (key in dicts.keys) {
            if (dicts[key] == 1) {
                dict.paste(convertToDict(readWordsFromFile(this, key + "_dict.txt")))
            }
        }

        if (dict.unused.list.size == 0) {
            Toast.makeText(this, "Игровой словарь пуст", Toast.LENGTH_SHORT).show()
            return
        }

        players = ArrayList()
        for (i in 0 until namesLayout.childCount) {
            val nameText: EditText =
                (namesLayout.getChildAt(i) as LinearLayout).getChildAt(1) as EditText
            var name: String = nameText.text.toString()
            players.add(Player(name))
        }



        if (configs["teamMode"] == 1) {
            game = GameByPair(players, dict)
        } else {
            game = GameEveryoneWithEveryone(players, dict)
        }

        game.nextRound()
        game.nextWord()

        goToGame(view)
    }

    fun startRound(view: View) {
        setContentView(R.layout.round)
        val textView = findViewById<TextView>(R.id.round_timerTextView)
        var text2: TextView = findViewById<TextView>(R.id.round_word)
        text2.text = game.currentWord.value
        object : CountDownTimer((1000 * (configs["time"]!!.toInt())).toLong(), 100) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                textView.text =
                    "Осталось: ${millisUntilFinished / 1000}.${millisUntilFinished % 1000 / 100}"
            }

            override fun onFinish() {
                val mediaPlayer = MediaPlayer.create(context, FileProvider.getUriForFile(context, "com.example.the_hat.fileprovider", File(context.getFilesDir(), "end_sound.mp3")))
                mediaPlayer.start()
                goToApprove(view)
                approving(view)
            }
        }.start()

        object : CountDownTimer((1000 * (configs["time"]!!.toInt() -5)).toLong(), 100) {
            override fun onTick(p0: Long) {
            }

            override fun onFinish() {
                val mediaPlayer = MediaPlayer.create(context, FileProvider.getUriForFile(context, "com.example.the_hat.fileprovider", File(context.getFilesDir(), "end_sound.mp3")))
                mediaPlayer.start()
            }
        }.start()
    }

    fun doneWord(view: View) {
        val mediaPlayer = MediaPlayer.create(context, FileProvider.getUriForFile(context, "com.example.the_hat.fileprovider", File(context.getFilesDir(), "doneWord_sound.mp3")))
        mediaPlayer.start()
        game.doneWord()
        getNewWord(view)
    }

    fun doneSkip(view: View) {
        if (game.hasSkipWord()) {
            game.doneSkipWord()
            var text: TextView = findViewById<TextView>(R.id.round_skippedWord)
            text.text = ""
            var button = findViewById<Button>(R.id.round_buttonDoneSkip)
            button.text = "Пропустить"
        } else {
            game.skipWord()
            getNewWord(view)
            var text: TextView = findViewById<TextView>(R.id.round_skippedWord)
            text.text = game.skipWord
            var button = findViewById<Button>(R.id.round_buttonDoneSkip)
            button.text = "Угадано пропущенное"
        }
    }

    fun failWord(view: View) {
        game.failWord()
        getNewWord(view)
    }

    fun failSkip(view: View) {
        if (game.hasSkipWord()) {
            game.failSkipWord()
            var text: TextView = findViewById<TextView>(R.id.round_skippedWord)
            text.text = ""
            var button = findViewById<Button>(R.id.round_buttonDoneSkip)
            button.text = "Пропустить"
        }
    }

    fun getNewWord(view: View) {
        var word: Word = game.nextWord()
        var text: TextView = findViewById<TextView>(R.id.round_word)
        text.text = word.value
    }

    private fun saveToFile(context: Context, fileName: String, content: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
            output.write(content.toByteArray())
        }
    }

    private fun saveToFile(context: Context, fileName: String, content: File) {
        context.assets.open(fileName).use { inputStream ->
            FileOutputStream(content).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun deleteFile(context: Context, fileName: String) {
        context.deleteFile(fileName)
    }

    private fun readFromFile(context: Context, fileName: String): String {
        return context.openFileInput(fileName).bufferedReader().useLines { lines ->
            lines.joinToString("\n")
        }
    }

    private fun readWordsFromFile(context: Context, fileName: String) : MutableList<String> {
        val text = readFromFile(context, fileName)
        val words = text.split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotEmpty() }
        return words.toMutableList()
    }

    private fun convertToDict(array: MutableList<String>) : Dict<Word> {
        val arrayWords : MutableList<Word> = mutableListOf()
        for (i in 1 until array.size) {
            arrayWords.add(Word(array[i]))
        }
        return Dict(RandomList(arrayWords))
    }

    private fun convertDictToText(dict: Dict<Word>) : String {
        var str : String = ""

        for (i in dict.unused.list) {
            str += " " + i.value
        }

        return str
    }

    fun opennerFile(view: View) {
        val text = findViewById<EditText>(R.id.menage_dict_text)
        val notif = findViewById<TextView>(R.id.menage_dict_notif)

        if (dicts.containsKey(text.text.toString())) {
            notif.text = "имя уже занято"
            return
        }

        openTxtFile()

        while (!fileList().contains("temp_dict.txt")) {}

        val words = readFromFile(this, "temp_dict.txt")
        saveToFile(this, text.text.toString() + "_dict.txt", words)
        deleteFile("temp_dict.txt")
        dicts[text.text.toString()] = 0
        text.text.clear()
        goToMenage(view)
    }

    private val openTxtFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                val text = readTextFromUri(it)
                saveToFile(this, "temp_dict.txt", text)
            }
        }
    }

    fun openTxtFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }
        openTxtFileLauncher.launch(intent)
    }

    private fun readTextFromUri(uri: Uri): String {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            return BufferedReader(InputStreamReader(inputStream)).readText()
        }
        return "unfind"
    }

    private fun updateDicts() {
        val names = fileList()

        for (name in names) {
            if (name.endsWith("_dict.txt") && name.removeSuffix("_dict.txt") != "temp") {
                dicts[name.removeSuffix("_dict.txt")] = 0
            }
        }
    }
}
