package com.softwarefactory.volleytest

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.softwarefactory.sdk.server.ServerError
import com.softwarefactory.sdk.server.ServerRequestManager
import com.softwarefactory.sdk.server.ServerResponse
import com.softwarefactory.volleytest.model.Post
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView:RecyclerView
    private lateinit var postList: MutableList<Post>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var recyclerViewAdapter:RecyclerViewAdapter
    lateinit var url:String


    //Testing for request string
    private lateinit var serverRequestManager:ServerRequestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        url = stringFromJNI()
        serverRequestManager = ServerRequestManager(this)
        recyclerView = findViewById(R.id.recyclerView)
        postList = ArrayList()
        layoutManager = LinearLayoutManager(this)
        recyclerViewAdapter = RecyclerViewAdapter(postList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = recyclerViewAdapter
        getData()
        getAllPosts()

    }


    private fun getData() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.show()

        val jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response ->
            for (i in 0 until response.length()) {
                try {
                    val jsonObject = response.getJSONObject(i)
                    Log.d("post",jsonObject.toString())
                    val post = Post()
                    post.title=jsonObject.getString("title")
                    post.body=jsonObject.getString("body")

                    postList.add(post)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    progressDialog.dismiss()
                }

            }
            recyclerViewAdapter.notifyDataSetChanged()
            progressDialog.dismiss()
        }, Response.ErrorListener { error ->
            Log.e("Volley", error.toString())
            progressDialog.dismiss()
        })
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest)
    }


    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun onResume() {
        super.onResume()
        serverRequestManager.getPost("1",object:ServerRequestManager.OnRequestFinishedListener{
            override fun onSuccess(response: ServerResponse) {
                super.onSuccess(response)
                try {
                    val dataObj = JSONObject(response.dataSt)
                    val postSt = dataObj.getString("post_id")
                    Log.d("post",postSt.toString())

                }catch (e:JSONException){
                    e.stackTrace
                }

            }
            override fun onError(err: ServerError) {
                super.onError(err)
            }
        })


    }
    fun getAllPosts(){
        serverRequestManager.getAllPosts(object :ServerRequestManager.OnRequestFinishedListener{
            override fun onSuccess(response: ServerResponse) {
                super.onSuccess(response)
                try {
                    val dataObj = JSONObject(response.dataSt)
                    for (i in 1 until dataObj.length()){
                        val posts = dataObj.getString(i.toString())
                        Log.d("posts", posts)
                    }
                }catch (e:JSONException){
                    e.stackTrace
                }
            }
        })
    }


}
