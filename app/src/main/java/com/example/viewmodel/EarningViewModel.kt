package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.EarningRepository
import com.example.data.EarningTransaction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

// Represents an interactive survey model
data class SurveyQuestion(
    val id: Int,
    val question: String,
    val options: List<String>
)

class EarningViewModel(private val repository: EarningRepository) : ViewModel() {

    // 1. Core State Flow
    val allTransactions: StateFlow<List<EarningTransaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalPoints: StateFlow<Int> = repository.totalPoints
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Current general alert message
    private val _uiAlert = MutableStateFlow<String?>(null)
    val uiAlert: StateFlow<String?> = _uiAlert.asStateFlow()

    // 2. Daily Check-in state
    private val _checkedInToday = MutableStateFlow(false)
    val checkedInToday: StateFlow<Boolean> = _checkedInToday.asStateFlow()

    private val _streakCount = MutableStateFlow(1)
    val streakCount: StateFlow<Int> = _streakCount.asStateFlow()

    // 3. Simulated Video Ad state
    private val _isVideoPlaying = MutableStateFlow(false)
    val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()

    private val _videoCountdown = MutableStateFlow(0)
    val videoCountdown: StateFlow<Int> = _videoCountdown.asStateFlow()

    private var videoJob: Job? = null

    // 4. Interactive Survey State
    private val _activeSurveyQuestion = MutableStateFlow<SurveyQuestion?>(null)
    val activeSurveyQuestion: StateFlow<SurveyQuestion?> = _activeSurveyQuestion.asStateFlow()

    private val _surveyStep = MutableStateFlow(0)
    val surveyStep: StateFlow<Int> = _surveyStep.asStateFlow()

    private val surveyQuestions = listOf(
        SurveyQuestion(1, "Which gadget do you plan to purchase next?", listOf("Smartphone", "Smartwatch", "Wireless Earbuds", "Laptop/PC")),
        SurveyQuestion(2, "How much time do you spend on mobile apps daily?", listOf("Under 1 hour", "1 - 3 hours", "3 - 5 hours", "5+ hours")),
        SurveyQuestion(3, "What type of online content do you consume most?", listOf("Educational Videos", "Gaming Streams", "Music & Podcasts", "Short-form Reels")),
        SurveyQuestion(4, "Which rewards do you prefer to redeem most?", listOf("Steam Gift Cards", "Amazon Shopping Vouchers", "PayPal Cashout", "Google Play Codes"))
    )

    // 5. Speed Solver Math Captcha state
    private val _mathNum1 = MutableStateFlow(0)
    val mathNum1: StateFlow<Int> = _mathNum1.asStateFlow()

    private val _mathNum2 = MutableStateFlow(0)
    val mathNum2: StateFlow<Int> = _mathNum2.asStateFlow()

    private val _mathAnswer = MutableStateFlow("")
    val mathAnswer: StateFlow<String> = _mathAnswer.asStateFlow()

    private val _mathError = MutableStateFlow(false)
    val mathError: StateFlow<Boolean> = _mathError.asStateFlow()

    // 6. Referral code state
    private val _referralInput = MutableStateFlow("")
    val referralInput: StateFlow<String> = _referralInput.asStateFlow()

    private val _referralApplied = MutableStateFlow(false)
    val referralApplied: StateFlow<Boolean> = _referralApplied.asStateFlow()

    // Referral code of this device (static unique mockup)
    val myReferralCode = "EARN77X"

    // 7. Security / Authentication Simulation
    private val _isUserLoggedIn = MutableStateFlow(true) // Defaults to true to support Mukesh's metadata session, can be logged out from profile
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("vishwakarmamukesh4539@gmail.com")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUserName = MutableStateFlow("Mukesh Vishwakarma")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _currentUserTier = MutableStateFlow("Pro Active")
    val currentUserTier: StateFlow<String> = _currentUserTier.asStateFlow()

    fun loginUser(email: String, name: String) {
        val trimmedEmail = email.trim()
        val trimmedName = name.trim()
        if (trimmedEmail.isEmpty()) {
            _uiAlert.value = "Error: Email address cannot be empty!"
            return
        }
        _currentUserEmail.value = trimmedEmail
        _currentUserName.value = if (trimmedName.isNotEmpty()) trimmedName else trimmedEmail.substringBefore("@")
        _isUserLoggedIn.value = true
        _uiAlert.value = "Welcome back, ${_currentUserName.value}! Secure session initialized."
    }

    fun logoutUser() {
        _isUserLoggedIn.value = false
        _uiAlert.value = "You have been logged out securely."
    }

