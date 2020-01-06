package pl.pwsztar.to_doer

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_btn.setOnClickListener {
            val name = name.text.toString()
            val surName = surname.text.toString()
            val login : String? = login_name.text.toString()
            val password = password.text.toString()
            val email = email.text.toString()
            val country = country.text.toString()

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password must be filled!",
                    Toast.LENGTH_SHORT)
                    .show()

                return@setOnClickListener
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) {
                        return@addOnCompleteListener
                    }
                    val user = User(email, password, login, name, surName, country)
                    save(user)

                    Toast.makeText(this, "Successfully created user ${login ?: email}",
                        Toast.LENGTH_SHORT)
                        .show()

                    goBack()

                }.addOnFailureListener {
                    Toast.makeText(this, "User ${login ?: email} registration failed!",
                        Toast.LENGTH_SHORT)
                        .show()

                    Log.d("[MainActivity]", "User ${login ?: email} registration failed!")
                }
        }

        go_back_btn.setOnClickListener {goBack()}

    }

    private fun goBack() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun save(user : User) {
        val uid = FirebaseAuth.getInstance().uid ?:  ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("[RegisterActivity] ","User ${user.login} added to db!")
            }.addOnFailureListener {
                Log.e("[RegisterActivity] ","User ${user.login} adding to db failed!")
            }

    }

}

class User(val email : String, val password : String, var login : String?,
           var name : String?, var surName : String?, var country : String?)
