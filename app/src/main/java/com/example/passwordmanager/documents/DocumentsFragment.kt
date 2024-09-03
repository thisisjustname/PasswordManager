package com.example.passwordmanager.documents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Explode
import android.transition.TransitionSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R
import com.example.passwordmanager.SecureImageStorage
import com.google.android.material.search.SearchBar
import java.util.UUID

class DocumentsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DocumentAdapter
    private lateinit var searchBar: SearchBar
    private lateinit var documentStorage: DocumentStorage
    private lateinit var secureStorage: SecureImageStorage
    private val documents = mutableListOf<Document>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_documents_fragment, container, false)

        recyclerView = view.findViewById(R.id.documentsRecyclerView)
        secureStorage = SecureImageStorage(requireContext())
        searchBar = view.findViewById(R.id.searchBar)
        documentStorage = DocumentStorage(requireContext())

        setupRecyclerView()
        setupSearchBar()
        loadDocuments()

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = DocumentAdapter(documents) { document, cardView, titleView, thumbnails ->
            // Устанавливаем transition names здесь
            ViewCompat.setTransitionName(cardView, "documentCard_${document.id}")
            ViewCompat.setTransitionName(titleView, "documentTitle_${document.id}")
            thumbnails.forEachIndexed { index, imageView ->
                if (imageView.visibility == View.VISIBLE) {
                    ViewCompat.setTransitionName(imageView, "documentImage_${document.id}_$index")
                }
            }
            openDocumentInfo(document, cardView, titleView, thumbnails)
        }
        recyclerView.adapter = adapter
    }

    private fun openDocumentInfo(document: Document, cardView: View, titleView: TextView, thumbnails: List<ImageView>) {
        val intent = Intent(context, DocumentInfoActivity::class.java).apply {
            putExtra("DOCUMENT_ID", document.id)
            putParcelableArrayListExtra("IMAGE_URIS", ArrayList(document.imageUris))
        }

        val pairs = mutableListOf<Pair<View, String>>()
        pairs.add(Pair(cardView, ViewCompat.getTransitionName(cardView) ?: ""))
        pairs.add(Pair(titleView, ViewCompat.getTransitionName(titleView) ?: ""))

        thumbnails.forEachIndexed { index, imageView ->
            if (imageView.visibility == View.VISIBLE) {
                pairs.add(Pair(imageView as View, ViewCompat.getTransitionName(imageView) ?: ""))
            }
        }


        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            *pairs.toTypedArray()
        )

        thumbnails.forEachIndexed { index, imageView ->
            if (imageView.visibility == View.VISIBLE) {
                val transitionName = ViewCompat.getTransitionName(imageView)
                Log.d("Transition", "Sending shared element: $transitionName, isAttached: ${imageView.isAttachedToWindow}")
                pairs.add(Pair(imageView as View, transitionName ?: ""))
            }
        }

        requireActivity().window.exitTransition = Explode()

        startActivity(intent, options.toBundle())
    }

    private fun setupSearchBar() {
        searchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add -> {
                    showAddDocumentDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadDocuments() {
        documents.clear()
        val loadedDocuments = documentStorage.getAllDocuments()
        loadedDocuments.forEach { document ->
            val imageUris = document.fileNames.mapNotNull { fileName ->
                secureStorage.getDecryptedImage(fileName)?.let { Uri.fromFile(it) }
            }
            documents.add(document.copy(imageUris = imageUris))
        }
        adapter.notifyDataSetChanged()
    }

    private fun showAddDocumentDialog() {
        val dialog = AddDocumentDialogFragment()
        dialog.show(childFragmentManager, "AddDocumentDialog")
    }

    private fun openDocumentInfo(document: Document) {
        val intent = Intent(context, DocumentInfoActivity::class.java)
        intent.putExtra("DOCUMENT_ID", document.id)
        startActivity(intent)
    }

    fun onDocumentAdded(newDocument: Document) {
        documents.add(newDocument)
        adapter.notifyItemInserted(documents.size - 1)
    }

}

data class Document(
    val id: String,
    val name: String,
    val fileNames: List<String>,
    var imageUris: List<Uri> = emptyList()
)