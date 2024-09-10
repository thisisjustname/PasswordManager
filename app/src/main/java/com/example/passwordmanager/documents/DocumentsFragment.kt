package com.example.passwordmanager.documents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R
import com.example.passwordmanager.SecureImageStorage
import com.google.android.material.search.SearchBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.util.Pair

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
            openDocumentInfo(document, cardView, titleView, thumbnails)
        }
        recyclerView.adapter = adapter
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
        viewLifecycleOwner.lifecycleScope.launch {
            val loadedDocuments = withContext(Dispatchers.IO) {
                documentStorage.getAllDocuments().map { document ->
                    val imageUris = document.fileNames.mapNotNull { fileName ->
                        secureStorage.getDecryptedImage(fileName)?.let { Uri.fromFile(it) }
                    }
                    document.copy(imageUris = imageUris)
                }
            }
            documents.clear()
            documents.addAll(loadedDocuments)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showAddDocumentDialog() {
        val dialog = AddDocumentDialogFragment()
        dialog.show(childFragmentManager, "AddDocumentDialog")
    }

    private fun openDocumentInfo(document: Document, cardView: View, titleView: TextView, thumbnails: List<ImageView>) {
        val intent = Intent(context, DocumentInfoActivity::class.java).apply {
            putExtra("DOCUMENT_ID", document.id)
            putExtra("DOCUMENT_NAME", document.name)
            putStringArrayListExtra("IMAGE_URIS", ArrayList(document.imageUris.map { it.toString() }))
        }

        val pairs = mutableListOf<Pair<View, String>>()
        pairs.add(Pair(cardView, "documentCard_${document.id}"))
        pairs.add(Pair(titleView, "documentTitle_${document.id}"))

        thumbnails.forEachIndexed { index, imageView ->
            if (imageView.visibility == View.VISIBLE) {
                pairs.add(Pair(imageView as View, "documentImage_${document.id}_$index"))
            }
        }

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            *pairs.toTypedArray()
        )

        startActivity(intent, options.toBundle())
    }

    fun onDocumentAdded(newDocument: Document) {
        viewLifecycleOwner.lifecycleScope.launch {
            val updatedDocument = withContext(Dispatchers.IO) {
                val imageUris = newDocument.fileNames.mapNotNull { fileName ->
                    secureStorage.getDecryptedImage(fileName)?.let { Uri.fromFile(it) }
                }
                newDocument.copy(imageUris = imageUris)
            }
            documents.add(updatedDocument)
            adapter.notifyItemInserted(documents.size - 1)
        }
    }
}

data class Document(
    val id: String,
    val name: String,
    val fileNames: List<String>,
    var imageUris: List<Uri> = emptyList()
)