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




// Fragmento que muestra la lista de ejercicios con un filtro de grupo
class ExercisesFragment : Fragment() {

    private lateinit var exercisesAdapter: ExerciseAdapter
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var groupSpinner: Spinner
    private var exerciseList: List<Exercise> = emptyList()

    // Método para inflar la vista y configurar el RecyclerView y el Spinner
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercises, container, false)

        groupSpinner = view.findViewById(R.id.spinner_group)
        exercisesRecyclerView = view.findViewById(R.id.recycler_view_exercises)

        // Inicializar el adaptador y configurar el RecyclerView
        exercisesAdapter = ExerciseAdapter(mutableListOf())
        exercisesRecyclerView.adapter = exercisesAdapter
        exercisesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Obtener los ejercicios de la base de datos
        fetchExercises()

        // Configurar el adaptador del Spinner y su evento OnItemSelectedListener
        val spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.exercise_groups, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        groupSpinner.adapter = spinnerAdapter

        groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // Filtrar la lista de ejercicios cuando se selecciona un grupo en el Spinner
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroup = parent?.getItemAtPosition(position).toString()
                filterExercises(selectedGroup)
            }

            // No hacer nada si no se selecciona ningún elemento en el Spinner
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        return view
    }

    // Método para obtener la lista de ejercicios de Firestore
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
                // Filtrar la lista de ejercicios según el grupo seleccionado en el Spinner
                filterExercises(groupSpinner.selectedItem.toString())
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    // Método para filtrar la lista de ejercicios según el grupo seleccionado
    private fun filterExercises(selectedGroup: String) {
        val filteredExercises = if (selectedGroup == "Show all") {
            exerciseList
        } else {
            exerciseList.filter { it.group == selectedGroup }
        }
        // Actualizar el adaptador del RecyclerView con los ejercicios filtrados
        exercisesAdapter.updateExercises(filteredExercises)
    }

    companion object {
        private const val TAG = "ExercisesFragment"
    }
}
