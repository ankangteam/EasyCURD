package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BuildMapperXml {
    private static final Logger logger = LoggerFactory.getLogger(BuildQuery.class);

    private static final String BASE_COLUMN_LIST ="base_column_list";
    private static final String BASE_QUERY_CONDITION="base_query_condition";
    private static final String BASE_QUERY_CONDITION_EXTEND="base_query_condition_extend";
    private static final String QUERY_CONDITION ="query_condition";

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPERS_XMLS);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPERS ;
        File poFile = new File(folder, className + ".xml");

        try (OutputStream out = new FileOutputStream(poFile);
             OutputStreamWriter outw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(outw)) {
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bw.newLine();
            bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"");
            bw.newLine();
            bw.write("\t\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
            bw.newLine();
            bw.write("<mapper namespace=\"" +Constants.PACKAGE_MAPPERS +"."+ className + "\">");
            bw.newLine();

            bw.write("\t<!--实体映射-->");
            bw.newLine();
            String poClass = Constants.PACKAGE_PO + "." + tableInfo.getBeanName();
            bw.write("\t<resultMap id=\"base_result_map\" type=\"" + poClass + "\">");


            FieldInfo isField = null;
            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                if("PRIMARY".equals(entry.getKey())){
                    List<FieldInfo> fieldList = entry.getValue();
                    if(fieldList.size() == 1){
                        isField = fieldList.get(0);
                        break;
                    }
                }
            }
            for(FieldInfo fieldInfo : tableInfo.getFieldList()){
                bw.write("\t\t<!-- " + fieldInfo.getComment() + "-->");
                bw.newLine();
                String key ="";
                if (isField != null && fieldInfo.getPropertyName().equals(isField.getPropertyName())) {
                    key = "id";
                } else {
                    key = "result";
                }
                bw.write("\t\t<" + key + " column=\"" + fieldInfo.getFieldName() + "\" property=\"" +fieldInfo.getPropertyName() +"\"/>");
                bw.newLine();
            }
            bw.write("\t</resultMap>");
            bw.newLine();

            //通用查询列
            bw.write("\t<!--通用查询结果列-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + BASE_COLUMN_LIST + "\">");
            bw.newLine();
            StringBuilder columnBuilder = new StringBuilder();
            for(FieldInfo fieldInfo : tableInfo.getFieldList()) {
                columnBuilder.append(fieldInfo.getFieldName()).append(",");
            }
            String columnBuilderStr = columnBuilder.substring(0, columnBuilder.lastIndexOf(","));
            bw.write("\t\t" + columnBuilderStr);
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();

            //基础查询条件
            bw.write("\t<!--基础查询条件-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + BASE_QUERY_CONDITION + "\">");
            for(FieldInfo fieldInfo : tableInfo.getFieldList()) {
                String stringQuery = "";
                if(ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType()) ) {
                    stringQuery = " and query." + fieldInfo.getPropertyName() + "!=''" ;
                }
                bw.newLine();
                bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + "!=null" + stringQuery + "\">");
                bw.newLine();
                bw.write("\t\t\tand " + fieldInfo.getFieldName() + "= #{query." + fieldInfo.getPropertyName() + "}");
                bw.newLine();
                bw.write("\t\t</if>");
            }bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();

            //扩展查询条件
            bw.write("\t<!--扩展查询条件-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + BASE_QUERY_CONDITION_EXTEND + "\">");
            bw.newLine();
            for(FieldInfo fieldInfo : tableInfo.getFieldExtendList()) {
                String andWhere = "";
                if(ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType()) ) {
                    andWhere = "and " + fieldInfo.getFieldName()+ " like concat('%', #{query." + fieldInfo.getPropertyName() + "},  '%')" ;
                } else if(ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType()) || ArrayUtils.contains(Constants.SQL_DATE_TYPES, fieldInfo.getSqlType()) ) {
                    if(fieldInfo.getPropertyName() .endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_START)) {
                        andWhere = "<![CDATA[ and " + fieldInfo.getFieldName() + " >= str_to_date(#{query." + fieldInfo.getPropertyName() + "},'%Y-%m-%d') ]]>";
                    } else if (fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_END )) {
                        andWhere = "<![CDATA[ and " + fieldInfo.getFieldName() + " < date_sub(str_to_date(#{query" + fieldInfo.getPropertyName() + "},'%Y-%m-%d'), interval -1 day) ]]>";
                    }
                }
                bw.newLine();
                bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + "!=null and query." + fieldInfo.getPropertyName() + "!=''\">");
                bw.newLine();
                bw.write("\t\t\t" + andWhere);
                bw.newLine();
                bw.write("\t\t</if>");
            }
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();
            bw.newLine();

            //通用查询条件
            bw.write("\t<!--通用查询条件-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + QUERY_CONDITION + "\">");
            bw.newLine();
            bw.write("\t\t<where>");
            bw.newLine();
            bw.write("\t\t\t<include refid=\"" + BASE_QUERY_CONDITION +"\" />");
            bw.newLine();
            bw.write("\t\t\t<include refid=\"" + BASE_QUERY_CONDITION_EXTEND +"\" />");
            bw.newLine();
            bw.write("\t\t</where>");
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();


            //查询列表
            bw.write("\t<!--查询列表-->");
            bw.newLine();
            bw.write("\t<select id=\"selectList\" resultMap=\"base_result_map\">");
            bw.newLine();
            bw.write("\t\tSELECT <include refid=\"" + BASE_COLUMN_LIST + "\"/> FROM " + tableInfo.getTableName() + " <include refid=\"" +QUERY_CONDITION + "\"/>");
            bw.newLine();
            bw.write("\t\t<if test=\"query.orderBy!=null\">order by ${query.orderBy}</if>");
            bw.newLine();
            bw.write("\t\t<if test=\"query.orderBy!=null\">limit #{query.simplePage},#{query.simplePage.end}</if>");
            bw.newLine();
            bw.write("\t</select>");
            bw.newLine();

            //查询数量
            bw.write("\t<!--查询数量-->");
            bw.newLine();
            bw.write("\t<select id=\"selectCount\" resultType=\"java.lang.Integer\">");
            bw.newLine();
            bw.write("\t\tSELECT count(1) FROM " + tableInfo.getTableName() + " <include refid=\"" + QUERY_CONDITION + "\"/>");
            bw.newLine();
            bw.write("\t</select>");
            bw.newLine();

            //单条插入
            bw.write("\t<!--单条插入-->");
            bw.newLine();
            bw.write("\t<insert id=\"insert\" parameterType=\"" + poClass + "\">");
            bw.newLine();
            FieldInfo autoIncrement = null;
            for (FieldInfo fieldInfo : tableInfo.getFieldList()){
                if(fieldInfo.getAutoIncrement()!=null && fieldInfo.getAutoIncrement()) {
                    autoIncrement = fieldInfo;
                    break;
                }
            }
            if(autoIncrement != null) {
                bw.write("\t\t<selectKey keyProperty=\"bean." + autoIncrement.getFieldName() + "\" resultType=\"Long\" order=\"AFTER\">");
                bw.newLine();
                bw.write("\t\t\tSELECT LAST_INSERT_ID();");
                bw.newLine();
                bw.write("\t\t</selectKey>");
                bw.newLine();
            }
            bw.write("\t\t\tINSERT INTO " + tableInfo.getTableName());
            bw.newLine();
            bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.newLine();
                bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " !=null\">");
                bw.newLine();
                bw.write("\t\t\t\t" + fieldInfo.getFieldName() +",");
                bw.newLine();
                bw.write("\t\t\t</if>");
            }
            bw.newLine();
            bw.write("\t\t</trim>");
            bw.newLine();
            bw.write("\t\t<trim prefix=\"value (\" suffix=\")\" suffixOverrides=\",\">");
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.newLine();
                bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " !=null\">");
                bw.newLine();
                bw.write("\t\t\t\t#{bean." + fieldInfo.getPropertyName() + "},");
                bw.newLine();
                bw.write("\t\t\t</if>");
            }
            bw.newLine();
            bw.write("\t\t</trim>");
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();

            //插入或更新
            bw.write("\t<!--插入或更新-->");
            bw.newLine();
            bw.write("\t<insert id=\"insertOrUpdate\" parameterType=\"" + poClass + "\">");
            bw.newLine();

            bw.write("\t\t\tINSERT INTO " + tableInfo.getTableName());
            bw.newLine();
            bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.newLine();
                bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " !=null\">");
                bw.newLine();
                bw.write("\t\t\t\t" + fieldInfo.getFieldName() +",");
                bw.newLine();
                bw.write("\t\t\t</if>");
            }
            bw.newLine();
            bw.write("\t\t</trim>");
            bw.newLine();
            bw.write("\t\t<trim prefix=\"value (\" suffix=\")\" suffixOverrides=\",\">");
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.newLine();
                bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " !=null\">");
                bw.newLine();
                bw.write("\t\t\t\t#{bean." + fieldInfo.getPropertyName() + "},");
                bw.newLine();
                bw.write("\t\t\t</if>");
            }
            bw.newLine();
            bw.write("\t\t</trim>");
            bw.newLine();
            bw.write("\t\t on DUPLICATE KEY UPDATE ");
            bw.newLine();


            Map<String, String> tempMap= new HashMap<>();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                List<FieldInfo> fieldInfoList= entry.getValue();
                    for(FieldInfo item : fieldInfoList) {
                        tempMap.put(item.getFieldName(), item.getFieldName());
                    }
            }

            bw.write("\t\t<trim suffixOverrides=\",\">");
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (tempMap.get(fieldInfo.getFieldName()) != null) {
                    continue;
                }
                bw.newLine();
                bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " !=null\">");
                bw.newLine();
                bw.write("\t\t\t\t " + fieldInfo.getFieldName() + " = VALUES(" + fieldInfo.getFieldName() + "),");
                bw.newLine();
                bw.write("\t\t\t</if>");
            }
            bw.newLine();
            bw.write("\t\t</trim>");
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();


            //批量插入
            bw.write("\t<!--批量插入-->");
            bw.newLine();
            bw.write("\t<insert id=\"insertBatch\">");
            bw.newLine();
            StringBuffer sbField = new StringBuffer();
            StringBuffer sbProperty = new StringBuffer();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (fieldInfo.getAutoIncrement()) {
                    continue;
                }
                sbProperty.append("#{item.").append(fieldInfo.getPropertyName()).append("},");
                sbField.append(fieldInfo.getFieldName()).append(",");
            }
            String sbstr = sbField.substring(0,sbField.lastIndexOf(","));
            bw.write("\t\tINSERT INTO " + tableInfo.getTableName() + "(" + sbstr + ")");
            bw.newLine();
            bw.write("\t\tVALUES");
            bw.newLine();
            bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">");
            bw.newLine();
            String sbPropertystr = sbProperty.substring(0,sbProperty.lastIndexOf(","));
            bw.write("\t\t\t(" +  sbPropertystr + ")");
            bw.newLine();
            bw.write("\t\t</foreach>");
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();


            //批量插入或更新
            bw.write("\t<!--批量插入或更新-->");
            bw.newLine();
            bw.write("\t<insert id=\"insertOrUpdateBatch\">");
            bw.newLine();
            bw.write("\t\tINSERT INTO " + tableInfo.getTableName() + "(" + sbstr + ")");
            bw.newLine();
            bw.write("\t\tVALUES");
            bw.newLine();
            bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">");
            bw.newLine();
            bw.write("\t\t\t(" +  sbPropertystr + ")");
            bw.newLine();
            bw.write("\t\t</foreach>");
            bw.newLine();
            StringBuffer sb = new StringBuffer();
            StringJoiner sj = new StringJoiner(",");
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (fieldInfo.getAutoIncrement()) {
                    continue;
                }
                sj.add(fieldInfo.getFieldName() + "= VALUES(" + fieldInfo.getFieldName() + ")");
            }
            sb.append(sj);
            bw.write("\t\ton DUPLICATE KEY UPDATE " + sb);
            bw.newLine();
            bw.write("\t</insert>");

            //根据主键查询
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                List<FieldInfo> fieldList = entry.getValue();

                Integer index = 0;
                StringBuffer methodName = new StringBuffer();
                StringBuffer paramName = new StringBuffer();

                for (FieldInfo fieldInfo : fieldList) {
                    index++;
                    methodName.append(StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName()));
                    paramName.append(fieldInfo.getFieldName() + "=#{" + fieldInfo.getPropertyName() + "}");
                    if(index < fieldList.size()) {
                        methodName.append("And");
                        paramName.append(" and ");
                    }
                }
                bw.newLine();
                bw.write("\t<!--根据" + methodName + "查询-->");
                bw.newLine();
                bw.write("\t<select id=\"selectBy" + methodName + "\" resultMap=\"base_result_map\">");
                bw.newLine();
                bw.write("\t\tselect <include refid=\""+ BASE_COLUMN_LIST +"\"/> from " + tableInfo.getTableName() + " where " + paramName);
                bw.newLine();
                bw.write("\t</select>");



                //根据更新
                bw.newLine();
                bw.write("\t<!--根据" + methodName + "更新-->");
                bw.newLine();
                bw.write("\t<update id=\"updateBy" + methodName + "\" parameterType=\"" + poClass + "\">");
                bw.newLine();
                bw.write("\t\tupdate " + tableInfo.getTableName());
                bw.newLine();
                bw.write("\t\t<set>");
                for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                    if (fieldInfo.getAutoIncrement()) {
                        continue;
                    }
                    bw.newLine();
                    bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " != null\">");
                    bw.newLine();
                    bw.write("\t\t\t" + fieldInfo.getFieldName() + "=#{bean." + fieldInfo.getPropertyName() + "},");
                    bw.newLine();
                    bw.write("\t\t\t</if>");
                }
                bw.newLine();
                bw.write("\t\t</set>");
                bw.newLine();
                bw.write("\t\twhere " + paramName);
                bw.newLine();
                bw.write("\t</update>");
                bw.newLine();


                //根据删除
                bw.write("\t<!--根据" + methodName + "删除-->");
                bw.newLine();
                bw.write("\t<delete id=\"deleteBy" + methodName + "\">");
                bw.newLine();
                bw.write("\t\tdelete from " + tableInfo.getTableName() + " where " + paramName);
                bw.newLine();
                bw.write("\t</delete>");

            }


            bw.newLine();
            bw.write("</mapper>");
        } catch (Exception e) {
            logger.error("创建Mapperxml失败", e);
        }
    }
}






















