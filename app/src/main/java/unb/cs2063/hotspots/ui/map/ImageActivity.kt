package unb.cs2063.hotspots.ui.map

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import unb.cs2063.hotspots.R
import unb.cs2063.hotspots.model.UserData

class ImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.image_activity)
        val imageView = findViewById<ImageView>(R.id.imageView)

        val userDataList = intent.getParcelableArrayListExtra<UserData>("userDataList")

        Glide.with(this)
            .load(userDataList?.get(0)?.uri)
            .into(imageView)





        Log.d(TAG,"test")
        Log.d(TAG, userDataList.toString())



    }


    companion object{
        const val TAG = "ImageActivity"
    }



}