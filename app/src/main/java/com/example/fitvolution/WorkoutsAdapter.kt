package com.example.fitvolution

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutsAdapter(
    private var workouts: List<Workout> = listOf(),
    private val onDeleteButtonClick: (String) -> Unit
) : RecyclerView.Adapter<WorkoutsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutNameTextView: TextView = itemView.findViewById(R.id.text_view_workout_name)
        val exercisesRecyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view_exercises)
        val deleteWorkoutButton: ImageButton = itemView.findViewById(R.id.delete_workout_button)

        init {
            deleteWorkoutButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteWorkoutConfirmationDialog(itemView, workouts[position])
                }
            }
        }
    }

    private fun showDeleteWorkoutConfirmationDialog(view: View, workout: Workout) {
        val alertDialog = AlertDialog.Builder(view.context)
        alertDialog.setTitle("Eliminar rutina")
        alertDialog.setMessage("¿Estás seguro de que deseas eliminar esta rutina de ejercicios?")
        alertDialog.setPositiveButton("Sí") { _, _ ->
            onDeleteButtonClick(workout.id) // Usamos el parámetro onDeleteButtonClick
        }
        alertDialog.setNegativeButton("No", null)
        alertDialog.show()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]

        holder.workoutNameTextView.text = workout.name
        holder.workoutNameTextView.setTypeface(holder.workoutNameTextView.typeface, Typeface.BOLD)

        val wtExercisesAdapter = WtExercisesAdapter(workout.exercisesList)
        holder.exercisesRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.exercisesRecyclerView.adapter = wtExercisesAdapter
    }

    override fun getItemCount(): Int = workouts.size

    fun updateWorkouts(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }
}

class WtExercisesAdapter(private val exercises: List<WtExercise>) :
    RecyclerView.Adapter<WtExercisesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_name)
        val groupTextView: TextView = itemView.findViewById(R.id.text_view_group)
        val seriesRepsWeightTextView: TextView = itemView.findViewById(R.id.text_view_series_reps_weight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wt_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]

        holder.nameTextView.text = exercise.name
        holder.nameTextView.setTypeface(holder.nameTextView.typeface, Typeface.BOLD)
        holder.groupTextView.text = exercise.group
        holder.seriesRepsWeightTextView.text = "${exercise.series}x ${exercise.reps}: ${exercise.weight}kg"
    }

    override fun getItemCount(): Int = exercises.size
}


