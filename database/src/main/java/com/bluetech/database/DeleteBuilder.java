/*
 * ****************************************************************************
 *   Copyright (C) 2005-2017 BlueTech Corporation. All rights reserved
 *   File        : DeleteBuilder.java
 *   Description :
 *
 *   Creation    : 17-02-15
 *   Author      : bruce.d.lan@gmail.com
 *   History     : Creation, 17-02-15, bruce.d.lan, Create the file
 * ****************************************************************************
 */

package com.bluetech.database;


import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.query.WhereCondition;

/**
 * Created by lanzh@ucweb.com on 2016/11/16.
 */
public class DeleteBuilder<T> {
    private final WhereCollector<T> whereCollector;
    private final AbstractDao<T, ?> dao;
    private List<Object> mWhereValues;

    public static <T1> DeleteBuilder<T1> internalCreate(AbstractDao<T1, ?> dao) {
        return new DeleteBuilder(dao);
    }

    protected DeleteBuilder(AbstractDao<T, ?> dao) {
        this(dao, "T");
    }

    protected DeleteBuilder(AbstractDao<T, ?> dao, String tablePrefix) {
        this.dao = dao;
        this.mWhereValues = new ArrayList<Object>();
        this.whereCollector = new WhereCollector(dao, tablePrefix);
    }

    public DeleteBuilder<T> where(WhereCondition cond, WhereCondition... condMore) {
        this.whereCollector.add(cond, condMore);
        return this;
    }

    public DeleteBuilder<T> whereOr(WhereCondition cond1, WhereCondition cond2, WhereCondition... condMore) {
        this.whereCollector.add(this.or(cond1, cond2, condMore), new WhereCondition[0]);
        return this;
    }

    public WhereCondition or(WhereCondition cond1, WhereCondition cond2, WhereCondition... condMore) {
        return this.whereCollector.combineWhereConditions(" OR ", cond1, cond2, condMore);
    }

    public WhereCondition and(WhereCondition cond1, WhereCondition cond2, WhereCondition... condMore) {
        return this.whereCollector.combineWhereConditions(" AND ", cond1, cond2, condMore);
    }

    private void appendJoinsAndWheres(StringBuilder builder, String tablePrefixOrNull) {
        boolean whereAppended = !whereCollector.isEmpty();
        mWhereValues.clear();
        if (whereAppended) {
            whereCollector.appendWhereClause(builder, tablePrefixOrNull, mWhereValues);
        }
    }

    protected static String[] toStringArray(Object[] values) {
        int length = values.length;
        String[] strings = new String[length];
        for (int i = 0; i < length; i++) {
            Object object = values[i];
            if (object != null) {
                strings[i] = object.toString();
            } else {
                strings[i] = null;
            }
        }
        return strings;
    }

    public int build() {
        Object database = dao.getDatabase().getRawDatabase();
        if(database instanceof SQLiteDatabase){
            SQLiteDatabase db = (SQLiteDatabase) database;

            StringBuilder whereClause = new StringBuilder();
            appendJoinsAndWheres(whereClause, null);
            return db.delete(dao.getTablename(), whereClause.toString(), toStringArray(mWhereValues.toArray()));
        }
        return -1;
    }

}

