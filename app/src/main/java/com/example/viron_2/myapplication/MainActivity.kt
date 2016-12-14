package com.example.viron_2.myapplication

import android.app.AlarmManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.example.viron_2.myapplication.db.TaskContract
import com.example.viron_2.myapplication.db.TaskContract.TaskBase.*
import com.example.viron_2.myapplication.db.TaskDbHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_task.*
import kotlinx.android.synthetic.main.item_task.view.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var TAG: String = "MainActivity";
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
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        mTaskListView =  list_tasks
        updateUI()
    }

    override fun onResume() {
        deleteExpiredTasks()
        super.onResume()
        updateUI()
    }

    fun closeTask(view: View) {
        val parent = view.parent as View
        val taskTextView = parent.task_title
        val task = taskTextView.text as String

        deleteDoneTask(task)
        updateUI()
    }


    private fun updateUI() {
        val data = ArrayList<Map<String,String>>()

        val db = mHelper.readableDatabase
        val cursor = db.query(TABLE,
                arrayOf(ID,TITLE, CLOSED, EXPIRES_AT),null,null,null,null,null)
        while (cursor.moveToNext()) {
            val c_idx = cursor.getColumnIndex(CLOSED)
            val cls = cursor.getInt(c_idx)!=0

            if (!cls) {
                val t_idx = cursor.getColumnIndex(TITLE)
                val d_idx = cursor.getColumnIndex(EXPIRES_AT)

                val m = HashMap<String,String>()
                m.put(TITLE,cursor.getString(t_idx))
                m.put(EXPIRES_AT,"Expire date: " + cursor.getString(d_idx))

                data.add(m)
            }
        }

        mAdapter=SimpleAdapter(this,
                data,
                R.layout.item_task,
                arrayOf(TITLE,EXPIRES_AT),
                intArrayOf(R.id.task_title,R.id.task_time))

        mTaskListView?.adapter=mAdapter

        cursor.close()
        db.close()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_task -> {
                val intent = Intent(this,CreateTaskActivity::class.java)
                intent.putExtra("TITLE","")
                startActivity(intent)
                return true

            }
            R.id.action_archive -> {
                val intent = Intent(this,ArchiveActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun deleteDoneTask(task: String) {
        val db = mHelper.writableDatabase

        val cursor = db.query(TABLE,arrayOf(ID,TITLE,EXPIRES_AT),
                "title = " + "'" + task + "'",null,null,null,null)
        cursor.moveToFirst()
        val idx = cursor.getColumnIndex(ID)
        val id = cursor.getInt(idx)
        val date = getDateTime(Date())

        val cv = ContentValues()
        cv.put(TITLE,task)
        cv.put(CLOSED,1)
        cv.put(EXPIRES_AT,date)

        db.update(TABLE,cv, ID + " = "+id,null)
        db.close()
    }

    private fun deleteExpiredTasks() {
        val db = mHelper.readableDatabase
        val cursor = db.query(TABLE,
                null,null,null,null,null,null)

        var count: Int = 0

        while (cursor.moveToNext()) {
            val e_idx = cursor.getColumnIndex(EXPIRES_AT)
            val c_idx = cursor.getColumnIndex(CLOSED)
            val cls = cursor.getInt(c_idx)!=0

            if (!cls) {
                val now: Date = Date()
                val expire: Date = getDateTime_fromString(cursor.getString(e_idx))

                //Log.d(TAG, now.toString() + " :: " + expire.toString())
                if (expire.before(now)) {
                    count++
                    val t_idx = cursor.getColumnIndex(TITLE)
                    val i_idx=cursor.getColumnIndex(ID)

                    val cv = ContentValues()
                    cv.put(TITLE, cursor.getString(t_idx))
                    cv.put(CLOSED, 1)
                    cv.put(EXPIRES_AT, getDateTime(now))
                    cv.put(EXPIRED,1)

                    db.update(TABLE,cv, ID + " = "+cursor.getInt(i_idx),null)

                }
            }
        }
        if (count>0) {
            createExpirationDialog(count)
        }
        db.close()
    }

    private fun createExpirationDialog(tasks_count: Int) {
        val dialog = AlertDialog.Builder(this)
                .setTitle(tasks_count.toString() + " task/tasks expired. \nYou can find them in archive.")
                .setPositiveButton("Ok",null)
                .create()
        dialog.show()
    }

    private fun getDateTime(date: Date): String {
        val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun getDateTime_fromString(s_date: String): Date {
        val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.parse(s_date)
    }
}
