package unb.cs2063.hotspots.ui.map

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import unb.cs2063.hotspots.R
import unb.cs2063.hotspots.model.UserData
import unb.cs2063.hotspots.utils.FireBaseUtil

class ImageActivity : AppCompatActivity() {

    private lateinit var currentUserData : UserData
    private lateinit var userDataList : ArrayList<UserData>
    private var firebase : FireBaseUtil = FireBaseUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_activity)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val likeButton = findViewById<Button>(R.id.likeButton)
        val dislikeButton = findViewById<Button>(R.id.dislikeButton)
        val reportButton = findViewById<Button>(R.id.reportButton)
        val exitButton = findViewById<ImageButton>(R.id.exitButton)
        val likedSet: MutableSet<String> = mutableSetOf()
        val dislikedSet: MutableSet<String> = mutableSetOf()

        val buttonPushDownAnimation = AnimationUtils.loadAnimation(this, R.anim.like_button_animation)
        val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_like_button_animation)

        supportActionBar?.hide()

        //getting userDataList
         userDataList = intent.getParcelableArrayListExtra<UserData>("userDataList")!!

        //setting the first image
        setImage(imageView, userDataList[0])

        likeButton.setOnClickListener {
            if(dislikedSet.contains(currentUserData.id)){
                dislikedSet.remove(currentUserData.id)
                currentUserData.dislikes -= 1
                dislikeButton.text = currentUserData.dislikes.toString()
            }
            //checks if user has already liked
            if(!likedSet.contains(currentUserData.id)) {
                likeButton.startAnimation(buttonPushDownAnimation)
                likeButton.animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {

                    }
                    override fun onAnimationRepeat(p0: Animation?) {}
                    override fun onAnimationEnd(animation: Animation) {
                        likedSet.add(currentUserData.id)
                        currentUserData.likes +=1
                        likeButton.text = currentUserData.likes.toString()
                        firebase.updateUserData(currentUserData)
                    }
                })

            }
            else{
                likeButton.startAnimation(shakeAnimation)
            }
        }

        dislikeButton.setOnClickListener {
            //checks if user already liked
            if (likedSet.contains(currentUserData.id)) {
                likedSet.remove(currentUserData.id)
                currentUserData.likes -= 1
                likeButton.text = currentUserData.likes.toString()
            }
            //checks if user has already disliked
            if (!dislikedSet.contains(currentUserData.id)) {
                dislikeButton.startAnimation(buttonPushDownAnimation)
                dislikeButton.animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {}
                    override fun onAnimationRepeat(p0: Animation?) {}
                    override fun onAnimationEnd(animation: Animation) {
                        dislikedSet.add(currentUserData.id)
                        currentUserData.dislikes += 1
                        dislikeButton.text = currentUserData.dislikes.toString()
                        firebase.updateUserData(currentUserData)
                    }
                })
            }
            else{
                dislikeButton.startAnimation(shakeAnimation)
            }
        }



        //setting up report button
        reportButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Report Picture")

            val input = EditText(this)
            input.hint = "Enter your report here"
            builder.setView(input)

            // Set up the submit and cancel buttons
            builder.setPositiveButton("Submit") { dialog, which ->
                Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
            builder.show()
        }

        //exit button
        exitButton.setOnClickListener {
            exitButton.startAnimation(buttonPushDownAnimation)
            exitButton.animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}
                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationEnd(animation: Animation) {
                    finish()
                }
            })
        }

        //setting up the swipe
        setupSwipeDetection(imageView, userDataList)

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

    private fun setImage(imageView: ImageView, userData: UserData) {
        currentUserData = userData
        findViewById<Button>(R.id.likeButton).text = currentUserData.likes.toString()
        findViewById<Button>(R.id.dislikeButton).text = currentUserData.dislikes.toString()
        findViewById<TextView>(R.id.timeAgo).text = currentUserData.getTimeAgo()

        val n = userDataList.indexOf(userData) + 1
        findViewById<TextView>(R.id.imageCounter).text = "$n/${userDataList.size}"

        // Show the loading layout while the image is loading
        val loadingLayout = findViewById<FrameLayout>(R.id.loadingLayout)
        loadingLayout.visibility = View.VISIBLE

        //Rotates loading icon
        startRotationAnimation()

        // Load the image into the view
        Glide.with(this)
            .load(userData.uri)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Hide the loading layout on failure
                    loadingLayout.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Image loaded successfully
                    // Hide the loading layout
                    loadingLayout.visibility = View.GONE
                    return false
                }
            })
            .into(imageView)
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

    //used for loading animation, rotates loading icon
    private fun startRotationAnimation() {
        val rotatingImageView = findViewById<ImageView>(R.id.rotatingImageView)
        val rotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.duration = 2000
        rotateAnimation.repeatCount = Animation.INFINITE
        rotatingImageView.startAnimation(rotateAnimation)
    }


    companion object{
        const val TAG = "ImageActivity"
        const val SWIPE_THRESHOLD = 100
        var x1: Float = 0f
        var x2: Float = 0f
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }



}