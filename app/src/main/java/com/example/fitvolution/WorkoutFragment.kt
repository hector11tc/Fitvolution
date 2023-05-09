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


// Esta es la clase de fragmento que maneja la pantalla de la lista de rutinas de entrenamiento
class WorkoutFragment : Fragment() {

    // Referencia al adaptador que maneja la lista de rutinas de entrenamiento en el RecyclerView
    private lateinit var workoutsAdapter: WorkoutsAdapter

    // onCreateView es llamada para inflar el layout del fragmento
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    // onResume es llamado cuando el fragmento está listo para interactuar con el usuario
    // En este caso, se utiliza para recargar la lista de rutinas de entrenamiento cada vez que el fragmento se muestra
    override fun onResume() {
        super.onResume()
        loadWorkoutsFromFirestore() // Recargar la lista de workouts
    }

    // onViewCreated es llamada después de que la vista del fragmento se ha creado
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos el adaptador de rutinas de entrenamiento con las funciones que se llamarán cuando se haga clic en los botones de eliminar y favorito
        workoutsAdapter = WorkoutsAdapter(
            onDeleteButtonClick = { workoutId ->
                deleteWorkoutAndReload(workoutId)
            },
            onFavouriteButtonClick = { workout ->
                toggleFavouriteAndUpdateFirestore(workout)
            }
        )

        // Configuramos el RecyclerView con el adaptador de rutinas de entrenamiento y un LinearLayoutManager
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_workouts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = workoutsAdapter

        // Configuramos el botón de agregar nueva rutina para iniciar NewWorkoutActivity cuando se haga clic en él
        val fabNewWorkout = view.findViewById<FloatingActionButton>(R.id.fab_new_workout)
        fabNewWorkout.setOnClickListener {
            val intent = Intent(activity, NewWorkoutActivity::class.java)
            startActivity(intent)
        }

        // Cargamos las rutinas de entrenamiento de Firestore
        loadWorkoutsFromFirestore()
    }

    // Esta función carga las rutinas de entrenamiento del usuario actual de Firestore
    private fun loadWorkoutsFromFirestore() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val firestore = FirebaseFirestore.getInstance()

        // Realizamos una consulta a Firestore para obtener las rutinas de entrenamiento del usuario actual
        firestore.collection("workouts")
            .whereEqualTo("user", firestore.collection("users").document(currentUser?.uid!!))
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d(TAG, "Workout documents retrieved: ${querySnapshot.documents.size}")

                // Transformamos los documentos de Firestore en objetos Workout y los añadimos a una lista
                val workoutsList = querySnapshot.documents.map { document ->
                    val workout = document.toObject(Workout::class.java)!!.copy(id = document.id)

                    // Para cada rutina de entrenamiento, cargamos sus ejercicios de Firestore
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

                    // Actualizamos la lista de ejercicios de la rutina
                    workout.exercisesList = exercisesList
                    Log.d(TAG, "Workout ${workout.name} exercises count: ${workout.exercisesList.size}")
                    workout
                }

                // Ordenamos las rutinas de entrenamiento para que las favoritas aparezcan primero
                val sortedWorkouts = workoutsList.sortedByDescending { it.favourite }
                Log.d(TAG, "Workouts retrieved: $sortedWorkouts")

                // Actualizamos el adaptador del RecyclerView con las nuevas rutinas de entrenamiento
                workoutsAdapter.updateWorkouts(sortedWorkouts)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting workouts", exception)
            }
    }

    // Esta función elimina una rutina de entrenamiento de Firestore y recarga la lista
    private fun deleteWorkoutAndReload(workoutId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Eliminamos la rutina de entrenamiento de Firestore
        firestore.collection("workouts")
            .document(workoutId)
            .delete()
            .addOnSuccessListener {
                // Recargamos las rutinas de entrenamiento
                loadWorkoutsFromFirestore()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error deleting workout", exception)
            }
    }

    // Esta función cambia el estado de favorito de una rutina de entrenamiento en Firestore y recarga la lista
    private fun toggleFavouriteAndUpdateFirestore(workout: Workout) {
        val firestore = FirebaseFirestore.getInstance()

        // Actualizamos el estado de favorito de la rutina de entrenamiento en Firestore
        firestore.collection("workouts")
            .document(workout.id)
            .update("favourite", !workout.favourite)
            .addOnSuccessListener {
                // Recargamos las rutinas de entrenamiento
                loadWorkoutsFromFirestore()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error updating favourite status", exception)
            }
    }
}








