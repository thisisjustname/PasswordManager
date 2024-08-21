package com.example.passwordmanager.documents

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R
import com.example.passwordmanager.SecureImageStorage
import com.example.passwordmanager.ImageAdapter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddDocumentDialogFragment : DialogFragment() {
    private lateinit var documentNameInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var btnGallery: Button
    private lateinit var btnCamera: Button
    private lateinit var btnAdd: Button
    private lateinit var btnCancel: Button

    private lateinit var secureStorage: SecureImageStorage
    private lateinit var documentStorage: DocumentStorage


    private val selectedImages = mutableListOf<Uri>()
    private var currentPhotoUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImages.add(it)
            updateRecyclerView()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            currentPhotoUri?.let { uri ->
                selectedImages.add(uri)
                updateRecyclerView()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_add_document, container, false)

        documentNameInput = view.findViewById(R.id.documentNameInput)
        recyclerView = view.findViewById(R.id.recyclerView)
        btnGallery = view.findViewById(R.id.btnGallery)
        btnCamera = view.findViewById(R.id.btnCamera)
        btnAdd = view.findViewById(R.id.btnAdd)
        btnCancel = view.findViewById(R.id.btnCancel)

        secureStorage = SecureImageStorage(requireContext())
        documentStorage = DocumentStorage(requireContext())

        secureStorage = SecureImageStorage(requireContext())

        setupRecyclerView()
        setupButtons()

        return view
    }


    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        imageAdapter = ImageAdapter { uri, view ->
            // Обработка нажатия на изображение, если необходимо
        }
        recyclerView.adapter = imageAdapter
    }

    private fun addImageToSelection(uri: Uri) {
        selectedImages.add(uri)
        updateRecyclerView()
    }

    private fun setupButtons() {
        btnGallery.setOnClickListener { openGallery() }
        btnCamera.setOnClickListener { takePhoto() }
        btnAdd.setOnClickListener { addDocument() }
        btnCancel.setOnClickListener { dismiss() }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun takePhoto() {
        val photoFile = createImageFile()
        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().applicationContext.packageName}.fileprovider",
                it
            )
            currentPhotoUri = photoURI
            cameraLauncher.launch(photoURI)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(null)
        return try {
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        } catch (ex: Exception) {
            null
        }
    }

    private fun updateRecyclerView() {
        imageAdapter.submitList(selectedImages.toList())
    }

    private fun addDocument() {
        val documentName = documentNameInput.text.toString()
        if (documentName.isNotEmpty() && selectedImages.isNotEmpty()) {
            val savedFileNames = secureStorage.saveEncryptedImages(selectedImages)
            val newDocument = Document(
                name = documentName,
                fileNames = savedFileNames,
                id = UUID.randomUUID().toString()
            )
            documentStorage.saveDocument(newDocument)
            (parentFragment as? DocumentsFragment)?.onDocumentAdded(newDocument)
            dismiss()
            Toast.makeText(context, "Document added successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Please enter a document name and select at least one image", Toast.LENGTH_SHORT).show()
        }
    }

}