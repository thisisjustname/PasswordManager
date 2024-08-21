package com.example.passwordmanager.documents

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.AuthenticationActivity
import com.example.passwordmanager.FullImageActivity
import com.example.passwordmanager.PinSetupActivity
import com.example.passwordmanager.PreferencesManager
import com.example.passwordmanager.R
import com.example.passwordmanager.SecureImageStorage
import com.example.passwordmanager.ImageAdapter

class DocumentInfoActivity : AppCompatActivity() {
    private lateinit var titleTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var secureStorage: SecureImageStorage
    private lateinit var documentStorage: DocumentStorage
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var cardView: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_info)

        // Отложим переход, пока не загрузим данные
        supportPostponeEnterTransition()

        titleTextView = findViewById(R.id.documentTitleTextView)
        recyclerView = findViewById(R.id.imagesRecyclerView)
        cardView = findViewById(R.id.documentCardView)  // Добавьте эту строку

        secureStorage = SecureImageStorage(this)
        documentStorage = DocumentStorage(this)
        preferencesManager = PreferencesManager(this)

        setupRecyclerView()

        val documentId = intent.getStringExtra("DOCUMENT_ID")
        if (documentId != null) {
            val document = loadDocument(documentId)
            if (document != null) {
                displayDocument(document)

                // Установим transition name для анимируемых элементов
                cardView.transitionName = "documentCard_${document.id}"
                titleTextView.transitionName = "documentTitle_${document.id}"

                // Запустим отложенный переход
                supportStartPostponedEnterTransition()
            } else {
                showErrorAndFinish()
            }
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

    private fun loadDocument(documentId: String): Document? {
        return try {
            documentStorage.getDocumentById(documentId)
        } catch (e: Exception) {
            Log.e("DocumentInfoActivity", "Error loading document: ${e.message}")
            null
        }
    }

    private fun displayDocument(document: Document) {
        titleTextView.text = document.name
        loadImages(document.fileNames)
    }

    private fun loadImages(fileNames: List<String>) {
        val uris = fileNames.mapNotNull { fileName ->
            try {
                secureStorage.getDecryptedImage(fileName)?.let { Uri.fromFile(it) }
            } catch (e: Exception) {
                Log.e("DocumentInfoActivity", "Error decrypting image $fileName: ${e.message}")
                null
            }
        }
        imageAdapter.submitList(uris)
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
}