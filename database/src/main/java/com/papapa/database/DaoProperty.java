/*
 * ****************************************************************************
 *   Copyright (C) 2005-2016 UCWEB Corporation. All rights reserved
 *   File        : DaoProperty.java
 *   Description :
 *
 *   Creation    : 16-11-11
 *   Author      : lanzh@ucweb.com
 *   History     : Creation, 16-11-11, lanzh, Create the file
 * ****************************************************************************
 */

package com.papapa.database;

import org.greenrobot.greendao.Property;


/**
 * Created by lanzh@ucweb.com on 2016/11/9.
 */

public class DaoProperty extends Property {
    public boolean mIsUnique;
    public boolean mIsNotNull;
    public String mTypeName;
    public DaoProperty(int ordinal, Class<?> type, String name, boolean primaryKey,
                       String columnName) {
        this(ordinal, type, name, primaryKey, columnName, false, false);
    }

    public DaoProperty(int ordinal, Class<?> type, String name, boolean primaryKey,
                       String columnName, boolean aIsUnique, boolean aIsNotNull) {
        super(ordinal, type, name, primaryKey, columnName);
        mIsUnique = aIsUnique;
        mIsNotNull = aIsNotNull;
        mTypeName = getTypeName();
    }

    private String getTypeName() {
        if (type.equals(Long.class)) {
            return "BIGINT";
        } else if (type.equals(Double.class)) {
            return "DOUBLE";
        } else if (type.equals(String.class)) {
            return "TEXT";
        } else if (type.equals(Byte[].class)) {
            return "BLOB";
        } else if (type.equals(Integer.class)){
            return "INTEGER";
        } else if (type.equals(Boolean.class)){
            return "TINYINT";
        } else if (type.equals(Short.class)) {
            return "SMALLINT";
        } else {
            throw new RuntimeException("Not support this type!");
        }
    }

    public String getDefaultValue(){
        if (type.equals(Long.class)) {
            return "0";
        } else if (type.equals(Double.class)) {
            return "0.0";
        } else if (type.equals(String.class)) {
            return "\"\"";
        } else if (type.equals(Byte[].class)) {
            return "0";
        } else if (type.equals(Integer.class)){
            return "0";
        } else if (type.equals(Short.class)){
            return "0";
        } else {
            throw new RuntimeException("Not support this type!");
        }
    }



}
