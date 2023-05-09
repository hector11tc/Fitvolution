package com.example.fitvolution

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.auth.FirebaseAuth



// This activity allows the user to create a new training routine.
class NewWorkoutActivity : AppCompatActivity() {
    // Definition of instance variables (Views and the exercise list)
    private lateinit var workoutNameEditText: EditText
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var addExerciseButton: Button
    private lateinit var createWorkoutButton: Button

    private val exercisesList = mutableListOf<WtExercise>()
    private lateinit var wtExercisesAdapter: WtExerciseAdapter

    // onCreate function, executed when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_workout)

        // Toolbar configuration
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "New Workout"

        // Initialisation of views
        workoutNameEditText = findViewById(R.id.editText_workout_name)
        exercisesRecyclerView = findViewById(R.id.recyclerView_exercises)
        addExerciseButton = findViewById(R.id.button_add_exercise)
        createWorkoutButton = findViewById(R.id.button_create_workout)

        // Configuration of RecyclerView
        setupRecyclerView()

        // Listeners for buttons
        addExerciseButton.setOnClickListener {
            showAddExerciseDialog()
        }

        createWorkoutButton.setOnClickListener {
            saveWorkoutToFirestore()
        }
    }

    // Function to handle the selection of items in the toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Pressing the back button ends the current activity and returns to the previous one.
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Configuration of RecyclerView
    private fun setupRecyclerView() {
        wtExercisesAdapter = WtExerciseAdapter(exercisesList)
        exercisesRecyclerView.layoutManager = LinearLayoutManager(this)
        exercisesRecyclerView.adapter = wtExercisesAdapter
    }

    // Function to display the add exercise dialogue
    @SuppressLint("MissingInflatedId")
    private fun showAddExerciseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exercise, null)

        // Configuration of NumberPickers
        val seriesPicker = dialogView.findViewById<NumberPicker>(R.id.number_picker_series)
        seriesPicker.minValue = 1
        seriesPicker.maxValue = 10

        val repsPicker = dialogView.findViewById<NumberPicker>(R.id.number_picker_reps)
        repsPicker.minValue = 1
        repsPicker.maxValue = 50

        val weightPicker = dialogView.findViewById<NumberPicker>(R.id.number_picker_weight)
        weightPicker.minValue = 1
        weightPicker.maxValue = 200

        // Exercise group spinner configuration
        val groupSpinner: Spinner = dialogView.findViewById(R.id.spinner_group)
        val groupArray = resources.getStringArray(R.array.exercise_groups_no_show_all)
        val groupAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, groupArray)
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        groupSpinner.adapter = groupAdapter

        // Exercise spinner configuration
        val exerciseSpinner: Spinner = dialogView.findViewById(R.id.spinner_exercise)
        val exerciseAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf())
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exerciseSpinner.adapter = exerciseAdapter

        // Listen to the selection change in the group spinner and update the exercise spinner accordingly.
        groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroup = groupArray[position]
                val exerciseRef = FirebaseFirestore.getInstance().collection("exercises")
                exerciseRef.whereEqualTo("group", selectedGroup).get()
                    .addOnSuccessListener { querySnapshot ->
                        val exerciseNames = querySnapshot.documents.map { doc ->
                            doc.getString("name") ?: ""
                        }
                        exerciseAdapter.clear()
                        exerciseAdapter.addAll(exerciseNames)
                        exerciseAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting exercises", exception)
                    }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }

        // Create the AlertDialog to add exercise
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Add Exercise")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val exercise = WtExercise(
                    name = exerciseSpinner.selectedItem.toString(),
                    group = groupSpinner.selectedItem.toString(),
                    series = seriesPicker.value,
                    reps = repsPicker.value,
                    weight = weightPicker.value
                )
                exercisesList.add(exercise)
                wtExercisesAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()

        alertDialog.show()
    }

    // Function to save the training routine in Firestore
    private fun saveWorkoutToFirestore() {
        val workoutName = workoutNameEditText.text.toString()

        if (workoutName.isBlank() || exercisesList.isEmpty()) {
            // Display an error message if the routine name is empty or there are no exercises.
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            // Saving exercises in Firestore
            val wtExercisesRefs = exercisesList.map { exercise ->
                val docRef = FirebaseFirestore.getInstance().collection("wt_exercises").document()
                docRef.set(exercise).await()
                docRef
            }

            // Saving the routine in Firestore
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUser!!.uid)
            val workout = Workout(
                name = workoutName,
                user = userRef,
                exercises = wtExercisesRefs,
                favourite = false
            )

            FirebaseFirestore.getInstance().collection("workouts").add(workout).await()

            withContext(Dispatchers.Main) {
                Toast.makeText(this@NewWorkoutActivity, "New workout correctly created!", Toast.LENGTH_SHORT).show()

                // Redirect to routines fragment in MainActivity
                redirectToWorkoutFragment()
            }
        }
    }


    // Function to redirect to routine fragment
    private fun redirectToWorkoutFragment() {
        finish()
    }

}


