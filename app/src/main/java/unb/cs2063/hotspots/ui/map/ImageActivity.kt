package unb.cs2063.hotspots.ui.map

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import unb.cs2063.hotspots.R

class ImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.image_activity)
        val imageView = findViewById<ImageView>(R.id.imageView)

        //val userDataList = intent.getSerializableExtra("userDataList") as List<UserData>

        //Log.d(TAG, userDataList.toString())

        //if (userDataList != null) {
        //    Log.d(TAG,"setting image")
        //    imageView.setImageURI(userDataList.get(0).uri.toUri())
        //}


    }


    companion object{
        const val TAG = "ImageActivity"
    }



}