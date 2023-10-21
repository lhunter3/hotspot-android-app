package unb.cs2063.hotspots.utils

import android.content.Context
import android.util.Log
import org.json.JSONObject
import unb.cs2063.hotspots.model.Question
import java.lang.Exception
import java.util.Objects

class JsonUtils(context: Context) {

    private lateinit var questions : ArrayList<Question>

    init{
        processJSON(context)
    }

    private fun processJSON(context: Context){
        questions = ArrayList()

        try{
            val jsonObject = JSONObject(Objects.requireNonNull(loadJSON(context)))

            val jsonArray = jsonObject.getJSONArray(JSON_HEADER)
            for (i in 0 until jsonArray.length()){
                var t = jsonArray.getJSONObject(i)
                val q = Question(t.getString(JSON_OBJ_TITLE),t.getString(JSON_OBJ_DESCRIPTION))
                questions.add(q)
            }
        }
        catch (e:Exception){
            Log.e(TAG, e.message.toString())
        }
    }

    private fun loadJSON(context: Context) : String?{
        val iStream = context.assets.open(JSON_FILE)
        val b = ByteArray(iStream.available())
        iStream.read(b)
        return String(b)
    }

    fun getQuestions(): ArrayList<Question>{
        return questions
    }

    companion object {
        private const val JSON_FILE = "FAQ.json"
        private const val TAG = "JsonUtils"
        private const val JSON_HEADER = "QUESTIONS"
        private const val JSON_OBJ_TITLE = "TITLE"
        private const val JSON_OBJ_DESCRIPTION = "DESCRIPTION"

    }
}