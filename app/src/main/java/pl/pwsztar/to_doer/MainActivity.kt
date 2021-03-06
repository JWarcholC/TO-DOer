package pl.pwsztar.to_doer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main_window.*
import pl.pwsztar.to_doer.utils.verifyUser
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentHashMap


class MainActivity : AppCompatActivity() {

    @SuppressLint("SimpleDateFormat")
    val sdf = SimpleDateFormat("dd.MM.yyyy")
    private var uid:String? = null
    var listItems = ArrayList<String>()
    var adapter: ArrayAdapter<String>? = null
    val tasksWithIdsInAdpater = ConcurrentHashMap<Int, String>()


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)

        uid = verifyUser()
        if(uid == null) {
            Log.d("[MainActivity]", "uid is null")
            Toast.makeText(this, "You are not logged in!", Toast.LENGTH_SHORT)
                .show()
            goToLoginActivity()
        }
        firebaseUsername()
        firebaseTasks()

        go_back_btn2.setOnClickListener { goToLoginActivity() }
        register_btn.setOnClickListener { goToNewTaskActivity() }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        taskList.adapter = adapter
        taskList.setOnItemClickListener { _, _, pos, _ ->
            firebaseDeleteTaskByUid(tasksWithIdsInAdpater[pos])
        }
    }

    private fun goToNewTaskActivity() {
        val intent = Intent(this, NewTaskActivity::class.java)
        startActivity(intent)
    }


    private fun goToLoginActivity() {
        if(AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut()
        }
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun firebaseTasks() {
        val ref = FirebaseDatabase.getInstance().getReference("/tasks/").child("$uid")
        val eventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(snapshots: DataSnapshot) {
                listItems.clear()

                var i = 0
                for(ds in snapshots.children) {
                    val category = ds.child("category").getValue(String::class.java)
                    val name = ds.child("name").getValue(String::class.java)
                    val date = ds.child("taskDate").getValue(String::class.java)
                        ?.replace("\\s".toRegex(), "")

                    if(!date.isNullOrEmpty()) {
                        if(System.currentTimeMillis() >= sdf.parse(date).time) {
                            ref.child("${ds.key}").removeValue()
                            continue
                        }
                        val formattedTask = String.format("%11s\t\t\t\t%-20s\t\t%-10s", date, name, category)

                        listItems.add(i, formattedTask)
                        ds.key?.let { tasksWithIdsInAdpater.put(i, it) }

                        i++
                    }
                }
                adapter?.notifyDataSetChanged()
            }
        }
        ref.addListenerForSingleValueEvent(eventListener)
    }

    private fun firebaseUsername() {
        val ref = FirebaseDatabase.getInstance().getReference("/users/").child("$uid")
        val eventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshots: DataSnapshot) {
                for(ds in snapshots.children) {
                    if(ds.key.equals("login")) {
                        textView20.text = " Hello, ${ds.getValue(String::class.java)}"
                        break
                    }
                }
            }
        }

        ref.addListenerForSingleValueEvent(eventListener)
    }

    private fun firebaseDeleteTaskByUid(refUid: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("/tasks/").child("$uid")
        ref.child("$refUid").removeValue().addOnSuccessListener {
            Toast.makeText(this, "Successfully deleted task $refUid", Toast.LENGTH_LONG)
                .show()

            adapter?.clear()
            firebaseTasks()
            adapter?.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Deleting task $refUid failed!", Toast.LENGTH_LONG)
                .show()
            return@addOnFailureListener
        }
    }
}