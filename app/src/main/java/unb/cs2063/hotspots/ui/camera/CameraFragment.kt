package unb.cs2063.hotspots.ui.camera

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageButton
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


class CameraFragment : Fragment() {

    private var binding: FragmentCameraBinding? = null
    private val Binding get() = binding!!
    private lateinit var capturedImageUri: Uri
    private lateinit var  navController: NavController
    private lateinit var imageCapture: ImageCapture
    private lateinit var outputDirectory: File
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var FireBaseUtil : FireBaseUtil = FireBaseUtil()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        navController = requireActivity().findNavController(R.id.nav_host_fragment_activity_main)
        val root: View = Binding.root


        //Camera Setup
        setupCamera()

        //take picture when button is clicked
        Binding.captureButton.setOnClickListener{
            takePhoto()
        }


        //Publish image to firebase ... animations, change to map once complete
        Binding.publish.setOnClickListener{
            FireBaseUtil.pushToFireBase(requireActivity(),capturedImageUri)
            Binding.publish.isClickable = false
            Binding.publish.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.button_disabled))
            Binding.progressBar.startAnimation(setupAnimations())
        }


        Binding.exitImage.setOnClickListener {
            hideImage()
        }

        return root
    }


    private fun setupCamera() {
        hideImage()
        outputDirectory = getOutputDirectory()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCamera()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCamera() {
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val preview = Preview.Builder().build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        preview.setSurfaceProvider(Binding.cameraPreview.surfaceProvider)

        cameraProvider?.unbindAll()
        camera = cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageCapture)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            onImageSavedCallback(photoFile)
        )
    }

    private fun onImageSavedCallback(photoFile: File) =
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Error: ${exc.message}")
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                hidePreview()
                capturedImageUri = photoFile.toUri()
                Binding.imageView.setImageURI(capturedImageUri)
                Log.i(TAG, "Image captured")
            }
        }

    private fun getOutputDirectory(): File {
        val mediaStoreDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(mediaStoreDir, "HotSpots").apply { mkdirs() }
    }

    //Does animations. Changes to Map page.
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
        animatorSet.duration = 200 // Set the animation duration in milliseconds

        // Listen for animation completion
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                Binding.publish.visibility = View.INVISIBLE
                Binding.exitImage.visibility = View.INVISIBLE
            }
            override fun onAnimationEnd(animation: Animator) {
                navController.navigate(R.id.navigation_map)
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}

        })

        animatorSet.start()
    }

    private fun hidePreview(){

        Binding.captureButton.visibility = View.INVISIBLE
        Binding.cameraPreview.visibility = View.INVISIBLE
        //container that has all the publish shit
        Binding.imageContainer.visibility = View.VISIBLE

    }

    private fun hideImage(){
        //container shit invisible
        Binding.imageContainer.visibility = View.INVISIBLE
        //preview visible
        Binding.captureButton.visibility = View.VISIBLE
        Binding.cameraPreview.visibility = View.VISIBLE

    }

    fun animateButton(view: ImageButton, animationDuration: Long, onClickAction: () -> Unit) {
        val animatorSet = AnimatorSet()

        // Scale down the button
        val scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.2f)
        val scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.2f)

        // Combine scale animations
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator)
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.duration = animationDuration

        // Listen for animation completion
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // Animation start callback (optional)
            }

            override fun onAnimationEnd(animation: Animator) {
                // Animation end callback
                //onClickAction()
            }

            override fun onAnimationCancel(animation: Animator) {
                // Animation cancel callback (optional)
            }

            override fun onAnimationRepeat(animation: Animator) {
                // Animation repeat callback (optional)
            }
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