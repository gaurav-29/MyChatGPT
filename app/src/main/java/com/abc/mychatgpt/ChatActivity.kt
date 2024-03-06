package com.abc.mychatgpt

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.abc.mychatgpt.adapters.MessageAdapter
import com.abc.mychatgpt.databinding.ActivityChatBinding
import com.abc.mychatgpt.models.MessageModel
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.Exception

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageList: ArrayList<MessageModel>
    private lateinit var adapter: MessageAdapter
    val apiUrl = "https://api.openai.com/v1/chat/completions"
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_toolbar))

        messageList = ArrayList()

        binding.chatRV.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(messageList)
        binding.chatRV.adapter = adapter

        binding.sendIV.setOnClickListener {
            if (Utils.isInternetAvailable(this)) {
                val text = binding.inputET.text.toString().trim()
                if (text.isNotEmpty() and text.isNotBlank()) {
                    addToChat(text, true)
                    getResponseFromAI(text)
                    binding.inputET.text.clear()
                    binding.welcomeTV.visibility = View.GONE
                } else {
                    Toast.makeText(this, "Please enter your query", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "No internet available", Toast.LENGTH_LONG).show()
            }
        }

        // Code to convert Speech to Text and send text in in API call.
        binding.speakIV.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...")
            try {
                activityResultLauncher.launch(intent)
            } catch (e : ActivityNotFoundException) {
                Toast.makeText(this, "Device is not supported for this feature", Toast.LENGTH_LONG).show()
            }
        }
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult? ->
            if (result!!.resultCode == RESULT_OK && result.data != null) {
                val speechData = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val speechText = speechData?.get(0)
                if (speechText != null) {
                    addToChat(speechText, true)
                    binding.welcomeTV.visibility = View.GONE
                    getResponseFromAI(speechText)
                }
                Log.e("TEXT", speechText.toString())
                Toast.makeText(this, "$speechText", Toast.LENGTH_SHORT).show()
            }
        }
        // ----------------- Speech To Text code ends.-----------------------------------------------
    }

    private fun addToChat(text: String, byUser: Boolean) {
        messageList.add(MessageModel(text, byUser))
        adapter.notifyDataSetChanged()
        binding.chatRV.smoothScrollToPosition(adapter.itemCount)
    }
    private fun addResponse(responseMsg: String) {
        messageList.removeAt(messageList.size-1)
        addToChat(responseMsg, false)
    }

    private fun getResponseFromAI(query: String) {

        messageList.add(MessageModel("Typing...", false))
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)

        val jsonObject = JSONObject()
        val jsonArray = JSONArray()
        val jsonObject2 = JSONObject()

        try {
            jsonObject.put("model", "gpt-3.5-turbo")
            jsonObject2.put("role", "user")
            jsonObject2.put("content", query)
            jsonArray.put(jsonObject2)
            jsonObject.put("messages", jsonArray)
            jsonObject.put("temperature", 1)
            jsonObject.put("max_tokens", 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val postRequest = object : JsonObjectRequest(Method.POST, apiUrl, jsonObject,

            Response.Listener { response ->
                Log.e("RES", response.toString())
                val responseMsg: String =
                    response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                val resp = responseMsg.replaceFirst("\n", "").replaceFirst("\n", "")
                addResponse(resp)
            },
            Response.ErrorListener { error ->
                Log.e("RES", "Error is : " + error.message + "\n" + error)
                addResponse("Failed to load response..")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"
                params["Authorization"] = "Bearer ${Utils.ACCESS_TOKEN}"
                return params
            }
        }
        postRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 25000
            }
            override fun getCurrentRetryCount(): Int {
                return 25000
            }
            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
                Log.e("RES", "Volley Error : " + error.message + "\n" + error)
            }
        }
        queue.add(postRequest)
    }
}