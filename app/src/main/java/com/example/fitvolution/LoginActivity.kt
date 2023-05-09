package com.example.fitvolution

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.properties.Delegates
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale






class LoginActivity : AppCompatActivity() {

    companion object{
        lateinit var useremail: String
        lateinit var providerSession: String
    }

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var lyTerms: LinearLayout

    private lateinit var mAuth: FirebaseAuth

    private var RESULT_CODE_GOOGLE_SIGN_IN = 100



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        lyTerms = findViewById(R.id.lyTerms)
        lyTerms.visibility = View.INVISIBLE

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        mAuth = FirebaseAuth.getInstance()

        manageButtonLogin()
        etEmail.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
        etPassword.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) goMain(currentUser.email.toString(), currentUser.providerId)
    }


//    función para que, una vez iniciada la sesión, si el usuario da al botón "back", no volver al activity_login
    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    private fun manageButtonLogin(){
        var tvLogin = findViewById<TextView>(R.id.tvLogin)
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        if (password.length < 6 || ValidateEmail.isEmail(email) == false){
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.clear_grey))
            tvLogin.isEnabled = false
        }
        else{
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.green_fitvolution))
            tvLogin.isEnabled = true
        }
    }

    private fun goMain(email: String, provider: String){
        useremail = email
        providerSession = provider

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun addUserToDB(email: String) {
        val dateRegister = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val dbRegister = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userDocument = hashMapOf(
                "email" to email,
                "dateRegister" to dateRegister
            )

            dbRegister.collection("users")
                .document(currentUser.uid) // Usamos el UID en lugar del correo electrónico
                .set(userDocument)
        } else {
            Log.e(TAG, "No se pudo obtener el usuario autenticado")
        }
    }


    private fun register(){
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener() {
                if (it.isSuccessful){
                    val email = email
                    addUserToDB(email)

                    goMain(email, "email")
                }
                else Toast.makeText(this, "Sorry, something went wrong.", Toast.LENGTH_SHORT).show()
            }
    }

    fun login(view: View){
        loginUser()
    }

    private fun loginUser(){
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful) goMain(email, "email")
                else{
                    if (lyTerms.visibility == View.INVISIBLE) {
                        lyTerms.visibility = View.VISIBLE
                        var tvLogin = findViewById<TextView>(R.id.tvLogin)
                        tvLogin.text = resources.getString(R.string.signUp)
                        Toast.makeText(this, "You don´t have an account. Please, accept the terms and conditions of use for sign up to Fitvolution!", Toast.LENGTH_LONG).show()

                    }
                    else{
                        var cbAcept = findViewById<CheckBox>(R.id.cbAcept)
                        if (cbAcept.isChecked) register()
                    }
                }
            }
    }

    fun goTerms(v: View){
        val intent = Intent(this, TermsActivity::class.java)
        startActivity(intent)
    }

    fun forgotPassword(v: View){
        resetPassword()
    }

    private fun resetPassword(){
        val e = etEmail.text.toString()
        if (!TextUtils.isEmpty(e)){
            mAuth.sendPasswordResetEmail(e)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) Toast.makeText(this, "Email sent to $e", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "A user with this email does not exist...", Toast.LENGTH_SHORT).show()
                }
        }
        else Toast.makeText(this, "Please, indicate an email", Toast.LENGTH_SHORT).show()
    }

    fun callSignInGoogle(view: View){
        signInGoogle()
    }

    private fun signInGoogle(){
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        var googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        startActivityForResult(googleSignInClient.signInIntent, RESULT_CODE_GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RESULT_CODE_GOOGLE_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!

                if (account != null) {
                    email = account.email!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    mAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            addUserToDB(email)
                            goMain(email, "Google")
                        }
                        else Toast.makeText(this, "Google conexion failed", Toast.LENGTH_SHORT)
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google conexion failed", Toast.LENGTH_SHORT)
            }
        }
    }
}












