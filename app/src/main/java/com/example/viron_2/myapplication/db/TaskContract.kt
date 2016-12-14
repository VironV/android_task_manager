package com.example.viron_2.myapplication.db;

import android.provider.BaseColumns;
import java.util.*


class TaskContract {
    companion object  {
        val DB_NAME: String = "myapp.db"
        val DB_VERSION: Int = 11
    }

    public class TaskBase : BaseColumns {
        companion object {
            val TABLE: String = "tasks"
            val TITLE: String = "title"
            val EXPIRES_AT: String = "expires_at"
            val CLOSED: String = "closed"
            val EXPIRED: String = "expired"
        }
    }
}

