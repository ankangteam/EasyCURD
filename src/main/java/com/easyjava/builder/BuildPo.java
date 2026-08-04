package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.DateUtils;
import org.apache.commons.lang3.ArrayUtils;
import com.easyjava.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BuildPo {
    private static final Logger logger = LoggerFactory.getLogger(BuildPo.class);
    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_PO);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File poFile = new File(folder, tableInfo.getBeanName() + ".java");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            bw = new BufferedWriter(outw);

            bw.write("package " + Constants.PACKAGE_PO +";");
            bw.newLine();
            bw.newLine();

            bw.write("import java.io.Serializable;");
            bw.newLine();
            if(tableInfo.getHaveDate() || tableInfo.getHaveDateTime()){
                bw.write("import java.util.Date;");
                bw.newLine();
                bw.write(Constants.BEAN_DATE_FORMAT_CLASS + ";");
                bw.newLine();
                bw.write(Constants.BEAN_DATE_UNFORMAT_CLASS + ";");
                bw.newLine();
                bw.write("import " + Constants.PACKAGE_ENUMS + ".DateTimePatternEnum;");
                bw.newLine();
                bw.write("import " + Constants.PACKAGE_UTILS+ ".DateUtils;");
                bw.newLine();
            }

            //忽略属性
            boolean haveIgnoreBean = false;
            for (FieldInfo field : tableInfo.getFieldList()){
                if(ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FILED.split("\\."),field.getPropertyName())){
                    haveIgnoreBean = true;
                    break;
                }
            }
            if(haveIgnoreBean){
                bw.write(Constants.BEAN_DATE_FORMAT_CLASS + ";");
                bw.newLine();
            }



            if(tableInfo.getHaveBigdecimal()){
                bw.write("import java.math.BigDecimal;");
            }
            bw.newLine();
            bw.newLine();
            //构建类注释
            BuildComment.createClassComment(bw,tableInfo.comment);
            bw.write("public class " + tableInfo.getBeanName() + " implements Serializable {");
            bw.newLine();

            for(FieldInfo field:tableInfo.getFieldList()){
                BuildComment.createFieldComment(bw,field.getComment());
                if(ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, field.getSqlType())){
                    bw.write("\t" + String.format(Constants.BEAN_DATE_FORMAT_EXPRESSION, DateUtils.YYYY_MM_DD_HH_MM_SS));
                    bw.newLine();
                    bw.write("\t" + String.format(Constants.BEAN_DATE_UNFORMAT_EXPRESSION, DateUtils.YYYY_MM_DD_HH_MM_SS)) ;
                    bw.newLine();
                }
                if(ArrayUtils.contains(Constants.SQL_DATE_TYPES, field.getSqlType())){
                    bw.write("\t" + String.format(Constants.BEAN_DATE_FORMAT_EXPRESSION, DateUtils.YYYY_MM_DD));
                    bw.newLine();
                    bw.write("\t" + String.format(Constants.BEAN_DATE_UNFORMAT_EXPRESSION, DateUtils.YYYY_MM_DD));
                    bw.newLine();
                }
                if(ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FILED.split("\\."),field.getPropertyName())){
                    bw.write("\t" + String.format(Constants.IGNORE_BEAN_TOJSON_EXPRESSION, DateUtils.YYYY_MM_DD));
                    bw.newLine();
                }


                bw.write("\tprivate"+ " "+ field.getJavaType()+ " " + field.getPropertyName()+";");
                bw.newLine();
                bw.newLine();
            }
            //setter and getter
            for(FieldInfo field:tableInfo.getFieldList()){
                String tempField = StringUtils.upperCaseFirstLetter(field.getPropertyName());
                bw.write("\tpublic void set" + tempField+ "(" + field.getJavaType() + " " + field.getPropertyName() + "){");
                bw.newLine();
                bw.write("\t\tthis." + field.getPropertyName() + " = " + field.getPropertyName() + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();

                bw.write("\tpublic "+ field.getJavaType()+" get" + tempField+ "(){");
                bw.newLine();
                bw.write("\t\treturn this." + field.getPropertyName() + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
            }
            //重写toString
            StringBuffer toString = new StringBuffer();

            for(FieldInfo field:tableInfo.getFieldList()){
                String properName = field.getPropertyName();
                if(ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, field.getSqlType())){
                    properName = "DateUtils.format("+properName+",DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())";
                }else if(ArrayUtils.contains(Constants.SQL_DATE_TYPES, field.getSqlType())){
                    properName = "DateUtils.format("+properName+",DateTimePatternEnum.YYYY_MM_DD.getPattern())";
                }
                toString.append("\"").append(field.getComment()).append(":\"+(").append(field.getPropertyName()).append("==null?\"空\":").append(properName).append(")+\",\"+");
            }
            toString.deleteCharAt(toString.length()-1);
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic String toString() {");
            bw.newLine();
            bw.write("\t\treturn " + toString + ";");
            bw.newLine();
            bw.write("\t}");

            bw.write("}");
            bw.flush();
        }catch (Exception e){
            logger.error("创建po失败",e);
        }finally {
            if(bw!=null){
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(outw!=null){
                try {
                    outw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
