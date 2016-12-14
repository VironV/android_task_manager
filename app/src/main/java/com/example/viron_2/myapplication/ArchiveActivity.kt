package com.example.viron_2.myapplication

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.viron_2.myapplication.db.TaskContract
import com.example.viron_2.myapplication.db.TaskDbHelper
import kotlinx.android.synthetic.main.activity_archive.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_task.view.*
import kotlinx.android.synthetic.main.old_item_task.*
import kotlinx.android.synthetic.main.old_item_task.view.*
import java.util.*

class ArchiveActivity : AppCompatActivity() {
    private var TAG: String = "ArchiveActivity";
    private var mHelper: TaskDbHelper = TaskDbHelper(this)
    private var mTaskListView: ListView? = null
    private var mAdapter: SimpleAdapter? = null

    val TABLE: String = TaskContract.TaskBase.TABLE
    val ID: String = "_id"
    val TITLE: String = TaskContract.TaskBase.TITLE
    val EXPIRES_AT: String = TaskContract.TaskBase.EXPIRES_AT
    val CLOSED: String = TaskContract.TaskBase.CLOSED
    val EXPIRED: String = TaskContract.TaskBase.EXPIRED


    override fun onCreate(savedInstanceState: Bundle?) {
        //Log.d(TAG, "\nCreate\n")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)
    }

    override fun onStart() {
        super.onStart()
        mTaskListView =  list_old_tasks
        updateUI()
    }

    private fun updateUI() {
        val data = ArrayList<Map<String,String>>()

        var count =0

        val db = mHelper.readableDatabase
        val cursor = db.query(TABLE,
                null,null,null,null,null,null)
        while (cursor.moveToNext()) {
            val c_idx = cursor.getColumnIndex(CLOSED)
            val cls = cursor.getInt(c_idx)!=0

            if (cls) {
                count++

                val t_idx = cursor.getColumnIndex(TITLE)
                val d_idx = cursor.getColumnIndex(EXPIRES_AT)
                val ex_idx = cursor.getColumnIndex(EXPIRED)

                val m = HashMap<String,String>()
                m.put(TITLE,cursor.getString(t_idx))
                m.put(EXPIRES_AT,"Close date: " + cursor.getString(d_idx))
                val exp = cursor.getInt(ex_idx)!=0
                if (exp) {
                    m.put(EXPIRED, "EXPIRED")
                } else {
                    m.put(EXPIRED, "DONE")
                }

                data.add(m)
            }
        }

        mAdapter= SimpleAdapter(this,
                data,
                R.layout.old_item_task,
                arrayOf(TITLE,EXPIRES_AT,EXPIRED),
                intArrayOf(R.id.old_task_title,R.id.task_close_time,R.id.task_expired))
        mTaskListView?.adapter=mAdapter

        /* //Not working...
        for (i in 0 until count) {
            if (data.get(i).get(EXPIRED)== "EXPIRED") {
                val item_id=mTaskListView?.getItemIdAtPosition(i)?.toInt()
                if (item_id!=null) {
                    Log.d(TAG,"DSDS")
                    val layout = mTaskListView?.findViewById(item_id) as RelativeLayout?
                    //layout?.setBackgroundColor(Color.parseColor("#990000"))
                    layout?.task_expired?.setTextColor(Color.parseColor("#009900"))
                }
            } else {
                val item_id=mTaskListView?.getItemIdAtPosition(i)?.toInt()
                if (item_id!=null) {
                    Log.d(TAG,"DSDS")
                    val layout = mTaskListView?.findViewById(item_id) as RelativeLayout?
                    //layout?.setBackgroundColor(Color.parseColor("#009900"))
                    layout?.task_expired?.setTextColor(Color.parseColor("#009900"))
                }
            }
        }
        */

        cursor.close()
        db.close()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.archive_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_archive -> {
                val taskEditText = EditText(this)
                val dialog = AlertDialog.Builder(this)
                        .setTitle("Delete all tasks in archive?")
                        .setPositiveButton("Yes") {
                            dialog,i ->
                            val taskList = ArrayList<String>()
                            val db = mHelper.readableDatabase
                            val cursor = db.query(TABLE,
                                    arrayOf(ID,CLOSED),null,null,null,null,null)
                            while (cursor.moveToNext()) {
                                val c_idx = cursor.getColumnIndex(CLOSED)
                                val cls = cursor.getInt(c_idx)!=0

                                if (cls) {
                                    val idx = cursor.getColumnIndex(ID)
                                    db.delete(TABLE,ID + " = " + cursor.getInt(idx), null)
                                }
                            }
                            db.close()

                            finish()
                        }
                        .setNegativeButton("Cancel",null)
                        .create()
                dialog.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    fun deleteTask(view: View) {
        val dialog = AlertDialog.Builder(this)
                .setTitle("Delete or recreate this task?")
                .setPositiveButton("Delete") {
                    dialog, which ->
                    val parent = view.parent as View
                    val taskTextView = parent.old_task_title
                    val task = taskTextView.text as String
                    deleteTask_delete(task)
                    updateUI()
                }
                .setNegativeButton("Rectreate") {
                    dialog, which ->
                    val parent = view.parent as View
                    val taskTextView = parent.old_task_title
                    val title = taskTextView.text as String

                    val intent = Intent(this, CreateTaskActivity::class.java)
                    intent.putExtra("TITLE",title)
                    startActivity(intent)
                    updateUI()
                }
                .setNeutralButton("Cancel",null)
                .create()
        dialog.show()
    }

    fun deleteTask_delete(task: String) {
        val db = mHelper.writableDatabase
        val cursor = db.query(TABLE,arrayOf(ID,TITLE),
                "title = " + "'" + task + "'",null,null,null,null)
        cursor.moveToFirst()
        val c_idx = cursor.getColumnIndex("_id")
        var id = cursor.getInt(c_idx)

        //Log.d(TAG,"id: " +id)

        db.delete(TABLE,ID + " = "+id,null)
        db.close()
    }
}
