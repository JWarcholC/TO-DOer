package pl.pwsztar.to_doer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_btn.setOnClickListener {
            val login = login.text.toString()
            val password = password.text.toString()

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

        //TODO: gmail

        //TODO: fb
    }

    private fun moveToNewTask() {
        val intent = Intent(this, NewTaskActivity::class.java)
        startActivity(intent)
    }
}
