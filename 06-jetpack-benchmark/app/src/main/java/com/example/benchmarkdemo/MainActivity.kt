package com.example.benchmarkdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_list_demo).setOnClickListener {
            startActivity(Intent(this, ListActivity::class.java))
        }

        // Небольшая задержка для демонстрации startup времени
        Thread.sleep(100)
    }
}