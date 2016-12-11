package com.example.viron_2.myapplication.db

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.viron_2.myapplication.db.TaskContract
import com.example.viron_2.myapplication.db.TaskContract.TaskBase
import android.database.Cursor
import android.util.Log
import android.support.v7.app.AppCompatActivity

class TaskDbHelper(context: Context) : SQLiteOpenHelper(
        context, TaskBase.TABLE, null, TaskContract.DB_VERSION) {

    val TAG = "dbhelper"
    val TABLE = TaskBase.TABLE

    companion object {
        val ID: String = "_id"
        val TITLE: String = TaskBase.TITLE
        val EXPIRES_AT: String = TaskBase.EXPIRES_AT
        val CLOSED: String = TaskBase.CLOSED
    }

    val DATABASE_CREATE=
        "CREATE TABLE ${TABLE} (" +
        "${ID} integer PRIMARY KEY autoincrement, " +
        "${TITLE} TEXT NOT NULL UNIQUE, "+
        "${EXPIRES_AT} DATETIME NOT NULL, "+
        "${CLOSED} integer" +
        ");"

    /*
    fun putTask(text: String) {
        val values = ContentValues()
        values.put(TITLE, text)
        getWritableDatabase().insert(TABLE,null,values)
    }

    fun getTask() :Cursor {
        return getReadableDatabase()
                .query(TABLE, arrayOf(ID, TITLE, CREATED_AT, EXPIRES_AT),null,null,null,null,null)
    }
    */

    override fun onCreate(p0: SQLiteDatabase) {
        Log.d(TAG,"\n\nCreating!!\n\n" + DATABASE_CREATE)
        p0.execSQL(DATABASE_CREATE)
    }

    override fun onUpgrade(p0: SQLiteDatabase, p1: Int, p2: Int) {
        p0.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(p0)
    }

}