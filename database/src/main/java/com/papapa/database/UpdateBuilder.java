/*
 * ****************************************************************************
 *   Copyright (C) 2005-2016 UCWEB Corporation. All rights reserved
 *   File        : UpdateBuilder.java
 *   Description :
 *
 *   Creation    : 16-11-16
 *   Author      : lanzh@ucweb.com
 *   History     : Creation, 16-11-16, lanzh, Create the file
 * ****************************************************************************
 */

package com.papapa.database;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.query.WhereCondition;

/**
 * Created by lanzh@ucweb.com on 2016/11/16.
 */
public class UpdateBuilder<T> {
    private final WhereCollector<T> whereCollector;
    private final AbstractDao<T, ?> dao;
    private List<Object> mWhereValues;
    private final String tablePrefix;
    private List<DaoProperty> mProperties;
    private List<Object> mValues;

    public static <T1> UpdateBuilder<T1> internalCreate(AbstractDao<T1, ?> dao) {
        return new UpdateBuilder(dao);
    }

    protected UpdateBuilder(AbstractDao<T, ?> dao) {
        this(dao, "T");
    }

    protected UpdateBuilder(AbstractDao<T, ?> dao, String tablePrefix) {
        this.dao = dao;
        this.tablePrefix = tablePrefix;
        this.mWhereValues = new ArrayList<Object>();
        this.whereCollector = new WhereCollector(dao, tablePrefix);
        this.mProperties = new ArrayList<DaoProperty>();
        this.mValues = new ArrayList<Object>();
    }


    public UpdateBuilder<T> set(DaoProperty aPropertie, Object value) {
        mProperties.add(aPropertie);
        mValues.add(value);
        return this;
    }

    public UpdateBuilder<T> clean(){
        mProperties.clear();
        mValues.clear();
        return this;
    }

    public UpdateBuilder<T> where(WhereCondition cond, WhereCondition... condMore) {
        this.whereCollector.add(cond, condMore);
        return this;
    }

    public UpdateBuilder<T> whereOr(WhereCondition cond1, WhereCondition cond2, WhereCondition... condMore) {
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
        int count = -1;
        if(database instanceof SQLiteDatabase){
            SQLiteDatabase db = (SQLiteDatabase) database;
            try{
                db.beginTransactionNonExclusive();
                ContentValues contentValues = new ContentValues();
                for(int i = 0; i < mProperties.size(); i++){
                    contentValues.put(mProperties.get(i).columnName, checkValueForType(mProperties.get(i), mValues.get(i)).toString());
                }

                StringBuilder whereClause = new StringBuilder();
                appendJoinsAndWheres(whereClause, null);
                count = db.update(dao.getTablename(), contentValues, whereClause.toString(), toStringArray(mWhereValues.toArray()));
                db.setTransactionSuccessful();
            } finally {
                try{
                    db.endTransaction();
                } catch (SQLiteFullException ex) {
                    ex.printStackTrace();
                } catch (SQLiteDiskIOException ex) {
                    ex.printStackTrace();
                }
            }

        }
        return count;
    }

    private static Object checkValueForType(Property property, Object value) {
        if (value != null && value.getClass().isArray()) {
            throw new DaoException("Illegal value: found array, but simple object required");
        }
        Class<?> type = property.type;
        if (type == Date.class) {
            if (value instanceof Date) {
                return ((Date) value).getTime();
            } else if (value instanceof Long) {
                return value;
            } else {
                throw new DaoException("Illegal date value: expected java.util.Date or Long for value " + value);
            }
        } else if (property.type == boolean.class || property.type == Boolean.class) {
            if (value instanceof Boolean) {
                return ((Boolean) value) ? 1 : 0;
            } else if (value instanceof Number) {
                int intValue = ((Number) value).intValue();
                if (intValue != 0 && intValue != 1) {
                    throw new DaoException("Illegal boolean value: numbers must be 0 or 1, but was " + value);
                }
            } else if (value instanceof String) {
                String stringValue = ((String) value);
                if ("TRUE".equalsIgnoreCase(stringValue)) {
                    return 1;
                } else if ("FALSE".equalsIgnoreCase(stringValue)) {
                    return 0;
                } else {
                    throw new DaoException(
                                            "Illegal boolean value: Strings must be \"TRUE\" or \"FALSE\" (case insensitive), but was "
                                              + value);
                }
            }
        }
        return value;
    }
}

