package org.tensorflow.lite.examples.objectdetection

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityMainBinding
import org.tensorflow.lite.examples.objectdetection.fragments.PermissionsFragmentDirections


import java.util.*


/**
 * Main entry point into our app. This app follows the single-activity pattern, and all
 * functionality is implemented in the form of fragments.
 */

class MainActivity : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var recognizer: SpeechRecognizer
    private var detectionRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context=this
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        var tts: TextToSpeech? = null
        //Initiate TTS
        tts = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                // Show the welcoming message
                tts?.speak("Welcome to RAYS, Here are the List of commands: Start Detection, Stop Detection, Navigate. How may I help you?", TextToSpeech.QUEUE_FLUSH, null, null)
                startListening()
            }
        })

        // Initialize the speech recognizer
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                startListening()
            }
            override fun onError(error: Int) {
                startListening()
            }

            fun onResume(){
                startListening()
            }

            override fun onResults(results: Bundle?) {
                val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                var destination: String? = ""
                if (result != null && result.isNotEmpty()) {
                    val spokenText = result[0]
                    Toast.makeText(context, spokenText, Toast.LENGTH_SHORT).show()
                    if (spokenText.contains("start detection") || spokenText.contains("start detecting"))  {
                        tts?.speak("Object Detection Initialised", TextToSpeech.QUEUE_FLUSH, null, null)
                        startDetection()
                    } else if (spokenText.contains("stop detection") || spokenText.contains("stop detecting")) {
                        tts?.speak("Object Detection Terminated", TextToSpeech.QUEUE_FLUSH, null, null)
                        stopDetection()
                    }else if (spokenText.contains("navigate to")) {
                        destination = spokenText.substringAfter("navigate to").trim()
                        tts?.speak("Navigating Maps", TextToSpeech.QUEUE_FLUSH, null, null)
                        startNavigation(destination)
                    }else{
                        startListening()
                    }
                    if(detectionRunning){
                        startListening()
                    }

                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recognizer.startListening(intent)
    }

    private fun startDetection() {
        if (!detectionRunning) {
            detectionRunning = true
            val navController = Navigation.findNavController(context as Activity, R.id.fragment_container)
            navController.navigate(PermissionsFragmentDirections.actionPermissionsToCamera())
            startListening()
        }
    }


    private fun stopDetection() {
        if (detectionRunning) {
            detectionRunning = false
            val navController = Navigation.findNavController(context as Activity, R.id.fragment_container)
            navController.navigate(R.id.action_camera_to_fragments)
            startListening()
        }
    }

    private fun startNavigation(destination: String?) {
        if (!destination.isNullOrEmpty()) {
            val gmmIntentUri = Uri.parse("google.navigation:q=$destination + \"&mode=w\"")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        } else {
            // Handle the case where the destination is not set
            Toast.makeText(this, "Destination not set", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        recognizer.destroy()
    }


    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
            // (https://issuetracker.google.com/issues/139738913)
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }
}