package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BuildMapper {
    private static final Logger logger = LoggerFactory.getLogger(BuildQuery.class);
    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPERS);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPERS;

        File poFile = new File(folder, className + ".java");

        try (OutputStream out = new FileOutputStream(poFile);
             OutputStreamWriter outw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(outw)) {

            bw.write("package " + Constants.PACKAGE_MAPPERS + ";");
            bw.newLine();
            bw.newLine();

            //导包
            bw.write("import org.apache.ibatis.annotations.Param;" +
                    "\nimport " + Constants.PACKAGE_PO +"."+tableInfo.getBeanName() +
                    ";\nimport "+ Constants.PACKAGE_QUERY +"."+tableInfo.getBeanParamName() + ";") ;
            bw.newLine();
            bw.newLine();
            //构建类注释
            BuildComment.createClassComment(bw, tableInfo.comment + "Mapper");
            bw.write("public interface " + className + " extends BaseMapper<" + tableInfo.getBeanName() + "," + tableInfo.getBeanParamName() + "> {");
            bw.newLine();

            Map<String,List<FieldInfo>> keyMap = tableInfo.getKeyIndexMap();

            for (Map.Entry<String, List<FieldInfo>> entry : keyMap.entrySet()) {
                List<FieldInfo> fieldList = entry.getValue();

                Integer index = 0;
                StringBuffer methodName = new StringBuffer();

                StringBuffer methodParam = new StringBuffer();

                for (FieldInfo fieldInfo : fieldList) {
                    index++;
                    methodName.append(StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName()));
                    if(index < fieldList.size()) {
                        methodParam.append(", ");
                    }
                    methodParam.append("@Param(\"").append(fieldInfo.getPropertyName()).append("\")").append(fieldInfo.getJavaType()).append(" ").append(fieldInfo.getPropertyName());
                }
                BuildComment.createFieldComment(bw,"根据" + methodName + "查询");
                bw.write("\t"+ tableInfo.getBeanName() +" selectBy" + methodName + "(" + methodParam + ");");
                bw.newLine();
                bw.newLine();

                BuildComment.createFieldComment(bw,"根据" + methodName + "更新新");
                bw.write("\tInteger updateBy" + methodName + "(@Param(\"bean\")"+ tableInfo.getBeanName() + " t, "+ methodParam + ");");
                bw.newLine();
                bw.newLine();

                BuildComment.createFieldComment(bw,"根据" + methodName + "删除");
                bw.write("\tInteger deleteBy" + methodName + "(" + methodParam + ");");
                bw.newLine();
                bw.newLine();

            }
            bw.write("}");
            bw.flush();
        } catch (Exception e) {
            logger.error("创建Mapper失败", e);
        }
    }
}
