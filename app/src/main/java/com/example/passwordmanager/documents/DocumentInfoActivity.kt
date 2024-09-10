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
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocumentInfoActivity : AppCompatActivity(), SecuritySettingsListener {
    private lateinit var titleTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var secureStorage: SecureImageStorage
    private lateinit var documentStorage: DocumentStorage
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var cardView: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.applyTheme(this)
        setContentView(R.layout.activity_document_info)

        ScreenshotProtectionHelper.applyScreenshotProtection(this)
        (application as MyApp).addSecuritySettingsListener(this)

        setupWindowAnimations()

        postponeEnterTransition()

        titleTextView = findViewById(R.id.documentTitleTextView)
        recyclerView = findViewById(R.id.imagesRecyclerView)
        cardView = findViewById(R.id.documentCardView)

        secureStorage = SecureImageStorage(this)
        documentStorage = DocumentStorage(this)
        preferencesManager = PreferencesManager(this)

        val documentId = intent.getStringExtra("DOCUMENT_ID")
        val documentName = intent.getStringExtra("DOCUMENT_NAME")
        val imageUriStrings = intent.getStringArrayListExtra("IMAGE_URIS")

        if (documentId != null && documentName != null && imageUriStrings != null) {
            ViewCompat.setTransitionName(cardView, "documentCard_$documentId")
            ViewCompat.setTransitionName(titleTextView, "documentTitle_$documentId")

            titleTextView.text = documentName

            setupRecyclerView()

            val imageUris = imageUriStrings.map { Uri.parse(it) }
            loadDocument(documentId, imageUris)
        } else {
            showErrorAndFinish()
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
        imageAdapter.submitList(imageUris)
        recyclerView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                recyclerView.viewTreeObserver.removeOnPreDrawListener(this)
                for (i in 0 until recyclerView.childCount) {
                    val view = recyclerView.getChildAt(i)
                    ViewCompat.setTransitionName(view, "documentImage_${documentId}_$i")
                }
                startPostponedEnterTransition()
                return true
            }
        })
    }

    private fun displayDocument(document: Document) {
        titleTextView.text = document.name
        imageAdapter.submitList(document.imageUris)
        startPostponedEnterTransition()
    }

    private fun showErrorAndFinish() {
        Toast.makeText(this, "Error loading document", Toast.LENGTH_LONG).show()
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
        finish()
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
            duration = 300
        }
        window.sharedElementEnterTransition = transition
        window.sharedElementReturnTransition = transition
    }
}