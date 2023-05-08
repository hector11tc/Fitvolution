package com.example.fitvolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.res.ResourcesCompat
import android.graphics.Typeface

// Adaptador para el RecyclerView que muestra la lista de ejercicios de la rutina de entrenamiento
class WtExerciseAdapter(private val exercises: List<WtExercise>) :
    RecyclerView.Adapter<WtExerciseAdapter.ViewHolder>() {

    // ViewHolder que contiene las vistas que se mostrarán en cada elemento del RecyclerView
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_name)
        val groupTextView: TextView = itemView.findViewById(R.id.text_view_group)
        val seriesRepsWeightTextView: TextView = itemView.findViewById(R.id.text_view_series_reps_weight)
    }

    // Método para crear el ViewHolder inflando la vista de cada elemento del RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wt_exercise, parent, false)
        return ViewHolder(view)
    }

    // Método para vincular los datos del ejercicio con sus respectivas vistas en el ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]

        // Establecer el texto de las vistas y aplicar formato
        holder.nameTextView.text = exercise.name
        holder.nameTextView.setTypeface(holder.nameTextView.typeface, Typeface.BOLD)
        holder.groupTextView.text = exercise.group
        holder.seriesRepsWeightTextView.text = "${exercise.series}x ${exercise.reps}: ${exercise.weight}kg"
    }

    // Método para obtener la cantidad de elementos en la lista de ejercicios
    override fun getItemCount(): Int = exercises.size
}

