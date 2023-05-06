package com.example.fitvolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adaptador para mostrar la lista de ejercicios en un RecyclerView
class ExerciseAdapter(private val exercises: MutableList<Exercise>) :
    RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

    // ViewHolder para contener las vistas de cada elemento de la lista de ejercicios
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.exercise_image)
        val nameTextView: TextView = itemView.findViewById(R.id.exercise_name)
        val groupTextView: TextView = itemView.findViewById(R.id.exercise_group)
    }

    // Método para inflar la vista de la tarjeta de ejercicio y crear un ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_card, parent, false)
        return ViewHolder(view)
    }

    // Método para vincular los datos del ejercicio con las vistas del ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.nameTextView.text = exercise.name
        holder.groupTextView.text = exercise.group

        // Obtener el identificador de recurso de la imagen basado en la ruta de la imagen en la base de datos
        val imageResource = holder.itemView.context.resources.getIdentifier(
            exercise.image, "drawable", holder.itemView.context.packageName
        )

        // Establecer la imagen en el ImageView
        holder.imageView.setImageResource(imageResource)
    }

    // Método para obtener la cantidad de elementos en la lista de ejercicios
    override fun getItemCount(): Int {
        return exercises.size
    }

    // Método para actualizar la lista de ejercicios en el adaptador y notificar cambios
    fun updateExercises(newExercises: List<Exercise>) {
        exercises.clear()
        exercises.addAll(newExercises)
        notifyDataSetChanged()
    }
}
