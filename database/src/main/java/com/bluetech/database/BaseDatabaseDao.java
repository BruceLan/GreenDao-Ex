/*
 * ****************************************************************************
 *   Copyright (C) 2005-2017 BlueTech Corporation. All rights reserved
 *   File        : BaseDatabaseDao.java
 *   Description : dao基类，主要提供了多主键情况下的数据的自动插入
 *
 *   Creation    : 17-02-15
 *   Author      : bruce.d.lan@gmail.com
 *   History     : Creation, 17-02-15, bruce.d.lan, Create the file
 * ****************************************************************************
 */

package com.bluetech.database;


import java.util.Arrays;
import java.util.Iterator;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.database.StandardDatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

/**
 * Created by lanzh@ucweb.com on 2016/11/10.
 */

public abstract class BaseDatabaseDao<T, P> extends AbstractDao<T, P> {

    protected DatabaseStatement mNonPkInsertOrReplaceStatement;
    protected DatabaseStatement mNonPkInsertStatement;

    public BaseDatabaseDao(DaoConfig config,
                           AbstractDaoSession daoSession) {
        super(config, daoSession);
    }

    public BaseDatabaseDao(DaoConfig config) {
        super(config);
    }

    public DatabaseStatement getInsertOrReplaceStatement() {

        if(this.mNonPkInsertOrReplaceStatement == null) {
            String sql = org.greenrobot.greendao.internal.SqlUtils.createSqlInsert("INSERT OR REPLACE INTO ", getTablename(), this.config.nonPkColumns);
            DatabaseStatement newInsertOrReplaceStatement = this.db.compileStatement(sql);
            synchronized(this) {
                if(this.mNonPkInsertOrReplaceStatement == null) {
                    this.mNonPkInsertOrReplaceStatement = newInsertOrReplaceStatement;
                }
            }

            if(this.mNonPkInsertOrReplaceStatement != newInsertOrReplaceStatement) {
                newInsertOrReplaceStatement.close();
            }
        }

        return this.mNonPkInsertOrReplaceStatement;
    }

    public DatabaseStatement getInsertStatement() {

        if(this.mNonPkInsertStatement == null) {
            String sql = org.greenrobot.greendao.internal.SqlUtils.createSqlInsert("INSERT INTO ", getTablename(), this.config.nonPkColumns);
            DatabaseStatement newInsertStatement = this.db.compileStatement(sql);
            synchronized(this) {
                if(this.mNonPkInsertStatement == null) {
                    this.mNonPkInsertStatement = newInsertStatement;
                }
            }

            if(this.mNonPkInsertStatement != newInsertStatement) {
                newInsertStatement.close();
            }
        }

        return this.mNonPkInsertStatement;
    }

    private DatabaseStatement getInsertStatement(boolean containPkColumn){
        DatabaseStatement statement;
        if (containPkColumn) {
            statement = this.statements.getInsertStatement();
        } else {
            statement = getInsertStatement();
        }
        return statement;
    }

    private DatabaseStatement getInsertOrReplaceStatement(boolean containPkColumn){
        DatabaseStatement statement;
        if (containPkColumn) {
            statement = this.statements.getInsertOrReplaceStatement();
        } else {
            statement = getInsertOrReplaceStatement();
        }
        return statement;
    }

    @Override
    public void insertInTx(Iterable<T> entities, boolean containPkColumn) {
        DatabaseStatement statement = getInsertStatement(containPkColumn);
        executeInsertInTx(statement, entities, isEntityUpdateable(),containPkColumn);
    }

    public void insertInTx(boolean containPkColumn, T... entities) {
        DatabaseStatement statement = getInsertStatement(containPkColumn);
        executeInsertInTx(statement, Arrays.asList(entities), isEntityUpdateable(),containPkColumn);
    }


    public void insertOrReplaceInTx(Iterable<T> entities, boolean setPrimaryKey, boolean containPkColumn) {
        DatabaseStatement statement = getInsertOrReplaceStatement(containPkColumn);
        this.executeInsertInTx(statement, entities, setPrimaryKey,containPkColumn);
    }

    @Override
    public void insertOrReplaceInTx(Iterable<T> entities, boolean containPkColumn) {
        DatabaseStatement statement = getInsertOrReplaceStatement(containPkColumn);
        this.executeInsertInTx(statement, entities, this.isEntityUpdateable(),containPkColumn);
    }


    public void insertOrReplaceInTx(boolean containPkColumn, T... entities) {
        this.insertOrReplaceInTx(Arrays.asList(entities), containPkColumn);
    }

