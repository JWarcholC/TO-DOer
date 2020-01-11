package pl.pwsztar.to_doer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
            var password = password.text.toString()
            if(login.isEmpty() && password.isEmpty()) {
                Toast.makeText(this, "E-mail and password must be filled!",
                    Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            password = password.md5()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(login, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) {
                        return@addOnCompleteListener
                    }
                    Log.d("[LoginActivity]", "Successfully logged in user $login")
                    moveToNewTask()

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

        //TODO: fb
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
                        null, null, null, null) }
                    user?.let { save(it) }

                    moveToNewTask()
                } else {
                    Log.w("[LoginActivity]", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this,"Auth Failed",Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun moveToNewTask() {
        val intent = Intent(this, NewTaskActivity::class.java)
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
