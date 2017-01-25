/*
 * ****************************************************************************
 *   Copyright (C) 2005-2016 UCWEB Corporation. All rights reserved
 *   File        : SqlUtils.java
 *   Description :
 *
 *   Creation    : 16-11-11
 *   Author      : lanzh@ucweb.com
 *   History     : Creation, 16-11-11, lanzh, Create the file
 * ****************************************************************************
 */
package com.papapa.database;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.internal.DaoConfig;

public class SqlUtils {

    public static String createTableSql(DaoConfig aDaoConfig){

        String tableName = aDaoConfig.tablename;
        DaoProperty[] properties = new DaoProperty[aDaoConfig.properties.length];
        for (int i = 0; i < aDaoConfig.properties.length; i++) {
            properties[i] = (DaoProperty)aDaoConfig.properties[i];
        }

        if (properties == null || properties.length <= 0) {
            return null;
        }

        List<DaoProperty> keyList = new ArrayList<DaoProperty>();
        for(DaoProperty property : properties) {
            if (property.primaryKey) {
                keyList.add(property);
            }
        }

        StringBuilder builder = new StringBuilder("CREATE TABLE ");
        builder.append("IF NOT EXISTS ");
        builder.append(tableName).append(" ");

        builder.append("(");
        int length = properties.length;
        for (int i = 0; i < length; i++) {
            DaoProperty property = properties[i];
            builder.append(property.columnName).append(" ").append(property.mTypeName);

            if (keyList.size() == 1 && property.primaryKey) {
                builder.append(" PRIMARY KEY ");
                builder.append(getPrimaryKeySuffix(property));
            } else {
                if (property.mIsNotNull) {
                    builder.append(" NOT NULL");
                }
                if (property.mIsUnique) {
                    builder.append(" UNIQUE");
                }
            }

            if (i < length - 1) {
                builder.append(",");
            }
        }

        if (keyList.size() > 1) {
            builder.append(",").append("CONSTRAINT DEFAULT_COMBINED_ID PRIMARY KEY (");
            int size = keyList.size();
            for (int i = 0; i < size; i++) {
                DaoProperty property = keyList.get(i);
                builder.append(property.columnName);
                if (i < size - 1) {
                    builder.append(",");
                }
            }
            builder.append(")");
        }

        builder.append(")");
        return builder.toString();
    }

    public static boolean checkPropertyExist(Database db, String tableName, DaoProperty property) {
        if (db == null) {
            return false;
        }
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);
            result = cursor != null && cursor.getColumnIndex(property.columnName) != -1;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public static String addDaoPropertySql(String tableName, DaoProperty property) {
        StringBuilder builder = new StringBuilder("ALTER TABLE ");
        builder.append(tableName).append(" ADD ").append(property.columnName).append(" ").append(property.mTypeName);
        if (property.mIsNotNull) {
            builder.append(" NOT NULL DEFAULT ").append(property.getDefaultValue());

        }
        if (property.mIsUnique) {
            builder.append(" UNIQUE");
        }
        return builder.toString();
    }


    public static void executeUpdateTableIndexSql(Database db, Class<? extends AbstractDao<?, ?>> daoClass, DaoConfig aDaoConfig){

        Index[] indexes = null;
        try {
            indexes = reflectIndexes(daoClass);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if(indexes == null){
            return;
        }

        for (Index index : indexes) {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE ").append(index.mType).append(" INDEX IF NOT EXISTS ").append(index.mName);
            builder.append(" ON ").append(aDaoConfig.tablename);
            builder.append("(\"");

            builder.append(index.mColumnList[0].columnName);

            for (int i = 1; i < index.mColumnList.length; i++) {
                Property property = index.mColumnList[i];
                builder.append("\" ,\"").append(property.columnName);
            }

            builder.append("\"").append(");");

            db.execSQL(builder.toString());
        }

    }

    private static Index[] reflectIndexes(Class<? extends AbstractDao<?, ?>> daoClass) throws IllegalAccessException {
        Class indexesClass = null;
        try {
            indexesClass = Class.forName(daoClass.getName() + "$Indexes");
        } catch (ClassNotFoundException aE) {
            aE.printStackTrace();
        }

        if(indexesClass == null){
            return null;
        }

        Field[] fields = indexesClass.getDeclaredFields();
        List<Index> indexList = new ArrayList<Index>();
        Field[] indexes = fields;
        int length = fields.length;

        for(int index = 0; index < length; ++index) {
            Field field = indexes[index];
            if((field.getModifiers() & 9) == 9) {
                Object fieldValue = field.get((Object)null);
                if(fieldValue instanceof Index) {
                    indexList.add((Index)fieldValue);
                }
            }
        }

        Index[] result = new Index[indexList.size()];
        result = indexList.toArray(result);

        return result;
    }

    private static String getPrimaryKeySuffix(Property aProperty){
        Class type = aProperty.type;
        String defaultStr = "AUTOINCREMENT";
        if (type.equals(Integer.class)){
            return defaultStr;
        } else {
            return " NOT NULL ";
        }
    }

    public static String createSqlUpdate(String tablename, DaoProperty[] updateColumns, Object[] values) {
        String quotedTablename = '\"' + tablename + '\"';
        StringBuilder builder = new StringBuilder("UPDATE ");
        builder.append(quotedTablename).append(" SET ");
        appendColumnsEqualPlaceholders(builder, updateColumns,values);
        return builder.toString();
    }

    private static StringBuilder appendColumnsEqualPlaceholders(StringBuilder builder, DaoProperty[] columns, Object[] values) {
        for (int i = 0; i < columns.length; i++) {
            appendColumn(builder, columns[i].columnName).append("=").append(values[i]);
            if (i < columns.length - 1) {
                builder.append(',');
            }
        }
        return builder;
    }

    private static StringBuilder appendColumnsEqValue(StringBuilder builder, String tableAlias, String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            appendColumn(builder, tableAlias, columns[i]).append("=?");
            if (i < columns.length - 1) {
                builder.append(',');
            }
        }
        return builder;
    }

    public static StringBuilder appendColumn(StringBuilder builder, String column) {
        builder.append('"').append(column).append('"');
        return builder;
    }

    private static StringBuilder appendColumn(StringBuilder builder, String tableAlias, String column) {
        builder.append(tableAlias).append(".\"").append(column).append('"');
        return builder;
    }

    public static String createSqlDelete(String tablename) {
        String quotedTablename = '\"' + tablename + '\"';
        StringBuilder builder = new StringBuilder("DELETE FROM ");
        builder.append(quotedTablename);
        return builder.toString();
    }
}
