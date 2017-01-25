/*
 * ****************************************************************************
 *   Copyright (C) 2005-2016 UCWEB Corporation. All rights reserved
 *   File        : BaseDatabase.java
 *   Description : 基本的database类，主要作用是catch一些外部不需要处理的异常，业务方选择性使用
 *
 *   Creation    : 16-11-11
 *   Author      : lanzh@ucweb.com
 *   History     : Creation, 16-11-11, lanzh, Create the file
 * ****************************************************************************
 */
package com.papapa.database;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.database.StandardDatabaseStatement;

/**
 * Created by lanzh@ucweb.com on 2016/11/17.
 */

public class BaseDatabase implements Database {
    private final SQLiteDatabase delegate;

    public BaseDatabase(SQLiteDatabase delegate) {
        this.delegate = delegate;
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return this.delegate.rawQuery(sql, selectionArgs);
    }

    public void execSQL(String sql) throws SQLException {
        this.delegate.execSQL(sql);
    }

    public void beginTransaction() {
        this.delegate.beginTransaction();
    }

    public void endTransaction() {
        try{
            this.delegate.endTransaction();
        } catch (SQLiteFullException ex) {
            ex.printStackTrace();
        } catch (SQLiteDiskIOException ex) {
            ex.printStackTrace();
        } catch (SQLiteException ex){
            ex.printStackTrace();
        }
    }

    public boolean inTransaction() {
        return this.delegate.inTransaction();
    }

    public void setTransactionSuccessful() {
        this.delegate.setTransactionSuccessful();
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        this.delegate.execSQL(sql, bindArgs);
    }

    public DatabaseStatement compileStatement(String sql) {
        return new StandardDatabaseStatement(this.delegate.compileStatement(sql));
    }

    public boolean isDbLockedByCurrentThread() {
        return this.delegate.isDbLockedByCurrentThread();
    }

    public void close() {
        this.delegate.close();
    }

    public Object getRawDatabase() {
        return this.delegate;
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return this.delegate;
    }
}
