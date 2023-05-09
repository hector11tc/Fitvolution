package com.example.fitvolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.res.ResourcesCompat
import android.graphics.Typeface

// Adapter for RecyclerView that displays the exercise list of the training routine.
class WtExerciseAdapter(private val exercises: List<WtExercise>) :
    RecyclerView.Adapter<WtExerciseAdapter.ViewHolder>() {

    // ViewHolder containing the views to be displayed in each element of the RecyclerView
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_name)
        val groupTextView: TextView = itemView.findViewById(R.id.text_view_group)
        val seriesRepsWeightTextView: TextView = itemView.findViewById(R.id.text_view_series_reps_weight)
    }

    // Method to create the ViewHolder by inflating the view of each RecyclerView element.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wt_exercise, parent, false)
        return ViewHolder(view)
    }

    // Method to link the exercise data with the respective views in the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]

        // Setting the text of views and applying formatting
        holder.nameTextView.text = exercise.name
        holder.nameTextView.setTypeface(holder.nameTextView.typeface, Typeface.BOLD)
        holder.groupTextView.text = exercise.group
        holder.seriesRepsWeightTextView.text = "${exercise.series}x ${exercise.reps}: ${exercise.weight}kg"
    }

    // Method for obtaining the number of items in the exercise list
    override fun getItemCount(): Int = exercises.size
}

