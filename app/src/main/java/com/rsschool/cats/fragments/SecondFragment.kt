package com.rsschool.cats.fragments

import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.rsschool.cats.R
import com.rsschool.cats.databinding.FragmentSecondBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import coil.api.load


class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        val id = arguments?.getString("id")
        binding.catIdTextView.text = String.format(getString(R.string.cat_item_id),id)
        val imageURL = arguments?.getString("image")
        binding.catImageView.load(imageURL)

        //For fixing perspective distortion in card flip animation
        val scale = requireContext().resources.displayMetrics.density
        binding.root.cameraDistance = 8000*scale

        /*
        //For sharedElement transitions
        binding.catTextView.transitionName = number // argument val, as imageURL
        binding.catImageView.transitionName = imageURL
        sharedElementEnterTransition = TransitionInflater.from(this.context).inflateTransition(R.transition.change_bounds)
        sharedElementReturnTransition = TransitionInflater.from(this.context).inflateTransition(R.transition.change_bounds)
        */

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*
        //For sharedElement transitions
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true ) {
                override fun handleOnBackPressed() {
                    val extras = FragmentNavigatorExtras(
                        binding.catTextView to binding.catTextView.transitionName,
                        binding.catImageView to binding.catImageView.transitionName
                    )
                    //Warning: recyclerview state not saved (?)
                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment, null, null, extras)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
         */
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_image -> {
                if (binding.catImageView.drawable != null) {
                    val bitmap = binding.catImageView.drawable.toBitmap()

                    if (isCallPermissionGranted()) {
                        saveImage(bitmap)
                    } else {
                        requestCallPermissions()
                        if (isCallPermissionGranted())
                            saveImage(bitmap)
                    }
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun isCallPermissionGranted(): Boolean{
        return ContextCompat.checkSelfPermission(requireActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    private fun requestCallPermissions (){
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE_CODE)
    }
    @Suppress("DEPRECATION")
    private fun saveImage(bitmap:Bitmap){
        var imageOutStream: OutputStream? = null
        var savePath: File? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "${arguments?.getString("id")}.jpg")
                put(MediaStore.MediaColumns.DISPLAY_NAME, "${arguments?.getString("id")}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES
                    + File.separator + getString(R.string.cats_pictures_folder))
            }

            val uri =
                context?.contentResolver?.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )

            if (uri != null) {
                imageOutStream = context?.contentResolver?.openOutputStream(uri)
            }
        } else {
            val saveDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/" + getString(R.string.cats_pictures_folder)
            )
            if (!saveDir.exists()) {
                saveDir.mkdirs()
            }
            val fileName = "${arguments?.getString("id")}.jpg"
            savePath = File(saveDir, fileName)

            imageOutStream = FileOutputStream(savePath)
        }

        var resultMes = ""
        GlobalScope.launch(Dispatchers.IO) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                resultMes = getString(R.string.cats_not_saved_toast)
                throw IOException("Failed to save bitmap.")
            }
            else resultMes = getString(R.string.cats_saved_toast)
            imageOutStream?.flush()
            imageOutStream?.close()
        }.invokeOnCompletion {
            requireActivity().runOnUiThread {
                Toast.makeText(requireActivity(), resultMes, Toast.LENGTH_SHORT)
                    .show()
            }
            if (savePath != null)
            //Pre Q, fixing scan delay in Gallery apps
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(savePath.toString()),
                    null,
                    null
                )
        }
    }

    private companion object{
        private const val REQUEST_EXTERNAL_STORAGE_CODE = 100
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
