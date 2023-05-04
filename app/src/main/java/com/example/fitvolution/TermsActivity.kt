 package com.example.fitvolution

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

 class TermsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        val backButton = findViewById<Button>(R.id.back_login)
        backButton.setOnClickListener {
            onBackPressed()
        }
//        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
//        setSupportActionBar(toolbar)
//        toolbar.title=getString(R.string.terms_title)
    }

}

