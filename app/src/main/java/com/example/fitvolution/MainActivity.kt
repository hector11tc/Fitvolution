package com.example.fitvolution


import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView


class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("navigate_to_workout_fragment", false)) {
            sharedPreferences.edit().remove("navigate_to_workout_fragment").apply()
            showWorkoutFragment()
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavigationView.setOnItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment
        val itemId = item.itemId
        fragment = when (itemId) {
            R.id.running_item -> {
                RunningFragment()
            }
            R.id.workout_item -> {
                WorkoutFragment()
            }
            R.id.exercises_item -> {
                ExercisesFragment()
            }
            else -> {
                ProfileFragment()
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_fragment, fragment)
            .commit()

        updateToolbarTitle(fragment)

        return true
    }

    private fun updateToolbarTitle(fragment: Fragment) {
        val title = when (fragment) {
            is RunningFragment -> "Running"
            is WorkoutFragment -> "Workout"
            is ExercisesFragment -> "Exercises"
            is ProfileFragment -> "Profile"
            else -> "Fitvolution"
        }
        supportActionBar?.title = title
    }

    private fun showWorkoutFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.running_fragment, WorkoutFragment())
        transaction.commit()
    }
}
