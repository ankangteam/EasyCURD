package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.PropertiesUtils;
import com.easyjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;

public class BuildTable {
    private static Connection connection = null;
    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);
    private static String SQL_SHOW_TABLE_STATUS = "show table status";
    private static String SQL_SHOW_TABLE_FIELDS = "show full fields from %s";
    private static String SQL_SHOW_TABLE_INDEX = "show index from %s";


    static {
        String url = PropertiesUtils.getProperty("db.url");
        String username = PropertiesUtils.getProperty("db.username");
        String password = PropertiesUtils.getProperty("db.password");
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            logger.info("数据库链接失败");
        }
    }
    public static List<TableInfo> getTables(){
        PreparedStatement preparedStatement = null;
        ResultSet tableResult = null;

        List<TableInfo> tableInfoList = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(SQL_SHOW_TABLE_STATUS);
            tableResult = preparedStatement.executeQuery();
            while (tableResult.next()) {
                String tableName = tableResult.getString("name");
                String comment = tableResult.getString("comment");

                String beanName = tableName;
                if (Constants.IGNORE_TABLE_PREFIX){
                    beanName = tableName.substring(tableName.indexOf("_")+ 1);
                }
                beanName = processFiled(beanName,true);

                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(tableName);
                tableInfo.setBeanName(beanName);
                tableInfo.setComment(comment);
                tableInfo.setBeanParamName(beanName + Constants.SUFFIX_BEAN_QUERY);

                readFieldInfo(tableInfo);
                getKeyIndexInfo(tableInfo);
                tableInfoList.add(tableInfo);
            }

        } catch (Exception e) {
            logger.info("读取表失败");
        } finally {
            if (tableResult != null) {
                try {
                    tableResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return tableInfoList;
    }

    private static void readFieldInfo(TableInfo tableInfo) {
        PreparedStatement preparedStatement = null;
        ResultSet fieldResult = null;

        boolean haveDateTime = false;
        boolean haveDate = false;
        boolean haveBigDecimal = false;

        List<FieldInfo> fieldInfoList = new ArrayList<>();
        List<FieldInfo> fieldExtendList = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(String.format(SQL_SHOW_TABLE_FIELDS, tableInfo.getTableName()));
            fieldResult = preparedStatement.executeQuery();
            while (fieldResult.next()) {
                String field = fieldResult.getString("field");
                String type = fieldResult.getString("type");
                String extra = fieldResult.getString("extra");
                String comment = fieldResult.getString("comment");
                if (type.indexOf("(")>0){
                    type = type.substring(0, type.indexOf("("));
                }
                String propertyName = processFiled(field,false);

                FieldInfo fieldInfo = new FieldInfo();
                fieldInfoList.add(fieldInfo);

                fieldInfo.setFieldName(field);
                fieldInfo.setSqlType(type);
                fieldInfo.setComment(comment);
                fieldInfo.setAutoIncrement("auto_increment".equalsIgnoreCase(extra));
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setJavaType(processJavaType(type));

                if(ArrayUtils.contains(Constants.SQL_DATE_TYPES,type)){
                    haveDate = true;
                }
                if(ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,type)){
                    haveDateTime = true;
                }
                if(ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE,type)){
                    haveBigDecimal = true;
                }
                if(ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)){
                    FieldInfo fuzzyfield = new FieldInfo();
                    fuzzyfield.setJavaType(fieldInfo.getJavaType());
                    fuzzyfield.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_FUZZY);
                    fuzzyfield.setFieldName(fieldInfo.getFieldName());
                    fuzzyfield.setSqlType(type);
                    fieldExtendList.add(fuzzyfield);
                }
                if(ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,type) || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,type)){
                    FieldInfo timeStartField = new FieldInfo();
                    timeStartField.setJavaType("String");
                    timeStartField.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_TIME_START);
                    timeStartField.setFieldName(fieldInfo.getFieldName());
                    timeStartField.setSqlType(type);
                    fieldExtendList.add(timeStartField);

                    FieldInfo timeEndField = new FieldInfo();
                    timeEndField.setJavaType("String");
                    timeEndField.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_TIME_END);
                    timeEndField.setFieldName(fieldInfo.getFieldName());
                    timeEndField.setSqlType(type);
                    fieldExtendList.add(timeEndField);
                }

            }
            tableInfo.setHaveDate(haveDate);
            tableInfo.setHaveDateTime(haveDateTime);
            tableInfo.setHaveBigdecimal(haveBigDecimal);
            tableInfo.setFieldList(fieldInfoList);
            tableInfo.setFieldExtendList(fieldExtendList);
        } catch (Exception e) {
            logger.info("读取表失败");
        } finally {
            if (fieldResult != null) {
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void getKeyIndexInfo(TableInfo tableInfo) {
        PreparedStatement preparedStatement = null;
        ResultSet fieldResult = null;

        List<FieldInfo> fieldInfoList = new ArrayList<>();
        try {
            Map<String,FieldInfo> tempMap = new HashMap<>();
            for(FieldInfo fieldInfo:tableInfo.getFieldList()){
                tempMap.put(fieldInfo.getFieldName(),fieldInfo);
            }

            preparedStatement = connection.prepareStatement(String.format(SQL_SHOW_TABLE_INDEX , tableInfo.getTableName()));
            fieldResult = preparedStatement.executeQuery();
            while (fieldResult.next()) {
                boolean isUnique = (fieldResult.getInt("non_unique") == 0);
                if (!isUnique) {
                    continue;
                }
                String keyName = fieldResult.getString("key_name");
                String columnName = fieldResult.getString("column_name");
                FieldInfo fieldInfo = tempMap.get(columnName);
                if (fieldInfo == null) {
                    continue;
                }
                tableInfo.getKeyIndexMap()
                        .computeIfAbsent(keyName, k -> new ArrayList<>())
                        .add(fieldInfo);
            }
        } catch (Exception e) {
            logger.info("读取索引失败");
        } finally {
            if (fieldResult != null) {
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private static String processFiled(String filed,Boolean upperCaseFirstLetter) {
        StringBuffer sb = new StringBuffer();
        String[] fields = filed.split("_");
        sb.append(upperCaseFirstLetter? StringUtils.upperCaseFirstLetter(fields[0]) : fields[0]);
        for (int i = 1; i < fields.length; i++) {
            sb.append(StringUtils.upperCaseFirstLetter(fields[i]));
        }
        return sb.toString();
    }

    private static String processJavaType(String type){
        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE, type)){
            return "Integer";
        } else if (ArrayUtils.contains(Constants.SQL_LONG_TYPE,type)) {
            return  "Long";
        } else if (ArrayUtils.contains(Constants.SQL_STRING_TYPE,type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE,type)) {
            return "BigDecimal";
        } else if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,type)) {
            return "Date";
        } else {
            throw new RuntimeException("无法识别的类型"+type);
        }
    }
    public static void main(String[] args) {
        System.out.println(Constants.PATH_PO);
    }
}
