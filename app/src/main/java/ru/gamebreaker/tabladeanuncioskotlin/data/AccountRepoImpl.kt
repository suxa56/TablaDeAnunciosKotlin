package ru.gamebreaker.tabladeanuncioskotlin.data

import android.widget.Toast
import com.google.firebase.auth.*
import ru.gamebreaker.tabladeanuncioskotlin.presentation.MainActivity
import ru.gamebreaker.tabladeanuncioskotlin.R
import ru.gamebreaker.tabladeanuncioskotlin.domain.repo.AccountRepo

class AccountRepoImpl(private val act: MainActivity): AccountRepo {

    private val mAuth = FirebaseAuth.getInstance()

    override fun signUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { it ->
                            if (it.isSuccessful) {
                                signUpWithEmailSuccessful(it.result?.user!!)
                            } else {
                                signUpWithEmailException(it.exception!!)
                            }
                        }
                }
            }
        }
    }

    override fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { it ->
                            if (it.isSuccessful) {
                                act.uiUpdate(it.result?.user)
                            } else {
                                signInWithEmailException(it.exception!!)
                            }
                        }
                }
            }
        }
    }

    fun signInAnonymously(listener: Listener) {
        mAuth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                listener.onComplete()
                Toast.makeText(act, R.string.you_are_logged_in_as_a_guest, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(act, R.string.failed_to_login_as_guest, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signUpWithEmailSuccessful(user: FirebaseUser) {
        sendEmailVerification(user)
        act.uiUpdate(user)
    }

    private fun signUpWithEmailException(e: Exception) {
        if (e is FirebaseAuthInvalidCredentialsException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG)
                    .show()
            }
        }
        if (e is FirebaseAuthWeakPasswordException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun signInWithEmailException(e: Exception) {

        Toast.makeText(act, act.resources.getString(R.string.sign_in_error), Toast.LENGTH_LONG)
            .show()
        //Log.d(MyLogConst.MY_LOG, "sign In With Email Exception : ${e}")
        if (e is FirebaseAuthUserCollisionException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                Toast.makeText(
                    act,
                    FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE,
                    Toast.LENGTH_LONG
                ).show()
            } else if (e.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_LONG)
                    .show()
            }
        } else if (e is FirebaseAuthInvalidUserException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_USER_NOT_FOUND, Toast.LENGTH_LONG)
                    .show() //добавить стринг описание ошибки(ошибок)
            }
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.send_verification_email_done),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.send_verification_email_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    interface Listener {
        fun onComplete()
    }
}