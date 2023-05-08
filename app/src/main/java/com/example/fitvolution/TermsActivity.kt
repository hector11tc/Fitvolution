 package com.example.fitvolution

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

 class TermsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        val toolbar = findViewById<Toolbar>(R.id.terms_toolbar)
        setSupportActionBar(toolbar)


        val backButton = findViewById<Button>(R.id.back_login)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

}

