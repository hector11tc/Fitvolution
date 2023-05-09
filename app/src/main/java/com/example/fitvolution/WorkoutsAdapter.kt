package com.example.fitvolution

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

// Adaptador para el RecyclerView que muestra la lista de rutinas de entrenamiento
class WorkoutsAdapter(
    // Lista inicial de rutinas, puede ser actualizada posteriormente
    private var workouts: List<Workout> = listOf(),
    // Callback que se llama cuando se hace click en el botón de eliminar
    private val onDeleteButtonClick: (String) -> Unit,
    // Callback que se llama cuando se hace click en el botón de favorito
    private val onFavouriteButtonClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutsAdapter.ViewHolder>() {

    // ViewHolder que contiene las vistas de cada elemento de la lista
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referencias a las vistas dentro del elemento de la lista
        val workoutNameTextView: TextView = itemView.findViewById(R.id.text_view_workout_name)
        val exercisesRecyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view_exercises)
        val deleteWorkoutButton: ImageButton = itemView.findViewById(R.id.delete_workout_button)
        val favouriteButton: ImageView = itemView.findViewById(R.id.favourite_button) // Referencia al botón de favorito

        init {
            // Configuramos el botón de eliminación para que muestre un diálogo de confirmación cuando se haga clic en él
            deleteWorkoutButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteWorkoutConfirmationDialog(itemView, workouts[position])
                }
            }

            // Configuramos el botón de favorito para que llame al callback correspondiente cuando se haga clic en él
            favouriteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavouriteButtonClick(workouts[position])
                }
            }
        }
    }

    // Función que muestra un diálogo de confirmación antes de eliminar una rutina de entrenamiento
    private fun showDeleteWorkoutConfirmationDialog(view: View, workout: Workout) {
        // Construcción y configuración del diálogo
        val alertDialog = AlertDialog.Builder(view.context)
        alertDialog.setTitle("Eliminar rutina")
        alertDialog.setMessage("¿Estás seguro de que deseas eliminar esta rutina de ejercicios?")
        alertDialog.setPositiveButton("Sí") { _, _ ->
            // Si el usuario confirma, llamamos al callback de eliminación
            onDeleteButtonClick(workout.id)
        }
        alertDialog.setNegativeButton("No", null)
        alertDialog.show()
    }

    // Esta función se llama para crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflamos la vista del elemento de la lista a partir de su layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_card, parent, false)
        // Creamos y devolvemos el ViewHolder
        return ViewHolder(view)
    }

    // Esta función se llama para configurar un ViewHolder existente con los datos de una rutina de entrenamiento
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]

        // Configuramos las vistas con los datos de la rutina
        holder.workoutNameTextView.text = workout.name
        holder.workoutNameTextView.setTypeface(holder.workoutNameTextView.typeface, Typeface.BOLD)
        holder.workoutNameTextView.paintFlags = holder.workoutNameTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG


        // Configuramos el botón de favorito con el estado correcto
        if (workout.favourite) {
            holder.favouriteButton.setImageResource(R.drawable.ic_star_filled)
        } else {
            holder.favouriteButton.setImageResource(R.drawable.ic_star_outline)
        }

        // Configuramos el RecyclerView anidado con la lista de ejercicios de la rutina
        val wtExercisesAdapter = WtExercisesAdapter(workout.exercisesList)
        holder.exercisesRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.exercisesRecyclerView.adapter = wtExercisesAdapter
    }

    // Esta función devuelve el número de elementos en la lista
    override fun getItemCount(): Int = workouts.size

    // Esta función actualiza la lista de rutinas y notifica al RecyclerView que los datos han cambiado
    fun updateWorkouts(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }
}

// Adaptador para el RecyclerView anidado que muestra la lista de ejercicios de una rutina
class WtExercisesAdapter(private val exercises: List<WtExercise>) :
    RecyclerView.Adapter<WtExercisesAdapter.ViewHolder>() {

    // ViewHolder que contiene las vistas de cada elemento de la lista
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referencias a las vistas dentro del elemento de la lista
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_name)
        val groupTextView: TextView = itemView.findViewById(R.id.text_view_group)
        val seriesRepsWeightTextView: TextView = itemView.findViewById(R.id.text_view_series_reps_weight)
    }

    // Esta función se llama para crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflamos la vista del elemento de la lista a partir de su layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wt_exercise, parent, false)
        // Creamos y devolvemos el ViewHolder
        return ViewHolder(view)
    }

    // Esta función se llama para configurar un ViewHolder existente con los datos de un ejercicio
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]

        // Configuramos las vistas con los datos del ejercicio
        holder.nameTextView.text = exercise.name
        holder.nameTextView.setTypeface(holder.nameTextView.typeface, Typeface.BOLD)
        holder.groupTextView.text = exercise.group
        holder.seriesRepsWeightTextView.text = "${exercise.series}x ${exercise.reps}: ${exercise.weight}kg"
    }

    // Esta función devuelve el número de elementos en la lista
    override fun getItemCount(): Int = exercises.size
}


