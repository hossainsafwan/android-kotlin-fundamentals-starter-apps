package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class GameViewModel: ViewModel() {

    companion object {
        // Time when the game is over
        private const val DONE = 0L

        // Countdown time interval
        private const val ONE_SECOND = 1000L

        // Total time for the game
        private const val COUNTDOWN_TIME = 60000L
    }

    // The current word
    private var _word = MutableLiveData<String>()
    val word: LiveData<String> get() = _word

    // The current score
    private var _score = MutableLiveData<Int>()
    val score:  LiveData<Int> get() = _score

    private var _eventAllWordsReleased = MutableLiveData<Boolean>()
    val eventAllWordsReleased: LiveData<Boolean> get() = _eventAllWordsReleased



    private val timer: CountDownTimer

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    //Timer
    private var _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long> get() = _currentTime

    // The String version of the current time
    val currentTimeString = Transformations.map(currentTime) { time->
        DateUtils.formatElapsedTime(time)
    }

    private var _hintLength = MutableLiveData<Int>()
    val hintLength: LiveData<Int> get() = _hintLength

    private var _hintIndex = MutableLiveData<Int>()
    val hintIndex: LiveData<Int> get() = _hintIndex

    private var _hintCharacter = MutableLiveData<String>()
    val hintCharacter: LiveData<String> get() = _hintCharacter


    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
            "queen",
            "hospital",
            "basketball",
            "cat",
            "change",
            "snail",
            "soup",
            "calendar",
            "sad",
            "desk",
            "guitar",
            "home",
            "railway",
            "zebra",
            "jelly",
            "car",
            "crow",
            "trade",
            "bag",
            "roll",
            "bubble"
        )
        wordList.shuffle()
    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = _score.value?.minus(1)
        nextWord()
    }

     fun onCorrect() {
        _score.value = _score.value?.plus(1)
        nextWord()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        if (!wordList.isEmpty()) {
            //Select and remove a word from the list
            val wordSelected = wordList.removeAt(0)
            _word.value = wordSelected

            _hintLength.value = wordSelected.length
            val randomIndex = (0..wordSelected.length).random()
            _hintIndex.value = randomIndex
            _hintCharacter.value = wordSelected.get(randomIndex-1).toString().toUpperCase()

        } else { // no more words in the wordsList
            resetList()
        }
    }

    fun onGameFinish() {
        _eventAllWordsReleased.value = true
    }
    fun onGameFinishComplete() {
        _eventAllWordsReleased.value = false
    }

    init {
        _word.value = ""
        _score.value = 0
        resetList()
        nextWord()
        Log.i("GameViewModel","GameViewModel Created!")
                                       //total time     time for one tick
        timer = object: CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                                    //e.g:  60000 / 1000
                _currentTime.value = millisUntilFinished/ONE_SECOND
            }

            override fun onFinish() {
                _currentTime.value = DONE
                onGameFinish()
            }
        }

        timer.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }
}