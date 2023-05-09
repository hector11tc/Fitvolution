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

// Adapter for RecyclerView showing the list of training routines
class WorkoutsAdapter(
    private var workouts: List<Workout> = listOf(),
    private val onDeleteButtonClick: (String) -> Unit,
    private val onFavouriteButtonClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutsAdapter.ViewHolder>() {

    // ViewHolder containing the views of each item in the list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutNameTextView: TextView = itemView.findViewById(R.id.text_view_workout_name)
        val exercisesRecyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view_exercises)
        val deleteWorkoutButton: ImageButton = itemView.findViewById(R.id.delete_workout_button)
        val favouriteButton: ImageView = itemView.findViewById(R.id.favourite_button)

        init {
            // Configure the delete button to display a confirmation dialog when clicked
            deleteWorkoutButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteWorkoutConfirmationDialog(itemView, workouts[position])
                }
            }

            // Set the bookmark button to call the corresponding callback when clicked on
            favouriteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavouriteButtonClick(workouts[position])
                }
            }
        }
    }

    // Function to display a confirmation dialogue before deleting a training routine
    private fun showDeleteWorkoutConfirmationDialog(view: View, workout: Workout) {
        val alertDialog = AlertDialog.Builder(view.context)
        alertDialog.setTitle("Eliminar rutina")
        alertDialog.setMessage("¿Estás seguro de que deseas eliminar esta rutina de ejercicios?")
        alertDialog.setPositiveButton("Sí") { _, _ ->
            // If the user confirms, we call the delete callback
            onDeleteButtonClick(workout.id)
        }
        alertDialog.setNegativeButton("No", null)
        alertDialog.show()
    }

    // This function is called to create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_card, parent, false)
        // We create and return the ViewHolder
        return ViewHolder(view)
    }

    // This function is called to configure an existing ViewHolder with data from a training routine.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]

        // We set up the views with the data from the routine
        holder.workoutNameTextView.text = workout.name
        holder.workoutNameTextView.setTypeface(holder.workoutNameTextView.typeface, Typeface.BOLD)
        holder.workoutNameTextView.paintFlags = holder.workoutNameTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG


        // Set the bookmark button to the correct status
        if (workout.favourite) {
            holder.favouriteButton.setImageResource(R.drawable.ic_star_filled)
        } else {
            holder.favouriteButton.setImageResource(R.drawable.ic_star_outline)
        }

        // We set up the nested RecyclerView with the list of exercises in the routine.
        val wtExercisesAdapter = WtExercisesAdapter(workout.exercisesList)
        holder.exercisesRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.exercisesRecyclerView.adapter = wtExercisesAdapter
    }

    // This function returns the number of items in the list.
    override fun getItemCount(): Int = workouts.size

    // This function updates the list of routines and notifies the RecyclerView that the data has changed.
    fun updateWorkouts(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }
}

// Adapter for nested RecyclerView showing the list of exercises in a routine
class WtExercisesAdapter(private val exercises: List<WtExercise>) :
    RecyclerView.Adapter<WtExercisesAdapter.ViewHolder>() {

    // ViewHolder containing the views of each item in the list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_name)
        val groupTextView: TextView = itemView.findViewById(R.id.text_view_group)
        val seriesRepsWeightTextView: TextView = itemView.findViewById(R.id.text_view_series_reps_weight)
    }

    // This function is called to create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // We inflate the view of the list item from its layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wt_exercise, parent, false)
        return ViewHolder(view)
    }

    // This function is called to set up an existing ViewHolder with data from an exercise.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]

        // We set up the views with the exercise data.
        holder.nameTextView.text = exercise.name
        holder.nameTextView.setTypeface(holder.nameTextView.typeface, Typeface.BOLD)
        holder.groupTextView.text = exercise.group
        holder.seriesRepsWeightTextView.text = "${exercise.series}x ${exercise.reps}: ${exercise.weight}kg"
    }

    // This function returns the number of items in the list.
    override fun getItemCount(): Int = exercises.size
}


