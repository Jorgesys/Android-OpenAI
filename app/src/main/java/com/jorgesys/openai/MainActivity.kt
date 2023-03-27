package com.jorgesys.openai

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var responseTxV: TextView
    lateinit var questionTxV: TextView
    lateinit var queryEdt: TextInputEditText
    //Get Open AI API Key from: https://platform.openai.com/account/api-keys
    var urlOpenAI = "https://api.openai.com/v1/completions"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        responseTxV = findViewById(R.id.idTVResponse)
        questionTxV = findViewById(R.id.idTVQuestion)
        queryEdt = findViewById(R.id.idEdtQuery)
        queryEdt.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                responseTxV.text = "Please wait..."
                if (!queryEdt.text.toString().isNullOrEmpty()) {
                    getResponse(queryEdt.text.toString())
                } else {
                    Toast.makeText(this, "Please enter your query for OpenAI..", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun getResponse(query: String) {
        questionTxV.text = query
        queryEdt.setText("")
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        val jsonObject: JSONObject? = JSONObject()
        jsonObject?.put("model", "text-davinci-003")
        jsonObject?.put("prompt", query)
        jsonObject?.put("temperature", 0)
        jsonObject?.put("max_tokens", 100)
        jsonObject?.put("top_p", 1)
        jsonObject?.put("frequency_penalty", 0.0)
        jsonObject?.put("presence_penalty", 0.0)

        val postRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, urlOpenAI, jsonObject,
                Response.Listener { response ->
                    val responseMsg: String =
                        response.getJSONArray("choices").getJSONObject(0).getString("text")
                    responseTxV.text = responseMsg
                    Log.i("OPENAIJorgesys", "JsonObjectRequest Response: $responseMsg")

                },
                Response.ErrorListener { error ->
                    Log.e("OPENAIJorgesys", "Error is : " + error.message + "\n" + error)
                }) {
                override fun getHeaders(): kotlin.collections.MutableMap<kotlin.String, kotlin.String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = "Bearer ${getString(R.string.openai_api_key)}"
                    Log.i("OPENAIJorgesys", "Request headers : $params")
                    return params;
                }
            }

        postRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 100000
            }

            override fun getCurrentRetryCount(): Int {
                return 100000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
                Log.e("OPENAIJorgesys", "Request error: ${error.message}")
            }
        })

        queue.add(postRequest)
    }
}