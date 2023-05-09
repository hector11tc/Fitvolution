package com.example.fitvolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter for displaying the exercise list in a RecyclerView
class ExerciseAdapter(private val exercises: MutableList<Exercise>) :
    RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

    // ViewHolder to contain the views of each item in the exercise list
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.exercise_image)
        val nameTextView: TextView = itemView.findViewById(R.id.exercise_name)
        val groupTextView: TextView = itemView.findViewById(R.id.exercise_group)
        val secGroupTextView: TextView = itemView.findViewById(R.id.exercise_sec_group)
    }

    // Method to inflate the view of the exercise card and create a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_card, parent, false)
        return ViewHolder(view)
    }

    // Method to link the exercise data to the ViewHolder views
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.nameTextView.text = exercise.name
        holder.groupTextView.text = exercise.group
        holder.secGroupTextView.text = exercise.sec_group

        // Get the resource identifier of the image based on the path to the image in the database
        val imageResource = holder.itemView.context.resources.getIdentifier(
            exercise.image, "drawable", holder.itemView.context.packageName
        )

        // Set the image in the ImageView
        holder.imageView.setImageResource(imageResource)
    }

    // Method for obtaining the number of items in the exercise list
    override fun getItemCount(): Int {
        return exercises.size
    }

    // Method for updating the list of exercises in the adapter and reporting changes
    fun updateExercises(newExercises: List<Exercise>) {
        exercises.clear()
        exercises.addAll(newExercises)
        notifyDataSetChanged()
    }
}
