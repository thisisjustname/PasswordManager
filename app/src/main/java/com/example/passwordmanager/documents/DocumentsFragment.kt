package com.example.passwordmanager.documents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R

class DocumentsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DocumentAdapter
    private val documents = mutableListOf<Document>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_documents_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.documentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = DocumentAdapter(documents)
        recyclerView.adapter = adapter
    }
}

data class Document(
    val name: String,
    val photoResId: Int
)