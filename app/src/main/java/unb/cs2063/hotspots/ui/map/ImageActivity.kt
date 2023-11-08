package unb.cs2063.hotspots.ui.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

        //we should display location, time of post aswell as the likes and dislikes. A fake report button or if likes<disliked dont show type beat.
        // Initialize like and dislike buttons
        val likeButton = findViewById<Button>(R.id.likeButton)
        val dislikeButton = findViewById<Button>(R.id.dislikeButton)
        var likeCount = currentUserData.likes
        var dislikeCount = currentUserData.dislikes
        val likeText = findViewById<TextView>(R.id.likeCount)
        val dislikeText = findViewById<TextView>(R.id.dislikeCount)
        val likedSet: MutableSet<String> = mutableSetOf()
        val dislikedSet: MutableSet<String> = mutableSetOf()

        likeButton.setOnClickListener {
            //checks if user has already liked or disliked
            if(!likedSet.contains(currentUserData.id) && !dislikedSet.contains(currentUserData.id)) {
                likedSet.add(currentUserData.id)
                likeCount++
                // Update the like count in the UserData object
                currentUserData.likes = likeCount
                // Update the UI with the new like count
                likeText.text = likeCount.toString()
            }
        }

        dislikeButton.setOnClickListener {
            //checks if user has already liked or disliked
            if(!likedSet.contains(currentUserData.id) && !dislikedSet.contains(currentUserData.id)) {
                dislikedSet.add(currentUserData.id)
                dislikeCount++
                // Update the dislike count in the UserData object
                currentUserData.likes = dislikeCount
                // Update the UI with the new dislike count
                dislikeText.text = dislikeCount.toString()
            }
        }

        //setting up report button
        val reportButton = findViewById<Button>(R.id.reportButton)
        reportButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Report Picture")

            val input = EditText(this)
            input.hint = "Enter your report here"
            builder.setView(input)

            // Set up the submit and cancel buttons
            builder.setPositiveButton("Submit") { dialog, which ->
                val reportText = input.text.toString()
                Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

            builder.show()
        }

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

    // all the work concerning with setting up the image should be done in here. setting like, dislike,location, timee it was posted.
    private fun setImage(imageView: ImageView, userData: UserData){

        currentUserData = userData




        //loads first image into view
        //should setup a placeholder .placeholder(), while it is active hide the buttons, when it loads set the proper button info + show
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