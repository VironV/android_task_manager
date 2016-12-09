package com.example.viron_2.myapplication

import android.R.id.input
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.example.viron_2.myapplication.db.TaskDbHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.item_todo.view.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var TAG: String = "MainActivity";
    private var mHelper: TaskDbHelper = TaskDbHelper(this)
    private var mTaskListView: ListView? = null
    private var mAdapter: ArrayAdapter<String>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        //Log.d(TAG, "\nCreate\n")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = mHelper.getReadableDatabase()
        val cursor = db.query("tasks", arrayOf("_id","title"),null,null,null,null,null)
        while (cursor.moveToNext()) {
            val idx = cursor.getColumnIndex("title")
            Log.d(TAG, "Task: " + cursor.getString(idx))
        }
        cursor.close()
        db.close()

    }

    override fun onStart() {
        super.onStart()
        mTaskListView =  activity_main.list_todo
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_task -> {
                Log.d(TAG, "Add a new task")

                val taskEditText = EditText(this)
                val dialog = AlertDialog.Builder(this)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add") {
                            dialog, which ->
                            val task = taskEditText.getText().toString()

                            val db: SQLiteDatabase = mHelper.getWritableDatabase()
                            val values = ContentValues()

                            values.put("title",task)
                            db.insertWithOnConflict("tasks",
                                    null,
                                    values,
                                    SQLiteDatabase.CONFLICT_REPLACE)
                            db.close()
                            updateUI()
                        }
                        .setNegativeButton("Cancel",null)
                        .create()
                dialog.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    public fun deleteTask(view: View) {
        val parent = view.parent as View
        val taskTextView = parent.task_title
        val task = taskTextView.text as String
        Log.d(TAG,"Task here is: " + task)
        val db = mHelper.writableDatabase
        db.delete("tasks","title = " + "'" + task + "'", null)
        db.close()
        updateUI()
    }

    private fun updateUI() {
        Log.d(TAG, "Updating UI")
        val taskList = ArrayList<String>()
        val db = mHelper.readableDatabase
        val cursor = db.query("tasks",
                arrayOf("_id","title"),null,null,null,null,null)
        while (cursor.moveToNext()) {
           // Log.d(TAG, "Cursooooor!")
            val idx = cursor.getColumnIndex("title")
            taskList.add(cursor.getString(idx))
        }

        if (mAdapter == null) {
            //Log.d(TAG, "Here we are")
            mAdapter = ArrayAdapter<String>(this,
                    R.layout.item_todo,
                    R.id.task_title,
                    taskList)
            mTaskListView?.setAdapter(mAdapter)
        } else {
            //Log.d(TAG, "Nope, here")
            mAdapter?.clear()
            mAdapter?.addAll(taskList)
            mAdapter?.notifyDataSetChanged()
        }

        cursor.close()
        db.close()
    }
}
