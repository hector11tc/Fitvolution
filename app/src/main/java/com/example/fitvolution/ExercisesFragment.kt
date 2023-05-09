package com.example.fitvolution

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject




// Excerpt showing the list of exercises with a group filter
class ExercisesFragment : Fragment() {

    private lateinit var exercisesAdapter: ExerciseAdapter
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var groupSpinner: Spinner
    private var exerciseList: List<Exercise> = emptyList()

    // Method for inflating the view and configuring the RecyclerView and the Spinner
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercises, container, false)

        groupSpinner = view.findViewById(R.id.spinner_group)
        exercisesRecyclerView = view.findViewById(R.id.recycler_view_exercises)

        // Initialising the adapter and configuring the RecyclerView
        exercisesAdapter = ExerciseAdapter(mutableListOf())
        exercisesRecyclerView.adapter = exercisesAdapter
        exercisesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Get the exercises from the database
        fetchExercises()

        // Configuring the Spinner adapter and its OnItemSelectedListener event
        val spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.exercise_groups, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        groupSpinner.adapter = spinnerAdapter

        groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // Filter the list of exercises when selecting a group in the Spinner
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroup = parent?.getItemAtPosition(position).toString()
                filterExercises(selectedGroup)
            }

            // Do nothing if no element is selected in the Spinner.
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        return view
    }

    // Method to obtain the Firestore exercise list
    private fun fetchExercises() {
        val db = FirebaseFirestore.getInstance()
        db.collection("exercises")
            .get()
            .addOnSuccessListener { documents ->
                val exercises = mutableListOf<Exercise>()
                for (document in documents) {
                    val exercise = document.toObject(Exercise::class.java)
                    exercises.add(exercise)
                }
                exerciseList = exercises
                // Filter the list of exercises according to the selected group in the Spinner
                filterExercises(groupSpinner.selectedItem.toString())
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    // Method to filter the list of exercises according to the selected group
    private fun filterExercises(selectedGroup: String) {
        val filteredExercises = if (selectedGroup == "Show all") {
            exerciseList
        } else {
            exerciseList.filter { it.group == selectedGroup }
        }
        // Update RecyclerView adapter with filtered exercises
        exercisesAdapter.updateExercises(filteredExercises)
    }

    companion object {
        private const val TAG = "ExercisesFragment"
    }
}
