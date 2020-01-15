package pl.pwsztar.to_doer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_new_task.*
import pl.pwsztar.to_doer.domain.Task
import pl.pwsztar.to_doer.utils.isConnectedToNetwork
import pl.pwsztar.to_doer.utils.verifyUser
import java.text.SimpleDateFormat
import java.util.*


class NewTaskActivity : AppCompatActivity() {

    var uid:String? = null

    @SuppressLint("SimpleDateFormat")
    val sdf = SimpleDateFormat("dd.MM.yyyy")

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
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

        edu_radio_btn.setOnClickListener {
            work_radio_btn.isChecked = false
            sport_radio_btn.isChecked = false
            home_radio_btn.isChecked = false
        }

        work_radio_btn.setOnClickListener {
            edu_radio_btn.isChecked = false
            sport_radio_btn.isChecked = false
            home_radio_btn.isChecked = false
        }

        sport_radio_btn.setOnClickListener {
            edu_radio_btn.isChecked = false
            work_radio_btn.isChecked = false
            home_radio_btn.isChecked = false
        }

        home_radio_btn.setOnClickListener {
            edu_radio_btn.isChecked = false
            work_radio_btn.isChecked = false
            sport_radio_btn.isChecked = false
        }

        home_radio_btn.setOnClickListener {
            edu_radio_btn.isChecked = false
            work_radio_btn.isChecked = false
            sport_radio_btn.isChecked = false
        }

        calendar_btn.setOnClickListener{
            new_task_main_view.isVisible = false
            new_task_calendar_view.isVisible = true
        }

        set_date_btn.setOnClickListener{
            new_task_main_view.isVisible = true
            new_task_calendar_view.isVisible = false

        }

        calendar.setOnDateChangeListener { _, year, mon, date ->
            var month = mon+1
            val input_date = "$date/$month/$year"
            val format1 = SimpleDateFormat("dd/MM/yyyy")
            val dt1 = format1.parse(input_date)
            if(System.currentTimeMillis() >= dt1.getTime()){
                date_text.setText("")
            }else{
                date_text.setText("$date.$month.$year")
            }

        }

        add_task_btn.setOnClickListener {
            if(!this.baseContext.isConnectedToNetwork()) {
                Toast.makeText(this, "Check network connection", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val taskName = task_name.text.toString()
            var taskDate = date_text.text.toString()

            if(taskName.isEmpty()) {
                Toast.makeText(this, "Enter task name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // to jest zabezpieczenie, ono w niczym nie przeszkadza (testowane xD)
            if(taskDate.isEmpty()) {
                taskDate = sdf.format(Calendar.getInstance().timeInMillis).toString()
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