package com.sasindu.personaldetailsapp


import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.util.PatternsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import es.dmoral.toasty.Toasty

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    var firebaseUser: FirebaseUser? = auth.currentUser

    init {
        checkAuthStatus()
        firebaseUser = auth.currentUser
    }


    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String,context: Context) {
        val emailPattern = PatternsCompat.EMAIL_ADDRESS

        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            Toasty.error(
                context,
                "Email or password can't be empty",
                Toast.LENGTH_SHORT,
                true
            ).show()
            return
        }

        if (!emailPattern.matcher(email).matches()) {
            _authState.value = AuthState.Error("Please add valid email !")
            Toasty.error(
                context,
                "Please add valid email !",
                Toast.LENGTH_SHORT,
                true
            ).show()
            return
        }

           if(password.length < 6 ){
               Toasty.error(
                   context,
                   "Password must be at least 6 characters long",
                   Toast.LENGTH_SHORT,
                   true
               ).show()
               return
           }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password.replace("\\s".toRegex(), "") )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }

        if ( _authState.value !is AuthState.Authenticated){
            Toasty.error(
                context,
                "Something went wrong!",
                Toast.LENGTH_SHORT,
                true
            ).show()
        }
    }


    fun signup(email: String, password: String,context: Context) {
        val emailPattern = PatternsCompat.EMAIL_ADDRESS
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            Toasty.error(
                context,
                "Email or password can't be empty",
                Toast.LENGTH_SHORT,
                true
            ).show()
            return
        }

        if (!emailPattern.matcher(email).matches()) {
            _authState.value = AuthState.Error("Please add valid email !")
            Toasty.error(
                context,
                "Please add valid email !",
                Toast.LENGTH_SHORT,
                true
            ).show()
            return
        }

        if(password.length < 6 ){
            Toasty.error(
                context,
                "Password must be at least 6 characters long",
                Toast.LENGTH_SHORT,
                true
            ).show()
            return
        }


        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password.replace("\\s".toRegex(), "") )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }

        if ( _authState.value !is AuthState.Authenticated){
            Toasty.error(
                context,
                _authState.value.toString(),
                Toast.LENGTH_SHORT,
                true
            ).show()
        }
    }

    fun sendEmailVerification(
        email: String,
        context: Context
    ) {
        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Unauthenticated
                    Toasty.success(
                        context,
                        "Password reset link send to email !",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                } else {
                    _authState.value =
                        AuthState.Error(
                            task.exception?.message ?: "Failed to send verification email"
                        )

                    Toasty.error(
                        context,
                        "Failed to send verification email !",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                }
            }
    }

    fun signout() {

        _authState.value = AuthState.Unauthenticated
        firebaseUser = null
        auth.signOut()
    }

    fun refreshUser() {
        firebaseUser = auth.currentUser // Update state when new user logs in
    }

    @SuppressLint("SuspiciousIndentation")
    fun getUserEligibleExpenses(
        context: Context,
        onResult: (SnapshotStateList<Expenses?>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val authViewModelForUser = AuthViewModel()
        val courseList = SnapshotStateList<Expenses?>()

        db.collection("Expenses")
            .whereEqualTo("email", authViewModelForUser.firebaseUser?.email.toString())
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                if (!queryDocumentSnapshots.isEmpty) {
                    val list = queryDocumentSnapshots.documents
                    courseList.clear()
                    for (d in list) {
                        val expense: Expenses? = d.toObject(Expenses::class.java)
                        expense?.let { courseList.add(it) }
                    }
                } else {
                    Toasty.warning(context, "No data found!", Toast.LENGTH_SHORT, true).show()
                }
                onResult(courseList) // Callback with updated data
            }
            .addOnFailureListener { e ->
                Toasty.error(context, "Failed to get data!", Toast.LENGTH_SHORT, true).show()
                onResult(courseList) // Return empty list on failure
            }
    }

}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}