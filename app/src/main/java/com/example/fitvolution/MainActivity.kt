package com.example.fitvolution


import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavigationView.setOnItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment
        val itemId = item.itemId
        fragment = if (itemId == R.id.workout_item) {
            WorkoutFragment()
        } else if (itemId == R.id.running_item) {
            RunningFragment()
        } else if (itemId == R.id.metrics_item) {
            MetricsFragment()
        } else {
            ProfileFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_fragment, fragment)
            .commit()
        return true
    }



}