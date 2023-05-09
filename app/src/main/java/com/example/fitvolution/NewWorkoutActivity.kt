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



// Esta actividad permite al usuario crear una nueva rutina de entrenamiento
class NewWorkoutActivity : AppCompatActivity() {
    // Definición de variables de instancia (Views y la lista de ejercicios)
    private lateinit var workoutNameEditText: EditText
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var addExerciseButton: Button
    private lateinit var createWorkoutButton: Button

    private val exercisesList = mutableListOf<WtExercise>()
    private lateinit var wtExercisesAdapter: WtExerciseAdapter

    // Función onCreate, se ejecuta al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_workout)

        // Configuración de la barra de herramientas
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "New Workout"

        // Inicialización de las vistas
        workoutNameEditText = findViewById(R.id.editText_workout_name)
        exercisesRecyclerView = findViewById(R.id.recyclerView_exercises)
        addExerciseButton = findViewById(R.id.button_add_exercise)
        createWorkoutButton = findViewById(R.id.button_create_workout)

        // Configuración del RecyclerView
        setupRecyclerView()

        // Listeners para los botones
        addExerciseButton.setOnClickListener {
            showAddExerciseDialog()
        }

        createWorkoutButton.setOnClickListener {
            saveWorkoutToFirestore()
        }
    }

    // Función para manejar la selección de elementos en la barra de herramientas
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Al presionar el botón de retroceso, se finaliza la actividad actual y se regresa a la anterior
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Configuración del RecyclerView
    private fun setupRecyclerView() {
        wtExercisesAdapter = WtExerciseAdapter(exercisesList)
        exercisesRecyclerView.layoutManager = LinearLayoutManager(this)
        exercisesRecyclerView.adapter = wtExercisesAdapter
    }

    // Función para mostrar el diálogo de agregar ejercicio
    @SuppressLint("MissingInflatedId")
    private fun showAddExerciseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exercise, null)

        // Configuración de los NumberPickers
        val seriesPicker = dialogView.findViewById<NumberPicker>(R.id.number_picker_series)
        seriesPicker.minValue = 1
        seriesPicker.maxValue = 10

        val repsPicker = dialogView.findViewById<NumberPicker>(R.id.number_picker_reps)
        repsPicker.minValue = 1
        repsPicker.maxValue = 50

        val weightPicker = dialogView.findViewById<NumberPicker>(R.id.number_picker_weight)
        weightPicker.minValue = 1
        weightPicker.maxValue = 200

        // Configuración del spinner de grupos de ejercicios
        val groupSpinner: Spinner = dialogView.findViewById(R.id.spinner_group)
        val groupArray = resources.getStringArray(R.array.exercise_groups_no_show_all)
        val groupAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, groupArray)
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        groupSpinner.adapter = groupAdapter

        // Configuración del spinner de ejercicios
        val exerciseSpinner: Spinner = dialogView.findViewById(R.id.spinner_exercise)
        val exerciseAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf())
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exerciseSpinner.adapter = exerciseAdapter

        // Escuchar el cambio de selección en el spinner de grupo y actualizar el spinner de ejercicios en consecuencia
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

        // Crear el AlertDialog para agregar ejercicio
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

    // Función para guardar la rutina de entrenamiento en Firestore
    private fun saveWorkoutToFirestore() {
        val workoutName = workoutNameEditText.text.toString()

        if (workoutName.isBlank() || exercisesList.isEmpty()) {
            // Mostrar un mensaje de error si el nombre de la rutina está vacío o no hay ejercicios
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            // Guardar los ejercicios en Firestore
            val wtExercisesRefs = exercisesList.map { exercise ->
                val docRef = FirebaseFirestore.getInstance().collection("wt_exercises").document()
                docRef.set(exercise).await()
                docRef
            }

            // Guardar la rutina en Firestore
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

                // Redireccionar al fragmento de rutinas en MainActivity
                redirectToWorkoutFragment()
            }
        }
    }


    // Función para redirigir al fragmento de rutinas
    private fun redirectToWorkoutFragment() {
        finish()
    }

}


