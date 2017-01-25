/*
 * ****************************************************************************
 *   Copyright (C) 2005-2016 UCWEB Corporation. All rights reserved
 *   File        : DaoMaster.java
 *   Description :
 *
 *   Creation    : 16-11-11
 *   Author      : lanzh@ucweb.com
 *   History     : Creation, 16-11-11, lanzh, Create the file
 * ****************************************************************************
 */

package com.papapa.database;

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
