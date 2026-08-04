package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class BuildQuery {
    private static final Logger logger = LoggerFactory.getLogger(BuildQuery.class);
    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_QUERY);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File poFile = new File(folder, tableInfo.getBeanName() + Constants.SUFFIX_BEAN_QUERY + ".java");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            bw = new BufferedWriter(outw);

            bw.write("package " + Constants.PACKAGE_QUERY +";");
            bw.newLine();
            bw.newLine();

            //导包
            if(tableInfo.getHaveDate() || tableInfo.getHaveDateTime()){
                bw.write("import java.util.Date;");
                bw.newLine();
            }

            if(tableInfo.getHaveBigdecimal()){
                bw.write("import java.math.BigDecimal;");
            }
            bw.newLine();
            bw.newLine();
            //构建类注释
            BuildComment.createClassComment(bw,tableInfo.comment + "查询");
            bw.write("public class " + tableInfo.getBeanName() + Constants.SUFFIX_BEAN_QUERY + " extends BaseQuery{");
            bw.newLine();

            for(FieldInfo field:tableInfo.getFieldList()){
                BuildComment.createFieldComment(bw,field.getComment());
                bw.write("\tprivate"+ " "+ field.getJavaType()+ " " + field.getPropertyName()+";");
                bw.newLine();
                bw.newLine();

                //String类型的参数
                if(Objects.equals(field.getJavaType(), "String")){
                    bw.write("\tprivate " + field.getJavaType() +" " +field.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_FUZZY + ";");
                    bw.newLine();
                    bw.newLine();
                }
                //日期类型
                if(Objects.equals(field.getJavaType(), "Date")){
                    bw.write("\tprivate String " + field.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_START + ";");
                    bw.newLine();
                    bw.newLine();
                    bw.write("\tprivate String " + field.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_END + ";");
                    bw.newLine();
                    bw.newLine();
                }

            }
            buildGetSet(bw,tableInfo.getFieldList());
            buildGetSet(bw,tableInfo.getFieldExtendList());
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
    //setter and getter
    private static void buildGetSet(BufferedWriter bw,List<FieldInfo> fieldInfoList)throws IOException {
        for(FieldInfo field:fieldInfoList){
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
    }
}
