package com.example.passwordmanager.documents

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R
import com.google.android.material.search.SearchBar
import java.util.UUID

class DocumentsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DocumentAdapter
    private lateinit var searchBar: SearchBar
    private lateinit var documentStorage: DocumentStorage
    private val documents = mutableListOf<Document>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_documents_fragment, container, false)

        recyclerView = view.findViewById(R.id.documentsRecyclerView)
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

    private fun openDocumentInfo(document: Document, cardView: View, titleView: TextView, thumbnails: List<ImageView>) {
        val intent = Intent(context, DocumentInfoActivity::class.java).apply {
            putExtra("DOCUMENT_ID", document.id)
        }

        val pairs = mutableListOf<Pair<View, String>>()
        pairs.add(Pair(cardView as View, "documentCard_${document.id}"))
        pairs.add(Pair(titleView as View, "documentTitle_${document.id}"))

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
        documents.addAll(documentStorage.getAllDocuments())
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
    val name: String,
    val fileNames: List<String>,
    val id: String = UUID.randomUUID().toString()
)