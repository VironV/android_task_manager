package com.example.viron_2.myapplication

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.viron_2.myapplication.db.TaskContract
import com.example.viron_2.myapplication.db.TaskDbHelper
import kotlinx.android.synthetic.main.activity_create_task.*
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskActivity : AppCompatActivity() {

    private var mHelper: TaskDbHelper = TaskDbHelper(this)
    private var start_title: String? = null

    val TABLE: String = TaskContract.TaskBase.TABLE
    val ID: String = "_id"
    val TITLE: String = TaskContract.TaskBase.TITLE
    val EXPIRES_AT: String = TaskContract.TaskBase.EXPIRES_AT
    val CLOSED: String = TaskContract.TaskBase.CLOSED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        start_title=intent.extras.getString("TITLE")
        val t_view=task_title_edit
        if (start_title!=null) {
            t_view.setText(start_title)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save_task -> {
                val day = task_date_edit.dayOfMonth
                val month = task_date_edit.month
                val year = task_date_edit.year - 1900

                var hour:Int
                var minute:Int
                if (Build.VERSION.SDK_INT >= 23) {
                     hour = task_time_edit.hour
                     minute = task_time_edit.minute
                }
                else {
                     hour = task_time_edit.currentHour
                     minute = task_time_edit.currentMinute
                }

                val date = Date(year,month,day,hour,minute)
                val title=task_title_edit.text.toString()

                val cv = ContentValues()
                cv.put(TITLE,title)
                cv.put(EXPIRES_AT,getDateTime(date))
                cv.put(CLOSED,0)

                val db: SQLiteDatabase = mHelper.writableDatabase
                db.insertWithOnConflict(TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_REPLACE)

                db.close()
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun getDateTime(date: Date): String {
        val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }

}
