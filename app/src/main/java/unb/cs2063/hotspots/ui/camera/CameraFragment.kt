package unb.cs2063.hotspots.ui.camera

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import unb.cs2063.hotspots.R
import unb.cs2063.hotspots.databinding.FragmentCameraBinding
import unb.cs2063.hotspots.utils.FireBaseUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random


class CameraFragment : Fragment() {

    private var binding: FragmentCameraBinding? = null
    private val Binding get() = binding!!
    private lateinit var capturedImageUri: Uri
    private lateinit var  navController: NavController
    private lateinit var imageCapture: ImageCapture
    private lateinit var tempOutputFile: File
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var facingFront: Boolean = true
    private var firebaseUtil : FireBaseUtil = FireBaseUtil()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        navController = requireActivity().findNavController(R.id.nav_host_fragment_activity_main)
        val root: View = Binding.root


        //Camera Setup
        initializeCamera()

        //Button Animation
        val buttonPushDownAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.like_button_animation)

        //Take Picture
        Binding.captureButton.setOnClickListener {
            Binding.captureButton.startAnimation(buttonPushDownAnimation)
            Binding.captureButton.animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {

                }
                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationEnd(animation: Animation) {
                    captureImageAction()
                }
            })
        }


        //Exit Picture
        Binding.exitImage.setOnClickListener {
            Binding.exitImage.startAnimation(buttonPushDownAnimation)
            Binding.exitImage.animation.setAnimationListener(object :Animation.AnimationListener{
                override fun onAnimationStart(p0: Animation?) {}
                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationEnd(animation: Animation) {
                    Binding.imageContainer.visibility = View.INVISIBLE
                    Binding.captureButton.visibility = View.VISIBLE
                    Binding.cameraPreview.visibility = View.VISIBLE
                }
            })
        }

        //Flip lens
        Binding.flipCamera.setOnClickListener {
            Binding.flipCamera.startAnimation(buttonPushDownAnimation)
            Binding.flipCamera.animation.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationStart(p0: Animation?) {
                }
                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationEnd(animation: Animation) {
                    bindCamera()
                }

            })
        }

        //Publish Image
        Binding.publish.setOnClickListener{
            displayPopupConfirmation()
        }

        return root
    }

    private fun displayPopupConfirmation() {
        val alertDialog = AlertDialog.Builder(requireContext()).create()

        alertDialog.setTitle("Attention")
        alertDialog.setMessage("This image will be displayed on the public map at this current location for 24 hours. \nDo you wish to continue?")

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, _ ->
            dialog.dismiss()
            publishConfirmed()
        }

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, _ ->
            dialog.dismiss()
        }

        alertDialog.show()
    }
    private fun publishConfirmed(){

        firebaseUtil.pushUserData(requireActivity(),capturedImageUri)

        Binding.exitImage.visibility = View.INVISIBLE
        Binding.flipCamera.visibility = View.INVISIBLE
        Binding.publish.isClickable = false

        Binding.publish.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.button_disabled))

        val spinnerAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        spinnerAnimation.interpolator = LinearInterpolator()
        spinnerAnimation.duration = Random.nextLong(700,1300)
        spinnerAnimation.repeatCount = 0

        Binding.progressBar.startAnimation(spinnerAnimation)
        Binding.progressBar.animation.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationStart(animation: Animation?) {
                Binding.progressBar.visibility = View.VISIBLE
            }
            override fun onAnimationEnd(animation: Animation?) {
                Binding.progressBar.clearAnimation()
                Binding.progressBar.visibility = View.INVISIBLE
                publishAnimation(Binding.imageView)
            }
            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    }
    private fun initializeCamera() {

        //changing vis of certain items.
        Binding.imageContainer.visibility = View.INVISIBLE
        Binding.captureButton.visibility = View.VISIBLE
        Binding.cameraPreview.visibility = View.VISIBLE

        tempOutputFile = getTempOutputFile()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCamera()
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    private fun bindCamera() {
        val cameraSelector: CameraSelector

        if(facingFront){
            Log.d(TAG,"switched to front")
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            facingFront = false
        }
        else{
            Log.d(TAG,"switched to back")
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            facingFront = true
        }

        val preview = Preview.Builder().build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        preview.setSurfaceProvider(Binding.cameraPreview.surfaceProvider)

        cameraProvider?.unbindAll()
        camera = cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageCapture)
    }
    private fun captureImageAction() {
        val imageCapture = imageCapture

        val imageOutputFile = File(
            tempOutputFile,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(imageOutputFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            onImageSavedCallback(imageOutputFile)
        )
    }
    private fun onImageSavedCallback(photoFile: File) =
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(e: ImageCaptureException) {
                Log.e(TAG, e.printStackTrace().toString())
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                Binding.captureButton.visibility = View.INVISIBLE
                Binding.cameraPreview.visibility = View.INVISIBLE
                Binding.imageContainer.visibility = View.VISIBLE

                capturedImageUri = photoFile.toUri()
                Binding.imageView.setImageURI(capturedImageUri)

                Log.d(TAG, "Image captured")
            }
        }
    private fun getTempOutputFile(): File {
        val mediaStoreDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(mediaStoreDir, "HotSpots").apply { mkdirs() }
    }
    private fun publishAnimation(view: View) {
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
        animatorSet.duration = 200

        // Listen for animation completion
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                Binding.publish.visibility = View.INVISIBLE
                Binding.exitImage.visibility = View.INVISIBLE
                Binding.flipCamera.visibility = View.INVISIBLE
            }
            override fun onAnimationEnd(animation: Animator) {
                navController.navigate(R.id.navigation_map)
            }
            override fun onAnimationCancel(animation: Animator) {

            }
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animatorSet.start()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val TAG = "CameraFragment"

    }
}