/*
 * ****************************************************************************
 *   Copyright (C) 2005-2016 UCWEB Corporation. All rights reserved
 *   File        : Index.java
 *   Description :
 *
 *   Creation    : 16-11-12
 *   Author      : lanzh@ucweb.com
 *   History     : Creation, 16-11-12, lanzh, Create the file
 * ****************************************************************************
 */

package com.papapa.database;


/**
 * Created by lanzh@ucweb.com on 2016/11/12.
 */

public class Index {

    public String mType;
    public String mName;
    public DaoProperty[] mColumnList;

    public Index(String aType, String aName, DaoProperty... aColumnList) {
        mType = aType;
        mName = aName;
        mColumnList = aColumnList;
    }

    public Index(String aName, DaoProperty... aColumnList) {
        this("", aName, aColumnList);
    }
}
