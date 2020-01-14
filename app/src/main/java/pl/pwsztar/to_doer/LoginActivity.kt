package pl.pwsztar.to_doer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import pl.pwsztar.to_doer.domain.User
import pl.pwsztar.to_doer.utils.isConnectedToNetwork
import pl.pwsztar.to_doer.utils.md5


class LoginActivity : AppCompatActivity() {
    //Google Login Request Code
    private val RC_SIGN_IN = 7

    //Google Sign In Client
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    // FB callback manager
    private val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        AppEventsLogger.activateApp(application)
        fb_login_btn.setReadPermissions("email", "public_profile", "user_friends")

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        login_btn.setOnClickListener {
            if(!this.baseContext.isConnectedToNetwork()) {
                Toast.makeText(this, "Check network connection", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val login = login.text.toString()
            val password = password.text.toString()
            if(login.isEmpty() && password.isEmpty()) {
                Toast.makeText(this, "E-mail and password must be filled!",
                    Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(login, password.md5())
                .addOnCompleteListener {
                    if(!it.isSuccessful) {
                        return@addOnCompleteListener
                    }
                    Log.d("[LoginActivity]", "Successfully logged in user $login")
                    moveToTaskList()

                }.addOnFailureListener {
                    Toast.makeText(this, "User $login logging in failed!",
                        Toast.LENGTH_SHORT)
                        .show()

                    Log.d("[LoginActivity]", "User $login logging in failed!")
                }
        }

        register_btn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        gmail_btn.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }


        fb_btn.setOnClickListener {
           fb_login_btn.performClick()
        }

        fb_login_btn.setOnClickListener {
            if(Profile.getCurrentProfile() != null) {
                moveToTaskList()
                return@setOnClickListener
            }

            firebaseAuthWithFacebook()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                Log.w("[LoginActivity]", "Google sign in failed", e)
            }
        } else { /* FB auth */
            callbackManager.onActivityResult(requestCode, resultCode, data)
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("[LoginActivity]", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("[LoginActivity]", "signInWithCredential:success")

                    val user = FirebaseAuth.getInstance().currentUser?.email?.let { User(it, "",
                        it, null, null, null) }
                    user?.let { save(it) }

                    moveToTaskList()
                } else {
                    Log.w("[LoginActivity]", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this,"Auth Failed",Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun firebaseAuthWithFacebook() {
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Log.i("[LoginActivity]", "Successfully logged in")
                    val currentProfile = Profile.getCurrentProfile()

                    val user = User(
                        "", "", currentProfile.id.toString(), currentProfile.firstName,
                        currentProfile.lastName, "FB World"
                    )
                    save(user)
                    moveToTaskList()
                }

                override fun onCancel() {
                    Log.i("[LoginActivity]", "FB cancelled")
                }

                override fun onError(error: FacebookException?) {
                    Toast.makeText(
                        this@LoginActivity, "Error ${error?.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
    }

    private fun moveToTaskList() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun save(user : User) {
        val uid = FirebaseAuth.getInstance().uid ?:  ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("[LoginActivity] ","User ${user.login} added to db!")
            }.addOnFailureListener {
                Log.e("[LoginActivity] ","User ${user.login} adding to db failed!")
            }

    }
}
