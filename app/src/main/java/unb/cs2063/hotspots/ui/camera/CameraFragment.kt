package unb.cs2063.hotspots.ui.camera

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Time
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import unb.cs2063.hotspots.R
import unb.cs2063.hotspots.databinding.FragmentCameraBinding
import kotlin.random.Random


class CameraFragment : Fragment() {

    private var TAG = "CameraFragment"

    private lateinit var cameraLauncher : ActivityResultLauncher<Intent>
    private lateinit var capturedImageUri: Uri

    private var binding: FragmentCameraBinding? = null
    private val Binding get() = binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)

        val root: View = Binding.root

        //making bg and buttons invisible for cameraloading
        Binding.cameraLayout.setBackgroundColor(Color.parseColor("#353535"))
        Binding.publish.visibility = View.INVISIBLE


        //waiting for camera response... displays image to screen
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Binding.imageView.setImageURI(capturedImageUri)

                Binding.cameraLayout.setBackgroundColor(Color.WHITE)
                Binding.publish.visibility = View.VISIBLE
            }
        }

        //Publish image to firebase...
        Binding.publish.setOnClickListener{
            Binding.publish.isClickable = false
            Binding.progressBar.startAnimation(setupAnimations())
        }

        dispatchTakePictureIntent()


        return root
    }


    //Gets the picture location to store on the heatmap
    //Picture name should be exact location for simplicity of retreival
    public fun getCurrentLocation(){
        TODO()
    }

    private fun dispatchTakePictureIntent() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "test.jpg")

        val resolver: ContentResolver = requireContext().contentResolver
        capturedImageUri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri)

        if (requireContext().packageManager != null) {
            cameraLauncher.launch(takePictureIntent)
        }
    }


    private fun setupAnimations(): RotateAnimation{
        val rotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.duration = 1000
        rotateAnimation.repeatCount = 0

        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                Binding.progressBar.visibility = View.VISIBLE

            }

            override fun onAnimationEnd(animation: Animation?) {
                Binding.progressBar.clearAnimation()
                Binding.progressBar.visibility = View.INVISIBLE
                foldImageToCorner(Binding.imageView)
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
        })
        return rotateAnimation
    }

    private fun foldImageToCorner(view: View) {
        val animatorSet = AnimatorSet()

        // Scale down the image
        val scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.2f)
        val scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.2f)

        // Translate the image to the corner
        val translationXAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, -view.width.toFloat())
        val translationYAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, -view.height.toFloat())

        // Combine animations
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator, translationXAnimator, translationYAnimator)
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.duration = 500 // Set the animation duration in milliseconds

        // Listen for animation completion
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                Binding.publish.visibility = View.INVISIBLE

            }
            override fun onAnimationEnd(animation: Animator) {
                // Animation is complete, you can perform additional actions if needed
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}

        })

        animatorSet.start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}