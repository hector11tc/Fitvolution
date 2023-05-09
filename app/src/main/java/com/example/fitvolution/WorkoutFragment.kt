package com.example.fitvolution

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


// This is the kind of fragment that handles the display of the list of training routines.
class  WorkoutFragment : Fragment() {

    // Reference to the adapter that handles the list of training routines in the RecyclerView
    private lateinit var workoutsAdapter: WorkoutsAdapter

    // onCreateView is called to inflate the fragment's layout.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    // onResume is called when the fragment is ready to interact with the user.
    // In this case, it is used to reload the list of training routines each time the snippet is displayed.
    override fun onResume() {
        super.onResume()
        loadWorkoutsFromFirestore() // Reload workout list
    }

    // onViewCreated is called after the fragment's view has been created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // We initialise the training routine adapter with the functions that will be called when the delete and favourite buttons are clicked
        workoutsAdapter = WorkoutsAdapter(
            onDeleteButtonClick = { workoutId ->
                deleteWorkoutAndReload(workoutId)
            },
            onFavouriteButtonClick = { workout ->
                toggleFavouriteAndUpdateFirestore(workout)
            }
        )

        // We set up the RecyclerView with the training routine adapter and a LinearLayoutManager.
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_workouts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = workoutsAdapter

        // Set the add new routine button to start NewWorkoutActivity when clicked on
        val fabNewWorkout = view.findViewById<FloatingActionButton>(R.id.fab_new_workout)
        fabNewWorkout.setOnClickListener {
            val intent = Intent(activity, NewWorkoutActivity::class.java)
            startActivity(intent)
        }

        // We upload Firestore training routines
        loadWorkoutsFromFirestore()
    }

    // This function loads the training routines of the current Firestore user.
    private fun loadWorkoutsFromFirestore() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val firestore = FirebaseFirestore.getInstance()

        // We query Firestore to obtain the current user's training routines.
        firestore.collection("workouts")
            .whereEqualTo("user", firestore.collection("users").document(currentUser?.uid!!))
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d(TAG, "Workout documents retrieved: ${querySnapshot.documents.size}")

                // We transform Firestore documents into Workout objects and add them to a list.
                val workoutsList = querySnapshot.documents.map { document ->
                    val workout = document.toObject(Workout::class.java)!!.copy(id = document.id)

                    // For each training routine, we load your exercises from Firestore
                    val exercisesRefList = workout.exercises
                    val exercisesList = mutableListOf<WtExercise>()
                    val deferredExercises = exercisesRefList.map { exerciseRef ->
                        GlobalScope.async(Dispatchers.IO) {
                            val exerciseDocument = exerciseRef.get().await()
                            val exercise = exerciseDocument.toObject(WtExercise::class.java)!!
                            exercisesList.add(exercise)
                        }
                    }
                    runBlocking {
                        deferredExercises.awaitAll()
                    }

                    // We update the list of exercises in the routine
                    workout.exercisesList = exercisesList
                    Log.d(TAG, "Workout ${workout.name} exercises count: ${workout.exercisesList.size}")
                    workout
                }

                // We order the training routines so that the favourites appear first.
                val sortedWorkouts = workoutsList.sortedByDescending { it.favourite }
                Log.d(TAG, "Workouts retrieved: $sortedWorkouts")

                // We updated the RecyclerView adapter with the new training routines.
                workoutsAdapter.updateWorkouts(sortedWorkouts)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting workouts", exception)
            }
    }

    // This function removes a training routine from the Firestore and reloads the list.
    private fun deleteWorkoutAndReload(workoutId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // We removed the training routine from Firestore
        firestore.collection("workouts")
            .document(workoutId)
            .delete()
            .addOnSuccessListener {
                loadWorkoutsFromFirestore()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error deleting workout", exception)
            }
    }

    // This function changes the favourite status of a training routine in Firestore and reloads the list.
    private fun toggleFavouriteAndUpdateFirestore(workout: Workout) {
        val firestore = FirebaseFirestore.getInstance()

        // We updated the favourite status of the Firestore training routine.
        firestore.collection("workouts")
            .document(workout.id)
            .update("favourite", !workout.favourite)
            .addOnSuccessListener {
                loadWorkoutsFromFirestore()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error updating favourite status", exception)
            }
    }
}








