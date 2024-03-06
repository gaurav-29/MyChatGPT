package com.abc.mychatgpt

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.abc.mychatgpt.databinding.ActivityGenerateImageBinding
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import org.json.JSONObject

class GenerateImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateImageBinding
    val apiUrl = "https://api.openai.com/v1/images/generations"
    lateinit var imageUrl: String
    var myDownloadId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_toolbar))

        binding.generateBTN.setOnClickListener {
            if (Utils.isInternetAvailable(this)) {
                val text = binding.inputET.text.toString().trim()
                if (text.isNotEmpty() and text.isNotBlank()) {
                    getResponseFromAI(text)
                    binding.inputET.text.clear()
                } else {
                    Toast.makeText(this, "Please enter your query", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "No internet available", Toast.LENGTH_LONG).show()
            }
        }
        binding.saveBTN.setOnClickListener {
            val filename = "gpt-${System.currentTimeMillis()}.png"
            val request = DownloadManager.Request(Uri.parse(imageUrl))
                .setTitle("Image Download")
                .setDescription("Image downloading from chatGPT...")
//                .setDestinationUri(Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

            val downloadManager = getSystemService(DownloadManager::class.java)
            myDownloadId = downloadManager.enqueue(request)
        }
        // If we want to know when the download completed, we need following broadcast receiver.
        val br = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val id = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == myDownloadId) {
                    Toast.makeText(applicationContext, "Image download completed from chatGPT.", Toast.LENGTH_LONG).show()
                }
            }
        }
        registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun getResponseFromAI(text: String) {

        binding.imageIV.visibility = View.INVISIBLE
        binding.text.visibility = View.VISIBLE

        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)

        val jsonObject = JSONObject()

        try {
            jsonObject.put("prompt", text)
            jsonObject.put("n", 1)
            jsonObject.put("size", "256x256")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val postRequest = object : JsonObjectRequest(
            Method.POST, apiUrl, jsonObject,

            Response.Listener { response ->
                Log.e("RES", response.toString())

                imageUrl =
                    response.getJSONArray("data").getJSONObject(0).getString("url")

                if (setImageInImageview(imageUrl)) {
                    binding.imageIV.visibility = View.VISIBLE
                    binding.text.visibility = View.GONE
                }
            },
            Response.ErrorListener { error ->
                binding.imageIV.visibility = View.VISIBLE
                binding.text.visibility = View.GONE
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

    private fun setImageInImageview(imageUrl: String): Boolean {
        Glide
            .with(this)
            .load(imageUrl)
            .centerCrop()
//            .placeholder(R.drawable.img_placeholder)
            .into(binding.imageIV)
         return true
    }
}