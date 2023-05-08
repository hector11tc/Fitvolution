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


class WorkoutFragment : Fragment() {
    private lateinit var workoutsAdapter: WorkoutsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        workoutsAdapter = WorkoutsAdapter(onDeleteButtonClick = { workoutId ->
            deleteWorkoutAndReload(workoutId)
        })
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_workouts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = workoutsAdapter

        val fabNewWorkout = view.findViewById<FloatingActionButton>(R.id.fab_new_workout)
        fabNewWorkout.setOnClickListener {
            val intent = Intent(activity, NewWorkoutActivity::class.java)
            startActivity(intent)
        }

        loadWorkoutsFromFirestore()
    }

    private fun loadWorkoutsFromFirestore() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("workouts")
            .whereEqualTo("user", firestore.collection("users").document(currentUser?.uid!!))
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d(TAG, "Workout documents retrieved: ${querySnapshot.documents.size}")

                val workoutsList = querySnapshot.documents.map { document ->
                    val workout = document.toObject(Workout::class.java)!!.copy(id = document.id)
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

                    workout.exercisesList = exercisesList
                    Log.d(TAG, "Workout ${workout.name} exercises count: ${workout.exercisesList.size}")
                    workout
                }
                Log.d(TAG, "Workouts retrieved: $workoutsList")
                workoutsAdapter.updateWorkouts(workoutsList)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting workouts", exception)
            }
    }

    private fun deleteWorkoutAndReload(workoutId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("workouts")
            .document(workoutId)
            .delete()
            .addOnSuccessListener {
                // Recargamos las rutinas de ejercicios
                loadWorkoutsFromFirestore()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error deleting workout", exception)
            }
    }
}







