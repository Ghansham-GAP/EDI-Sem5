package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class HistoryItem(val imagePath: String, val result: String, val date: String, val time: String)

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_history)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val historyRecyclerView = findViewById<RecyclerView>(R.id.history_recycler_view)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)

        val historyAdapter = HistoryAdapter(MainActivity.historyList)
        historyRecyclerView.adapter = historyAdapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
