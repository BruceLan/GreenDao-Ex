/*
 * ****************************************************************************
 *   Copyright (C) 2005-2017 BlueTech Corporation. All rights reserved
 *   File        : DaoMaster.java
 *   Description :
 *
 *   Creation    : 17-02-15
 *   Author      : bruce.d.lan@gmail.com
 *   History     : Creation, 17-02-15, bruce.d.lan, Create the file
 * ****************************************************************************
 */

package com.bluetech.database;

import org.greenrobot.greendao.AbstractDaoMaster;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;


/**
 * Created by lanzh@ucweb.com on 2016/11/10.
 */

public class DaoMaster extends AbstractDaoMaster {
    public interface IDaoMasterActor{
        AbstractDaoSession newSession();
        AbstractDaoSession newSession(IdentityScopeType aIdentityScopeType);
    }

    private IDaoMasterActor mActor;

    public DaoMaster(Database db, int schemaVersion, IDaoMasterActor aActor) {
        super(db, schemaVersion);
        mActor = aActor;
    }

    @Override
    public AbstractDaoSession newSession() {
        return mActor.newSession();
    }

    @Override
    public AbstractDaoSession newSession(IdentityScopeType aIdentityScopeType) {
        return mActor.newSession(aIdentityScopeType);
    }
}
