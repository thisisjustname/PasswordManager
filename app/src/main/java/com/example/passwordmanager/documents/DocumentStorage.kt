package com.example.passwordmanager.documents

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DocumentStorage(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("DocumentStorage", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveDocument(document: Document) {
        val documents = getAllDocuments().toMutableList()
        documents.add(document)
        saveAllDocuments(documents)
    }

    fun getAllDocuments(): List<Document> {
        val json = sharedPreferences.getString("documents", null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<List<Document>>() {}.type)
        } else {
            emptyList()
        }
    }

    private fun saveAllDocuments(documents: List<Document>) {
        val json = gson.toJson(documents)
        sharedPreferences.edit().putString("documents", json).apply()
    }

    fun getDocumentById(id: String): Document? {
        return getAllDocuments().find { it.id == id }
    }

    fun updateDocument(updatedDocument: Document) {
        val documents = getAllDocuments().toMutableList()
        val index = documents.indexOfFirst { it.id == updatedDocument.id }
        if (index != -1) {
            documents[index] = updatedDocument
            saveAllDocuments(documents)
        }
    }

    fun deleteDocument(id: String) {
        val documents = getAllDocuments().toMutableList()
        documents.removeAll { it.id == id }
        saveAllDocuments(documents)
    }
}