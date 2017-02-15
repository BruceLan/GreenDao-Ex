/*
 * ****************************************************************************
 *   Copyright (C) 2005-2017 BlueTech Corporation. All rights reserved
 *   File        : AbstractDaoManager.java
 *   Description :
 *
 *   Creation    : 17-02-15
 *   Author      : bruce.d.lan@gmail.com
 *   History     : Creation, 17-02-15, bruce.d.lan, Create the file
 * ****************************************************************************
 */

package com.bluetech.database;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.internal.DaoConfig;

/**
 * Created by lanzh@ucweb.com on 2016/11/10.
 */

public abstract class AbstractDaoManager implements DaoMaster.IDaoMasterActor,BaseDatabaseOpenHelper.IDatabaseActor{
    protected BaseDatabaseOpenHelper mOpenHelper;
    protected DaoMaster mDaoMaster;
    protected Map<Class<? extends AbstractDao<?, ?>>, DaoConfig> mDaoConfigMap;
    private final Class[] sDaoClasses;
    private Database mDatabase;

    public AbstractDaoManager(Context aContext) {
        mOpenHelper = new BaseDatabaseOpenHelper(aContext, this);
        mDaoConfigMap = new HashMap<Class<? extends AbstractDao<?, ?>>, DaoConfig>();
        sDaoClasses = getDaoClasses();
        mDatabase = mOpenHelper.getWritableDb();
        mDaoMaster = new DaoMaster(mDatabase, getVersion(), this);
        registerDao(mDatabase);
    }

    private void registerDao(Database db){
        for (Class daoCls : sDaoClasses) {
            registerDaoConfig(db, daoCls);
        }
    }

    protected DaoConfig getDaoConfig(Database aDatabase, Class<? extends AbstractDao<?, ?>> T){
        DaoConfig config = mDaoConfigMap.get(T);
        if(config == null){
            config = new DaoConfig(aDatabase, T);
            mDaoConfigMap.put(T, config);
        }

        return config;
    }

    protected void registerDaoConfig(Database aDatabase, Class<? extends AbstractDao<?, ?>> T){
        DaoConfig config = mDaoConfigMap.get(T);
        if(config == null){
            config = new DaoConfig(aDatabase, T);
            mDaoConfigMap.put(T, config);
        }
    }

    @Override
    public void onCreate(Database db) {
        try{
            db.beginTransaction();
            for (Class daoCls : sDaoClasses) {
                DaoConfig daoConfig = getDaoConfig(db, daoCls);
                db.execSQL(SqlUtils.createTableSql(daoConfig));

                SqlUtils.executeUpdateTableIndexSql(db ,daoCls, daoConfig);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        try {
            db.beginTransaction();
            for (Class daoCls : sDaoClasses) {
                DaoConfig daoConfig = getDaoConfig(db, daoCls);

                db.execSQL(SqlUtils.createTableSql(daoConfig));

                try {
                    SqlUtils.executeUpdateTableIndexSql(db, daoCls, daoConfig);
                } catch (Exception ex) {
                    onUpgradeIndexError(db, oldVersion, newVersion, daoCls , daoConfig);
                }

                DaoProperty[] properties = new DaoProperty[daoConfig.properties.length];
                for (int i = 0; i < daoConfig.properties.length; i++) {
                    properties[i] = (DaoProperty) daoConfig.properties[i];
                }
                for (DaoProperty property : properties) {
                    if (!SqlUtils.checkPropertyExist(db, daoConfig.tablename, property)) {
                        db.execSQL(SqlUtils.addDaoPropertySql(daoConfig.tablename, property));
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    protected void onUpgradeIndexError(Database db, int oldVersion, int newVersion, Class<? extends AbstractDao<?, ?>> daoClass, DaoConfig daoConfig) {

    }

    public void destroy() {
        try {
            mDatabase.close();
            mOpenHelper.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected abstract Class[] getDaoClasses();

}
