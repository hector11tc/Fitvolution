package com.example.fitvolution

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import android.widget.ImageView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val textViewEmail = view.findViewById<TextView>(R.id.text_view_email)
        val textViewDate = view.findViewById<TextView>(R.id.text_view_date)
        val textViewHeight = view.findViewById<TextView>(R.id.text_view_height)
        val textViewWeight = view.findViewById<TextView>(R.id.text_view_weight)
        val textViewBodyFat = view.findViewById<TextView>(R.id.text_view_bodyFat)
        val textViewBodyWater = view.findViewById<TextView>(R.id.text_view_bodyWater)
        val textViewMuscleMass = view.findViewById<TextView>(R.id.text_view_muscleMass)
        val textViewWhenNothing = view.findViewById<TextView>(R.id.text_view_when_nothing)

        val buttonAddMetric = view.findViewById<FloatingActionButton>(R.id.fabAddMetrics)
        buttonAddMetric.setOnClickListener {
            // Open dialog to add new metrics
            val builder = AlertDialog.Builder(requireContext())

            // Inflate the layout for the dialog
            val inflater = requireActivity().layoutInflater
            val dialogLayout = inflater.inflate(R.layout.dialog_add_metrics, null)

            // Set the custom layout as the dialog's view
            builder.setView(dialogLayout)

            // Add action buttons to the dialog
            builder.setPositiveButton("Add") { dialog, id ->
                // User clicked the "Add" button, so save the metrics
                val height = dialogLayout.findViewById<EditText>(R.id.etHeight).text.toString().toFloat()
                val weight = dialogLayout.findViewById<EditText>(R.id.etWeight).text.toString().toFloat()
                val bodyFat = dialogLayout.findViewById<EditText>(R.id.etBodyFat).text.toString().toInt()
                val bodyWater = dialogLayout.findViewById<EditText>(R.id.etBodyWater).text.toString().toInt()
                val muscleMass = dialogLayout.findViewById<EditText>(R.id.etMuscleMass).text.toString().toFloat()

                // Save the metrics to the database
                val currentUser = auth.currentUser
                val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUser!!.uid)
                if (currentUser != null) {
                    val metrics = hashMapOf(
                        "date" to Calendar.getInstance().time,
                        "user" to userRef,
                        "values" to hashMapOf(
                            "height" to height,
                            "weight" to weight,
                            "bodyFat" to bodyFat,
                            "bodyWater" to bodyWater,
                            "muscleMass" to muscleMass
                        )
                    )
                    db.collection("metrics")
                        .add(metrics)
                        .addOnSuccessListener {
                            // Refresh the displayed metrics
                            displayMetrics(textViewWhenNothing, textViewHeight, textViewWeight, textViewBodyFat, textViewBodyWater, textViewMuscleMass, currentUser)
                        }
                }
            }
            builder.setNegativeButton("Cancel") { dialog, id ->
                // User cancelled the dialog, so close the dialog
                dialog.cancel()
            }

            // Create and show the dialog
            val dialog = builder.create()
            dialog.show()
        }

        // Get current user
        val user = auth.currentUser
        if (user != null) {
            // Set user email
            textViewEmail.text = user.email

            // Set user registration date
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = dateFormat.format(user.metadata?.creationTimestamp)
            textViewDate.text = getString(R.string.fitvolutioner_since, date)

            // Display the most recent metrics
            displayMetrics(textViewWhenNothing, textViewHeight, textViewWeight, textViewBodyFat, textViewBodyWater, textViewMuscleMass, user)
        }

        val signOutIV = view.findViewById<ImageView>(R.id.ivLogout)
        signOutIV?.setOnClickListener {
            callSignOut(it)
        }
    }

    private fun displayMetrics(
        textViewWhenNothing: TextView,
        textViewHeight: TextView,
        textViewWeight: TextView,
        textViewBodyFat: TextView,
        textViewBodyWater: TextView,
        textViewMuscleMass: TextView,
        user: FirebaseUser
    ) {
        // Get the most recent metrics from Firebase
        db.collection("metrics")
            .whereEqualTo("user", FirebaseFirestore.getInstance().collection("users").document(user!!.uid))
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Got ${documents.size()} documents from Firestore")
                if (documents.isEmpty) {
                    // Handle case where no metrics are found
                    textViewWhenNothing.visibility = View.VISIBLE
                    textViewHeight.visibility = View.INVISIBLE
                    textViewWeight.visibility = View.INVISIBLE
                    textViewBodyFat.visibility = View.INVISIBLE
                    textViewBodyWater.visibility = View.INVISIBLE
                    textViewMuscleMass.visibility = View.INVISIBLE
                } else {
                    textViewWhenNothing.visibility = View.INVISIBLE
                    for (document in documents) {
                        val metrics = document.toObject(Metrics::class.java)
                        textViewHeight.text = "Height: " + "${metrics.values.height} cm"
                        textViewWeight.text = "Weight: " + "${metrics.values.weight} kg"
                        textViewBodyFat.text = "Body fat: " + "${metrics.values.bodyFat} %"
                        textViewBodyWater.text = "Body water: " + "${metrics.values.bodyWater} %"
                        textViewMuscleMass.text = "Muscle mass: " + "${metrics.values.muscleMass} kg"

                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }


    fun callSignOut(view: View){
        signOut()
    }

    private fun signOut(){
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this@ProfileFragment.context, LoginActivity::class.java))
        requireActivity().finish()
    }
}





