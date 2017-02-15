/*
 * ****************************************************************************
 *   Copyright (C) 2005-2017 BlueTech Corporation. All rights reserved
 *   File        : Index.java
 *   Description :
 *
 *   Creation    : 17-02-15
 *   Author      : bruce.d.lan@gmail.com
 *   History     : Creation, 17-02-15, bruce.d.lan, Create the file
 * ****************************************************************************
 */

package com.bluetech.database;


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
