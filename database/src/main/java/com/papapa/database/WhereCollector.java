/*
 * ****************************************************************************
 *   Copyright (C) 2005-2016 UCWEB Corporation. All rights reserved
 *   File        : WhereCollector.java
 *   Description :
 *
 *   Creation    : 16-11-16
 *   Author      : lanzh@ucweb.com
 *   History     : Creation, 16-11-16, lanzh, Create the file
 * ****************************************************************************
 */

package com.papapa.database;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.query.WhereCondition;

/**
 * Created by lanzh@ucweb.com on 2016/11/16.
 */

class WhereCollector<T> {
    private final AbstractDao<T, ?> dao;
    private final List<WhereCondition> whereConditions;
    private final String tablePrefix;

    WhereCollector(AbstractDao<T, ?> dao, String tablePrefix) {
        this.dao = dao;
        this.tablePrefix = tablePrefix;
        this.whereConditions = new ArrayList();
    }

    void add(WhereCondition cond, WhereCondition... condMore) {
        this.checkCondition(cond);
        this.whereConditions.add(cond);
        WhereCondition[] var3 = condMore;
        int var4 = condMore.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            WhereCondition whereCondition = var3[var5];
            this.checkCondition(whereCondition);
            this.whereConditions.add(whereCondition);
        }

    }

    WhereCondition combineWhereConditions(String combineOp, WhereCondition cond1, WhereCondition cond2, WhereCondition... condMore) {
        StringBuilder builder = new StringBuilder("(");
        ArrayList combinedValues = new ArrayList();
        this.addCondition(builder, combinedValues, cond1);
        builder.append(combineOp);
        this.addCondition(builder, combinedValues, cond2);
        WhereCondition[] var7 = condMore;
        int var8 = condMore.length;

        for(int var9 = 0; var9 < var8; ++var9) {
            WhereCondition cond = var7[var9];
            builder.append(combineOp);
            this.addCondition(builder, combinedValues, cond);
        }

        builder.append(')');
        return new WhereCondition.StringCondition(builder.toString(), combinedValues.toArray());
    }

    void addCondition(StringBuilder builder, List<Object> values, WhereCondition condition) {
        this.checkCondition(condition);
        condition.appendTo(builder, this.tablePrefix);
        condition.appendValuesTo(values);
    }

    void checkCondition(WhereCondition whereCondition) {
        if(whereCondition instanceof WhereCondition.PropertyCondition) {
            this.checkProperty(((WhereCondition.PropertyCondition)whereCondition).property);
        }

    }

    void checkProperty(Property property) {
        if(this.dao != null) {
            Property[] properties = this.dao.getProperties();
            boolean found = false;
            Property[] var4 = properties;
            int var5 = properties.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Property property2 = var4[var6];
                if(property == property2) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                throw new DaoException("Property \'" + property.name + "\' is not part of " + this.dao);
            }
        }

    }

    void appendWhereClause(StringBuilder builder, String tablePrefixOrNull, List<Object> values) {
        ListIterator iter = this.whereConditions.listIterator();

        while(iter.hasNext()) {
            if(iter.hasPrevious()) {
                builder.append(" AND ");
            }

            WhereCondition condition = (WhereCondition)iter.next();
            condition.appendTo(builder, tablePrefixOrNull);
            condition.appendValuesTo(values);
        }

    }

    boolean isEmpty() {
        return this.whereConditions.isEmpty();
    }
}
