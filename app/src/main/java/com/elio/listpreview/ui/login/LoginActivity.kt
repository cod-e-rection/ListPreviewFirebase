package com.elio.listpreview.ui.login

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.elio.listpreview.HomeActivity
import com.elio.listpreview.databinding.ActivityLoginBinding
import com.elio.listpreview.R
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    private var signUpView: Boolean = true
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        signUpView = intent?.getBooleanExtra("signUp", true) ?: true

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val signUpButton = binding.signUpButton
        val scopicTitle = binding.scopicTitle
        val emailHint = binding.emailHint
        val passHint = binding.passwordHint
        val signInOutPrompt = binding.signInOutPrompt
        val signInTextView = binding.signInTextView

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            // Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                if (signUpView) {
                    auth.signInWithEmailAndPassword(username.text.toString(), password.text.toString())
                        .addOnCompleteListener(this@LoginActivity) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success")
                                val user = auth.currentUser
                                //updateUI(user)
                                loginViewModel.login(username.text.toString(), password.text.toString())
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.exception)
                                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT,).show()
                                //updateUI(null)
                            }
                        }
                } else {
                    auth.createUserWithEmailAndPassword(username.text.toString(), password.text.toString())
                        .addOnCompleteListener(this@LoginActivity) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success")
                                val user = auth.currentUser
                                //updateUI(user)
                                loginViewModel.login(username.text.toString(), password.text.toString())
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT,
                                ).show()
                                //updateUI(null)
                            }
                        }
                }
                //loginViewModel.login(username.text.toString(), password.text.toString())
            }

            // show sign Up View's at first
            if (signUpView) {
                signUpButton?.visibility = View.VISIBLE
                scopicTitle?.visibility = View.VISIBLE
                signInTextView?.visibility = View.GONE
            } else {
                signUpButton?.visibility = View.GONE
                scopicTitle?.visibility = View.GONE
                login.text = "Sign Up"
                signInOutPrompt?.text = "Sign Up"
                signInTextView?.visibility = View.VISIBLE
                signInTextView?.text = "Have an account? Sign In"
                // re open login
                signInTextView?.setOnClickListener {
                    val openSignUpScreen = Intent(context, LoginActivity::class.java)
                    openSignUpScreen.putExtra("signUp", true)
                    startActivity(openSignUpScreen)
                }
            }

            signUpButton?.setOnClickListener {
                val openSignUpScreen = Intent(context, LoginActivity::class.java)
                openSignUpScreen.putExtra("signUp", false)
                startActivity(openSignUpScreen)
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName

        Toast.makeText(applicationContext, "$welcome $displayName", Toast.LENGTH_LONG).show()
        val openHome = Intent(this, HomeActivity::class.java)
        openHome.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(openHome)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    // move to the Home screen if the user is logged in
    private fun reload() {
        val name = getData()
        Toast.makeText(applicationContext, "Welcome Back $name", Toast.LENGTH_LONG).show()
        val openHome = Intent(this, HomeActivity::class.java)
        openHome.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(openHome)
    }

    // get email data
    private fun getData() : String {
        val user = auth.currentUser
        var name = ""
        user?.let {
            // Name
            name = it.displayName.toString()
        }
        return name
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}