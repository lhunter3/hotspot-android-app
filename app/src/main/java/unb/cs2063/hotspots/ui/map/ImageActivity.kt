package unb.cs2063.hotspots.ui.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import unb.cs2063.hotspots.R
import unb.cs2063.hotspots.model.UserData

class ImageActivity : AppCompatActivity() {

    private lateinit var currentUserData : UserData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_activity)
        val imageView = findViewById<ImageView>(R.id.imageView)

        supportActionBar?.hide()
        //getting userDataList
        val userDataList = intent.getParcelableArrayListExtra<UserData>("userDataList")

        //setting the first image
        setImage(imageView, userDataList?.get(0) ?: UserData())


        //setting up the swipe
        setupSwipeDetection(imageView, userDataList!!)

        userDataList.forEach{ Log.d(TAG, it.toString())}




    }


    // janky way but works.
    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipeDetection(imageView: ImageView, userDataList: ArrayList<UserData>) {
        imageView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x1 = event.x
                    true
                }
                MotionEvent.ACTION_UP -> {
                    x2 = event.x
                    val deltaX = x2 - x1

                    if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                        if (x2 > x1) {
                            // Right swipe, perform action
                            performSwipeBackAction(imageView, userDataList)
                        } else {
                            // Left swipe, perform action
                            performSwipeNextAction(imageView, userDataList)
                        }
                    }

                    true
                }
                else -> false
            }
        }
    }

    private fun setImage(imageView: ImageView, userData: UserData){

        currentUserData = userData

        //loads first image into view
        Glide.with(this)
            .load(userData.uri)
            .into(imageView)

        //set like count and dislike count
        //TODO

    }

    private fun performSwipeBackAction(imageView: ImageView, userDataList: ArrayList<UserData>){
        //swipe back
        val index = userDataList.indexOf(currentUserData)
        if(index > 0){
            setImage(imageView, userDataList[index-1])
        }
    }

    private fun performSwipeNextAction(imageView: ImageView, userDataList: ArrayList<UserData>) {
        //swipe next
        val index = userDataList.indexOf(currentUserData)
        if(index < userDataList.size-1){
            setImage(imageView, userDataList[index+1])
        }
    }



    companion object{
        const val TAG = "ImageActivity"
        const val SWIPE_THRESHOLD = 100 // Adjust this threshold as needed
        var x1: Float = 0f
        var x2: Float = 0f
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }



}