    private void executeInsertInTx(DatabaseStatement stmt, Iterable<T> entities, boolean setPrimaryKey, boolean containPkColumn) {
        this.db.beginTransaction();

        try {
            synchronized(stmt) {
                if(this.identityScope != null) {
                    this.identityScope.lock();
                }

                try {
                    if(this.isStandardSQLite) {
                        SQLiteStatement rawStmt = (SQLiteStatement)stmt.getRawStatement();
                        Iterator entity = entities.iterator();

                        while(entity.hasNext()) {
                            T rowId = (T)entity.next();
                            if(containPkColumn){
                                this.bindValues(rawStmt, rowId);
                            }else{
                                this.bindNonPkValues(rawStmt, rowId);
                            }
                            if(setPrimaryKey) {
                                long rowId1 = rawStmt.executeInsert();
                                this.updateKeyAfterInsertAndAttach(rowId, rowId1, false);
                            } else {
                                rawStmt.execute();
                            }
                        }
                    } else {
                        Iterator rawStmt1 = entities.iterator();

                        while(rawStmt1.hasNext()) {
                            T entity1 = (T)rawStmt1.next();
                            if(containPkColumn){
                                this.bindValues(stmt, entity1);
                            }else{
                                this.bindNonPkValues(stmt, entity1);
                            }
                            if(setPrimaryKey) {
                                long rowId2 = stmt.executeInsert();
                                this.updateKeyAfterInsertAndAttach(entity1, rowId2, false);
                            } else {
                                stmt.execute();
                            }
                        }
                    }
                } finally {
                    if(this.identityScope != null) {
                        this.identityScope.unlock();
                    }

                }
            }

            this.db.setTransactionSuccessful();
        } finally {
            this.db.endTransaction();
        }

    }

    public long insert(T entity, boolean containPkColumn) {
        DatabaseStatement statement = getInsertStatement(containPkColumn);
        return this.executeInsert(entity, statement, true, containPkColumn);
    }

    public long insertWithoutSettingPk(T entity, boolean containPkColumn) {
        DatabaseStatement statement = getInsertStatement(containPkColumn);
        return this.executeInsert(entity, statement, false, containPkColumn);
    }

    public long insertOrReplace(T entity, boolean containPkColumn) {
        DatabaseStatement statement = getInsertOrReplaceStatement(containPkColumn);
        return this.executeInsert(entity, statement, true, containPkColumn);
    }

    private long executeInsert(T entity, DatabaseStatement stmt, boolean setKeyAndAttach, boolean containPkColumn) {
        long rowId;
        if(this.db.isDbLockedByCurrentThread()) {
            rowId = this.insertInsideTx(entity, stmt, containPkColumn);
        } else {
            this.db.beginTransaction();

            try {
                rowId = this.insertInsideTx(entity, stmt, containPkColumn);
                this.db.setTransactionSuccessful();
            } finally {
                this.db.endTransaction();
            }
        }

        if(setKeyAndAttach) {
            this.updateKeyAfterInsertAndAttach(entity, rowId, true);
        }

        return rowId;
    }

    private long insertInsideTx(T entity, DatabaseStatement stmt, boolean containPkColumn) {
        synchronized(stmt) {
            if(this.isStandardSQLite) {
                SQLiteStatement rawStmt = (SQLiteStatement)stmt.getRawStatement();
                if(containPkColumn){
                    this.bindValues(rawStmt, entity);
                }else{
                    this.bindNonPkValues(rawStmt, entity);
                }

                return rawStmt.executeInsert();
            } else {
                if(containPkColumn){
                    this.bindValues(stmt, entity);
                }else{
                    this.bindNonPkValues(stmt, entity);
                }
                return stmt.executeInsert();
            }
        }
    }

    public UpdateBuilder<T> updateBuilder(){
        return UpdateBuilder.internalCreate(this);
    }

    public DeleteBuilder<T> deleteBuilder(){
        return DeleteBuilder.internalCreate(this);
    }

    protected void bindNonPkValues(DatabaseStatement stmt, T entity) {
    }

    protected void bindNonPkValues(SQLiteStatement stmt, T entity) {
        bindNonPkValues(new StandardDatabaseStatement(stmt), entity);
    }

    protected String getValue(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    protected String getString(Cursor cursor, int index) {
        return cursor.isNull(index) ? null : cursor.getString(index);
    }

    protected long getLong(Cursor cursor, int index) {
        return cursor.isNull(index) ? -1 : cursor.getLong(index);
    }


    @Override
    protected void bindValues(SQLiteStatement stmt, T entity) {
        bindValues(new StandardDatabaseStatement(stmt), entity);
    }
}
