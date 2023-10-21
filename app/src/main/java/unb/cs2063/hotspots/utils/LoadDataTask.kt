package unb.cs2063.hotspots.utils

import android.content.Context

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import unb.cs2063.hotspots.model.Question
import unb.cs2063.hotspots.ui.info.RecyclerAdapter
import java.util.concurrent.Executors


class LoadDataTask(private val activity: AppCompatActivity, recyclerView: RecyclerView?) {
    private val context: Context = activity.applicationContext
    private var recyclerView = recyclerView
    private val jsonUtils : JsonUtils = JsonUtils(context)
    private val TAG = "LoadDataTask"


    fun execute(){
        Executors.newSingleThreadExecutor().execute{
            val mainHandler = Handler(Looper.getMainLooper())
            val questions = jsonUtils.getQuestions()

            Log.i(TAG,questions.toString())
            mainHandler.post{
                updateDisplay(questions)
            }
        }
    }

    private fun updateDisplay(questions: ArrayList<Question>) {
        setupRecyclerView(questions)
        Toast.makeText(context, "Loaded File", Toast.LENGTH_LONG)
    }

    private fun setupRecyclerView(questions: ArrayList<Question>) {
        recyclerView!!.adapter = RecyclerAdapter(activity,questions)
    }

}