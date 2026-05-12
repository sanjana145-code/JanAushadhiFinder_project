package com.example.janaushadhifinder

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.janaushadhifinder.ai.GeminiRepository
import com.example.janaushadhifinder.data.MedicineRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AiAssistantUiState(
    val question: String = "",
    val answer: String = "",
    val extractedText: String = "",
    val matches: List<Medicine> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)

class AiAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val geminiRepository = GeminiRepository()
    private val medicineRepository = MedicineRepository(application)
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState

    fun onQuestionChanged(value: String) {
        _uiState.value = _uiState.value.copy(question = value)
    }

    fun askAi() {
        val question = _uiState.value.question.trim()
        if (question.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            val prompt = """
                You are a cautious healthcare savings assistant for Jan-Aushadhi users in India.
                Explain in simple language, suggest generic alternatives when relevant, and include a short disclaimer that this is not medical advice.
                User question: $question
            """.trimIndent()
            val result = geminiRepository.ask(prompt)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                answer = result.getOrElse { it.message ?: "Unable to reach Gemini." },
                error = result.exceptionOrNull()?.message.orEmpty()
            )
        }
    }

    fun explainMedicine(medicine: Medicine) {
        onQuestionChanged("Explain ${medicine.genericName}, common uses, side effects, and safe generic switching guidance.")
        askAi()
    }

    fun runOcrFromUri(uri: Uri) {
        viewModelScope.launch {
            val image = InputImage.fromFilePath(getApplication(), uri)
            runOcr(image)
        }
    }

    fun runOcrFromBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            runOcr(InputImage.fromBitmap(bitmap, 0))
        }
    }

    private suspend fun runOcr(image: InputImage) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = "")
        runCatching {
            medicineRepository.seedIfNeeded()
            val text = recognizer.process(image).await().text
            val medicines = medicineRepository.observeMedicines("").first()
            val matches = medicines.filter { medicine ->
                text.contains(medicine.brandName, ignoreCase = true) ||
                    text.contains(medicine.genericName.substringBefore(" "), ignoreCase = true)
            }.take(20)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                extractedText = text,
                matches = matches,
                answer = if (matches.isEmpty()) "No medicine matches found in the local database." else ""
            )
        }.onFailure {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = it.message ?: "OCR failed."
            )
        }
    }
}
