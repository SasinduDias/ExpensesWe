package com.sasindu.personaldetailsapp


import android.content.Context
import android.widget.Toast
import androidx.core.util.PatternsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    fun login(email: String, password: String) {
        val emailPattern = PatternsCompat.EMAIL_ADDRESS

        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }

        if (!emailPattern.matcher(email).matches()) {
            _authState.value = AuthState.Error("Please add valid email !")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }


    fun signup(email: String, password: String) {
        val emailPattern = PatternsCompat.EMAIL_ADDRESS
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }

        if (!emailPattern.matcher(email).matches()) {
            _authState.value = AuthState.Error("Please add valid email !")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
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
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}