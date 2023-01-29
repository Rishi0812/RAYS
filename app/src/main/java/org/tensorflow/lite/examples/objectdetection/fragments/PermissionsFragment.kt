package org.tensorflow.lite.examples.objectdetection.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import org.tensorflow.lite.examples.objectdetection.R

private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

/**
 * The sole purpose of this fragment is to request permissions and, once granted, display the
 * camera fragment to the user.
 */
class PermissionsFragment : Fragment() {

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(context, "Camera permission request granted", Toast.LENGTH_LONG).show()
                requestMicrophonePermission()
            } else {
                Toast.makeText(context, "Camera permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private val requestMicrophonePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(context, "Microphone permission request granted", Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(context, "Microphone permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestMicrophonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    public fun navigateToCamera() {
        lifecycleScope.launchWhenStarted {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                PermissionsFragmentDirections.actionPermissionsToCamera())
        }
    }

    public fun navigateBack() {
        val navController = Navigation.findNavController(requireActivity(), R.id.fragment_container)
        navController.navigate(R.id.action_camera_to_permissions)
    }


    companion object {

        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