    fun signUpUser(name: String, email: String) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        if (trimmedName.isEmpty() || trimmedEmail.isEmpty()) {
            _uiAlert.value = "Error: Please fill in all fields!"
            return
        }
        _currentUserEmail.value = trimmedEmail
        _currentUserName.value = trimmedName
        _isUserLoggedIn.value = true
        _uiAlert.value = "Account created successfully! Enjoy a verification gift of +100 Coins!"
        viewModelScope.launch {
            repository.insertTransaction(
                EarningTransaction(
                    title = "Profile Verification Reward",
                    points = 100,
                    type = "SIGN_UP_BONUS",
                    details = "Bonus for completing security registration details."
                )
            )
        }
    }

    init {
        checkDailyStatus()
        generateNewMathPuzzle()
        insertWelcomeBonusIfEmpty()
    }

    private fun insertWelcomeBonusIfEmpty() {
        viewModelScope.launch {
            // Need a brief delay for database check to load
            delay(300)
            if (allTransactions.value.isEmpty()) {
                repository.insertTransaction(
                    EarningTransaction(
                        title = "Welcome Bonus Gift",
                        points = 150,
                        type = "WELCOME",
                        details = "Started earning journey with a sweet initial gift!"
                    )
                )
            }
        }
    }

    fun dismissAlert() {
        _uiAlert.value = null
    }

    // Check if Checked In Today
    fun checkDailyStatus() {
        viewModelScope.launch {
            val startOfToday = getStartOfToday()
            val checksToday = repository.getCountToday("CHECK_IN", startOfToday)
            _checkedInToday.value = checksToday > 0

            // Estimate streaks on how frequently checkins were performed
            // For design realism: we read total transactions and check if we checked-in yesterday or today
            val allHistory = allTransactions.value
            val checkins = allHistory.filter { it.type == "CHECK_IN" }
            if (checkins.isNotEmpty()) {
                _streakCount.value = (checkins.size % 7) + 1
            } else {
                _streakCount.value = 1
            }
        }
    }

    // Execute Daily Check-In
    fun claimDailyCheckIn() {
        viewModelScope.launch {
            val startOfToday = getStartOfToday()
            val alreadyChecked = repository.getCountToday("CHECK_IN", startOfToday) > 0
            if (alreadyChecked) {
                _uiAlert.value = "You have already claimed today's Check-In reward!"
                return@launch
            }

            val rewardAmount = 100
            repository.insertTransaction(
                EarningTransaction(
                    title = "Daily Check-In Reward",
                    points = rewardAmount,
                    type = "CHECK_IN",
                    details = "Day ${_streakCount.value} reward claimed successfully!"
                )
            )
            _checkedInToday.value = true
            _streakCount.value += 1
            _uiAlert.value = "Awesome! Claimed Daily Bonus of +$rewardAmount Coins! Streak extended."
        }
    }

    // Video Playback Logic
    fun startVideoAd() {
        if (_isVideoPlaying.value) return
        _isVideoPlaying.value = true
        _videoCountdown.value = 10

        videoJob = viewModelScope.launch {
            while (_videoCountdown.value > 0) {
                delay(1000)
                _videoCountdown.value -= 1
            }
            // Add rewards
            val pointsGained = 30
            repository.insertTransaction(
                EarningTransaction(
                    title = "Video Sponsor Reward",
                    points = pointsGained,
                    type = "VIDEO",
                    details = "Sponsor video streamed successfully."
                )
            )
            _isVideoPlaying.value = false
            _uiAlert.value = "Success! Stream completed. +$pointsGained Coins credited to ledger!"
        }
    }

    fun cancelVideoAd() {
        videoJob?.cancel()
        _isVideoPlaying.value = false
        _videoCountdown.value = 0
    }

    // Survey Flow logic
    fun startSurvey() {
        _surveyStep.value = 0
        _activeSurveyQuestion.value = surveyQuestions[_surveyStep.value]
    }

    fun answerSurveyQuestion(answer: String) {
        val currentStep = _surveyStep.value
        if (currentStep < surveyQuestions.size - 1) {
            _surveyStep.value = currentStep + 1
            _activeSurveyQuestion.value = surveyQuestions[_surveyStep.value]
        } else {
            // Completed!
            viewModelScope.launch {
                val reward = 200
                repository.insertTransaction(
                    EarningTransaction(
                        title = "Insights Survey Complete",
                        points = reward,
                        type = "SURVEY",
                        details = "Completed the consumer electronics tech preference questionnaire."
                    )
                )
                _activeSurveyQuestion.value = null
                _uiAlert.value = "Great work! Survey complete. Express Earning of +$reward Coins received!"
            }
        }
    }

    fun cancelSurvey() {
        _activeSurveyQuestion.value = null
        _surveyStep.value = 0
    }

    // Static captcha logic
    fun generateNewMathPuzzle() {
        _mathNum1.value = Random.nextInt(10, 89)
        _mathNum2.value = Random.nextInt(5, 9)
        _mathAnswer.value = ""
        _mathError.value = false
    }

    fun onMathAnswerChanged(input: String) {
        _mathAnswer.value = input.filter { it.isDigit() }
    }

    fun submitMathSolve() {
        val solution = _mathNum1.value + _mathNum2.value
        val userAns = _mathAnswer.value.toIntOrNull()
        if (userAns == solution) {
            viewModelScope.launch {
                val reward = 20
                repository.insertTransaction(
                    EarningTransaction(
                        title = "Precision Solver Micro-Task",
                        points = reward,
                        type = "MATH_GAME",
                        details = "Solved arithmetic puzzle verified: ${_mathNum1.value} + ${_mathNum2.value} = $solution"
                    )
                )
                generateNewMathPuzzle()
                _uiAlert.value = "Correct! Captcha verified. +$reward Coins added."
            }
        } else {
            _mathError.value = true
        }
    }

    // 8. Interactive Gaming Quest State
    private val _isGameRunning = MutableStateFlow(false)
    val isGameRunning: StateFlow<Boolean> = _isGameRunning.asStateFlow()

    private val _gameScore = MutableStateFlow(0)
    val gameScore: StateFlow<Int> = _gameScore.asStateFlow()

    private val _gameTimer = MutableStateFlow(15) // 15-second blitz
    val gameTimer: StateFlow<Int> = _gameTimer.asStateFlow()

    // Random coordinates inside the active board container represented by float alignment anchors
    private val _targetX = MutableStateFlow(0.5f)
    val targetX: StateFlow<Float> = _targetX.asStateFlow()

    private val _targetY = MutableStateFlow(0.5f)
    val targetY: StateFlow<Float> = _targetY.asStateFlow()

    private var gameJob: Job? = null

    fun startGamingQuest() {
        if (_isGameRunning.value) return
        _isGameRunning.value = true
        _gameScore.value = 0
        _gameTimer.value = 15
        randomizeTargetPosition()

        gameJob = viewModelScope.launch {
            while (_gameTimer.value > 0) {
                delay(1000)
                _gameTimer.value -= 1
                // Periodic auto-teleport of target if not clicked (makes it dynamic!)
                if (_gameTimer.value % 2 == 0) {
                    randomizeTargetPosition()
                }
            }
            // Game Finished!
            _isGameRunning.value = false
            val scoreEarned = _gameScore.value
            if (scoreEarned > 0) {
                val coinsAwarded = scoreEarned * 10 // Each target click = 10 coins!
                repository.insertTransaction(
                    EarningTransaction(
                        title = "Gaming Arcade Reward",
                        points = coinsAwarded,
                        type = "GAMING",
                        details = "Scored $scoreEarned taps in arcade click-quest game arena!"
                    )
                )
                _uiAlert.value = "Game Over! You hit $scoreEarned targets and earned +$coinsAwarded Coins!"
            } else {
                _uiAlert.value = "Game Over! No targets were hit. Try again to claim rewards!"
            }
        }
    }

    fun hitGamingTarget() {
        if (!_isGameRunning.value) return
        _gameScore.value += 1
        randomizeTargetPosition()
    }

    private fun randomizeTargetPosition() {
        _targetX.value = Random.nextFloat().coerceIn(0.1f, 0.9f)
        _targetY.value = Random.nextFloat().coerceIn(0.1f, 0.9f)
    }

    fun quitGamingQuest() {
        gameJob?.cancel()
        _isGameRunning.value = false
        _gameScore.value = 0
        _gameTimer.value = 0
    }

    // Referral input handling
    fun onReferralInputChanged(code: String) {
        _referralInput.value = code.uppercase()
    }

    fun applyReferralCode() {
        val input = _referralInput.value.trim()
        if (input.isEmpty()) return

        if (input == myReferralCode) {
            _uiAlert.value = "Error: You cannot enter your own referral code!"
            return
        }

        if (input.length < 5) {
            _uiAlert.value = "Error: Invalid referral code format!"
            return
        }

        viewModelScope.launch {
            // Check if referral bonus was already claimed
            val count = repository.allTransactions.stateIn(viewModelScope).value.count { it.type == "REFERRAL_REWARD" }
            if (count > 0) {
                _uiAlert.value = "Error: Referral bonus already claimed on this device!"
                return@launch
            }

            val reward = 250
            repository.insertTransaction(
                EarningTransaction(
                    title = "Friend Referral Reward",
                    points = reward,
                    type = "REFERRAL_REWARD",
                    details = "Claimed promo code: $input"
                )
            )
            _referralApplied.value = true
            _referralInput.value = ""
            _uiAlert.value = "Amazing! Promo code accepted. Instant bonus of +$reward Coins added to profile!"
        }
    }

    // Redeem Coins
    fun redeemGiftCard(itemTitle: String, costCoins: Int, payoutAccount: String) {
        val currentPoints = totalPoints.value
        if (currentPoints < costCoins) {
            _uiAlert.value = "Error: Insufficient points! You need $costCoins Coins, currently have $currentPoints."
            return
        }

        viewModelScope.launch {
            repository.insertTransaction(
                EarningTransaction(
                    title = "Redeem: $itemTitle",
                    points = -costCoins,
                    type = "REDEEM",
                    details = "Payout Account: $payoutAccount. Progress: Confirmed & processing."
                )
            )
            _uiAlert.value = "Redemption Confirmed! -$costCoins Coins processed. $itemTitle will be sent to $payoutAccount within 24 hours!"
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
            _checkedInToday.value = false
            _streakCount.value = 1
            _referralApplied.value = false
            insertWelcomeBonusIfEmpty()
            _uiAlert.value = "Simulator stats and transactions reset successfully!"
        }
    }

    private fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

// ViewModel Factory
class EarningViewModelFactory(private val repository: EarningRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EarningViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EarningViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
