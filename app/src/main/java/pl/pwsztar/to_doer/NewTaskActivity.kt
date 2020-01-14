package pl.pwsztar.to_doer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_new_task.*
import pl.pwsztar.to_doer.domain.Task
import pl.pwsztar.to_doer.utils.isConnectedToNetwork
import pl.pwsztar.to_doer.utils.verifyUser


class NewTaskActivity : AppCompatActivity() {

    var uid:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)
        uid = verifyUser()
        if(uid == null) {
            Log.d("[NewTaskActivity]", "uid is null")
            Toast.makeText(this, "You are not logged in!", Toast.LENGTH_SHORT)
                .show()
            goToLoginActivity()
        }

        go_back_btn.setOnClickListener {
            goToMainActivity()
        }

        add_task_btn.setOnClickListener {
            if(!this.baseContext.isConnectedToNetwork()) {
                Toast.makeText(this, "Check network connection", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val taskName = task_name.text.toString()
            val taskDate = date_text.text.toString()

            if(taskName.isEmpty()) {
                Toast.makeText(this, "Enter task name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val taskCategory = getCheckedRadioButton()
            if(taskCategory == null) {
                Toast.makeText(this, "Check one of the category",
                    Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val task = Task(taskName, taskCategory, taskDate)
            saveTask(task)

        }
    }

    private fun saveTask(task: Task) {
        val ref = FirebaseDatabase.getInstance().getReference("/tasks/")
        val dataRef = ref.child("$uid")
        val newTaskRef = dataRef.push()

        newTaskRef.setValue(task).addOnSuccessListener {
            Log.d("[NewTaskActivity] ","Task ${task.name} added to db!")
            Toast.makeText(this, "Task ${task.name} successfully added",
                Toast.LENGTH_LONG)
                .show()

            goToMainActivity()
        }.addOnFailureListener {
            Log.e("[NewTaskActivity] ","Task ${task.name} adding to db failed!")
            Toast.makeText(this, "Task ${task.name}  adding failed",
                Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    private fun goToLoginActivity() {
        if(AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut()
        }
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun getCheckedRadioButton(): String? {
        var type:String? = null
        when {
            edu_radio_btn.isChecked -> {
                type = edu_radio_btn.text.toString()
            }
            sport_radio_btn.isChecked -> {
                type = sport_radio_btn.text.toString()
            }
            work_radio_btn.isChecked -> {
                type = work_radio_btn.text.toString()
            }
            home_radio_btn.isChecked -> {
                type = home_radio_btn.text.toString()
            }
        }

        return type
    }
}