package com.abc.mychatgpt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.abc.mychatgpt.databinding.ActivityTranslateBinding
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class TranslateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTranslateBinding
    val apiUrl = "https://api.openai.com/v1/chat/completions"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTranslateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_toolbar))

        binding.gujaratiBTN.setOnClickListener {
            if (Utils.isInternetAvailable(this)) {
                val text = binding.inputET.text.toString().trim()
                val language = "Gujarati"
                if (text.isNotEmpty() and text.isNotBlank()) {
                    getResponseFromAI(text, language)
                    //binding.inputET.text.clear()
                } else {
                    Toast.makeText(this, "Please enter your query", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "No internet available", Toast.LENGTH_LONG).show()
            }
        }
        binding.hindiBTN.setOnClickListener {
            if (Utils.isInternetAvailable(this)) {
                val text = binding.inputET.text.toString().trim()
                val language = "Hindi"
                if (text.isNotEmpty() and text.isNotBlank()) {
                    getResponseFromAI(text, language)
                    //binding.inputET.text.clear()
                } else {
                    Toast.makeText(this, "Please enter your query", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "No internet available", Toast.LENGTH_LONG).show()
            }
        }
        binding.japaneseBTN.setOnClickListener {
            if (Utils.isInternetAvailable(this)) {
                val text = binding.inputET.text.toString().trim()
                val language = "Japanese"
                if (text.isNotEmpty() and text.isNotBlank()) {
                    getResponseFromAI(text, language)
                    //binding.inputET.text.clear()
                } else {
                    Toast.makeText(this, "Please enter your query", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "No internet available", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getResponseFromAI(text: String, language: String) {

        binding.translatingTV.visibility = View.VISIBLE
        binding.translatedTV.visibility = View.GONE

        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)

        val jsonObject = JSONObject()
        val jsonArray = JSONArray()
        val jsonObject2 = JSONObject()

        try {
            jsonObject.put("model", "gpt-3.5-turbo")
            jsonObject2.put("role", "user")
            jsonObject2.put("content", "Translate $text into $language")
            jsonArray.put(jsonObject2)
            jsonObject.put("messages", jsonArray)
            jsonObject.put("temperature", 1)
            jsonObject.put("max_tokens", 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val postRequest = object : JsonObjectRequest(
            Method.POST, apiUrl, jsonObject,

            Response.Listener { response ->
                Log.e("RES", response.toString())
                binding.translatingTV.visibility = View.GONE
                binding.translatedTV.visibility = View.VISIBLE
                val responseMsg: String =
                    response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                val resp = responseMsg.replaceFirst("\n", "").replaceFirst("\n", "")
                binding.translatedTV.text = resp

            },
            Response.ErrorListener { error ->
                binding.translatingTV.visibility = View.GONE
                binding.translatedTV.visibility = View.VISIBLE
                Log.e("RES", "Error is : " + error.message + "\n" + error)
                Toast.makeText(this, "Something went wrong...", Toast.LENGTH_LONG).show()

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