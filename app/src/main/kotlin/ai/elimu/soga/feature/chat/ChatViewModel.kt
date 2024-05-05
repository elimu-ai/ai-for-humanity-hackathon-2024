package ai.elimu.soga.feature.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    generativeModel: GenerativeModel
) : ViewModel() {

    var pointCounter = 0.0f;

    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("You'll act as a friendly tutor who helps 6-year-old children learn basic math. Use emojis when you create word problems. Your name is Nya.") },
            content(role = "model") { text("Hi there! I'm Nya, your friendly math tutor \uD83D\uDE0A I'm here to help you learn some cool math concepts and have fun while doing it! \uD83C\uDF89") }
        )
    )

    private val _uiState: MutableStateFlow<ChatUiState> =
        MutableStateFlow(ChatUiState(chat.history.map { content ->
            // Map the initial messages
            ChatMessage(
                text = content.parts.first().asTextOrNull() ?: "",
                participant = if (content.role == "user") Participant.USER else Participant.MODEL,
                isPending = false
            )
        }))
    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()


    fun sendMessage(userMessage: String) {
        // Add a pending message
        _uiState.value.addMessage(
            ChatMessage(
                text = userMessage,
                participant = Participant.USER,
                isPending = true
            )
        )



        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userMessage)

                _uiState.value.replaceLastPendingMessage()

                Log.i("NumberGenerated","Number 2")

                response.text?.let { modelResponse ->
                    _uiState.value.addMessage(
                        ChatMessage(
                            text = modelResponse,
                            participant = Participant.MODEL,
                            isPending = false
                        )
                    )
                    if (modelResponse.contains("\uD83C\uDF1F") || modelResponse.contains("Nya") || modelResponse.contains("Correct") || modelResponse.contains("that's right") || modelResponse.contains("fantastic")) { pointCounter += 0.1f
                        Log.i("Mytag","viewModelScope") }
                }
            } catch (e: Exception) {
                _uiState.value.replaceLastPendingMessage()
                _uiState.value.addMessage(
                    ChatMessage(
                        text = e.localizedMessage,
                        participant = Participant.ERROR
                    )
                )
            }
        }
    }
}
