package com.example.passwordmanager.documents

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Explode
import android.transition.TransitionSet
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.AuthenticationActivity
import com.example.passwordmanager.FullImageActivity
import com.example.passwordmanager.R
import com.example.passwordmanager.SecureImageStorage
import com.example.passwordmanager.ImageAdapter
import com.example.passwordmanager.MyApp
import com.example.passwordmanager.PinSetupActivity
import com.example.passwordmanager.PreferencesManager
import com.example.passwordmanager.ScreenshotProtectionHelper
import com.example.passwordmanager.SecuritySettingsListener
import com.example.passwordmanager.ThemeHelper

class DocumentInfoActivity : AppCompatActivity(), SecuritySettingsListener {
    private lateinit var titleTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var secureStorage: SecureImageStorage
    private lateinit var documentStorage: DocumentStorage
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var cardView: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_info)

        ScreenshotProtectionHelper.applyScreenshotProtection(this)
        (application as MyApp).addSecuritySettingsListener(this)

        setupWindowAnimations()

        window.enterTransition = Explode()

        postponeEnterTransition()

        titleTextView = findViewById(R.id.documentTitleTextView)
        recyclerView = findViewById(R.id.imagesRecyclerView)
        cardView = findViewById(R.id.documentCardView)

        secureStorage = SecureImageStorage(this)
        documentStorage = DocumentStorage(this)
        preferencesManager = PreferencesManager(this)

        val documentId = intent.getStringExtra("DOCUMENT_ID")
        if (documentId != null) {
            ViewCompat.setTransitionName(cardView, "documentCard_$documentId")
            ViewCompat.setTransitionName(titleTextView, "documentTitle_$documentId")
        }
        val imageUris = intent.getParcelableArrayListExtra<Uri>("IMAGE_URIS")

        recyclerView = findViewById(R.id.imagesRecyclerView)
        setupRecyclerView()

        if (documentId != null && imageUris != null) {
            loadDocument(documentId, imageUris)
        } else {
            // Обработка ошибки
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        imageAdapter = ImageAdapter { uri, view ->
            val intent = Intent(this, FullImageActivity::class.java).apply {
                putExtra("imageUri", uri.toString())
                putExtra("transitionName", view.transitionName)
            }
            val options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                view,
                view.transitionName
            )
            startActivity(intent, options.toBundle())
        }
        recyclerView.adapter = imageAdapter

    }

    private fun loadDocument(documentId: String, imageUris: List<Uri>) {
        val document = documentStorage.getDocumentById(documentId)
        if (document != null) {
            titleTextView.text = document.name
            imageAdapter.submitList(imageUris)

            recyclerView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    recyclerView.viewTreeObserver.removeOnPreDrawListener(this)
                    for (i in 0 until recyclerView.childCount) {
                        val view = recyclerView.getChildAt(i)
                        ViewCompat.setTransitionName(view, "documentImage_${document.id}_$i")
                    }
                    startPostponedEnterTransition()
                    return true
                }
            })
        } else {
            // Обработка ошибки
        }
    }

    private fun displayDocument(document: Document) {
        titleTextView.text = document.name
        loadImages(document)
    }

    private fun loadImages(document: Document) {
        val imageUris = document.fileNames.mapNotNull { fileName ->
            secureStorage.getDecryptedImage(fileName)?.let { Uri.fromFile(it) }
        }
        imageAdapter.submitList(imageUris)

        // Set transition names for images
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                for (i in 0 until recyclerView.childCount) {
                    val view = recyclerView.getChildAt(i)
                    ViewCompat.setTransitionName(view, "documentImage_${document.id}_$i")
                    Log.d("DocumentsFragment", "DocumentInfoActivity Added transition name for image documentImage_${document.id}_$i")
                }
            }
        })
    }

    private fun showErrorAndFinish() {
        Toast.makeText(this, "error", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (!preferencesManager.isAuthenticated()) {
            startAuthenticationActivity()
        }
    }

    private fun startAuthenticationActivity() {
        val intent = if (preferencesManager.isPinSet()) {
            Intent(this, AuthenticationActivity::class.java)
        } else {
            Intent(this, PinSetupActivity::class.java)
        }
        startActivity(intent)
        finish()  // Закрываем DocumentInfoActivity, чтобы пользователь не мог вернуться к ней без аутентификации
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as MyApp).removeSecuritySettingsListener(this)
    }

    override fun onSecuritySettingsChanged() {
        ScreenshotProtectionHelper.applyScreenshotProtection(this)
    }

    private fun setupWindowAnimations() {
        val transition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeImageTransform())
            duration = 300 // или другое подходящее значение
        }
        window.sharedElementEnterTransition = transition
        window.sharedElementReturnTransition = transition
    }
}