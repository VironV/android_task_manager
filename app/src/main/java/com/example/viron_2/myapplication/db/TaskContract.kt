package com.example.viron_2.myapplication.db;

import android.provider.BaseColumns;

public class TaskContract {
    public val DB_NAME: String = "myapp.db"
    public val DB_VERSION: Int = 1

    public class TaskEntry : BaseColumns {
        public val TABLE: String = "tasks"
        public val COL_TASK_TITLE: String = "title"
    }
}

