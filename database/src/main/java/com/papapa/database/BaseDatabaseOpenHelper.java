/*
 * ****************************************************************************
 *   Copyright (C) 2005-2016 UCWEB Corporation. All rights reserved
 *   File        : BaseDatabaseOpenHelper.java
 *   Description :
 *
 *   Creation    : 16-11-11
 *   Author      : lanzh@ucweb.com
 *   History     : Creation, 16-11-11, lanzh, Create the file
 * ****************************************************************************
 */

package com.papapa.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;

/**
 * Created by lanzh@ucweb.com on 2016/11/10.
 */

public class BaseDatabaseOpenHelper extends DatabaseOpenHelper {
    public interface IDatabaseActor{
        int getVersion();
        String getName();
        void onCreate(Database db);
        void onUpgrade(Database db, int oldVersion, int newVersion);
    }

    protected IDatabaseActor mActor;

    public BaseDatabaseOpenHelper(Context context, IDatabaseActor aActor) {
        super(context, aActor.getName(), aActor.getVersion());
        mActor = aActor;
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
        mActor.onCreate(db);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        mActor.onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase sqLiteDatabase = super.getWritableDatabase();
        return sqLiteDatabase;
    }

    @Override
    protected Database wrap(SQLiteDatabase sqLiteDatabase) {
        return new BaseDatabase(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
