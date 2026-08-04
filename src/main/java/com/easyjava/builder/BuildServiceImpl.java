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
import java.util.Map;

public class BuildServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(BuildService.class);
    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_SERVICE_IMPL);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String interfaceName = tableInfo.getBeanName() + "Service";
        String className = tableInfo.getBeanName() + "ServiceImpl";
        String mapperName = tableInfo.getBeanName() + Constants.SUFFIX_MAPPERS;
        String mapperBeanName = StringUtils.lowerCaseFirstLetter(mapperName);


        File poFile = new File(folder, className + ".java");


        try (OutputStream out = new FileOutputStream(poFile);
             OutputStreamWriter outw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(outw)
        ) {
            bw.write("package " + Constants.PACKAGE_SERVICE_IMPL + ";");
            bw.newLine();
            bw.newLine();

            //导包
            bw.write("import org.springframework.stereotype.Service;\nimport jakarta.annotation.Resource;\nimport java.util.List;\nimport java.util.Collections;");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_PO + "." + tableInfo.getBeanName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_QUERY + "." + tableInfo.getBeanParamName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_MAPPERS + "." + mapperName + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_VO + ".PaginationResultVO;");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_SERVICE + "." + interfaceName + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_QUERY + ".SimplePage;");


            bw.newLine();
            BuildComment.createClassComment(bw,className);
            bw.newLine();
            bw.write("@Service(\"" + StringUtils.lowerCaseFirstLetter(interfaceName) +"\")");
            bw.newLine();
            bw.write("public class " + className + " implements " + interfaceName + "{");
            bw.newLine();
            bw.write("\t@Resource\n" + "\tprivate " + mapperName +" "+ mapperBeanName +";");


            bw.newLine();
            BuildComment.createFieldComment(bw,"根据条件查询列表");
            bw.write("\tpublic List<" + tableInfo.getBeanName() + "> findListByParam(" + tableInfo.getBeanParamName() + " query){");
            bw.newLine();
            bw.write("\t\treturn this." + mapperBeanName + ".selectList(query);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            BuildComment.createFieldComment(bw,"根据条件查询数量");
            bw.write("\tpublic Integer findCountByParam(" + tableInfo.getBeanParamName() + " query){");
            bw.newLine();
            bw.write("\t\treturn this." + mapperBeanName + ".selectCount(query);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            BuildComment.createFieldComment(bw,"分页查询");
            bw.write("\tpublic PaginationResultVO<" + tableInfo.getBeanName() + "> findListByPage(" + tableInfo.getBeanParamName() + " query){");
            bw.newLine();
            bw.write("\t\tInteger count = this.findCountByParam(query);");
            bw.newLine();
            bw.write("\t\tPaginationResultVO<" + tableInfo.getBeanName() + "> result = new PaginationResultVO<>();");
            bw.newLine();
            bw.write("\t\tresult.setTotalCount(count);");
            bw.newLine();
            bw.write("\t\tSimplePage page = new SimplePage(query.getPageNo(),query.getPageSize(),count);");
            bw.newLine();
            bw.write("\t\tList<" + tableInfo.getBeanName() + "> list = count > 0 ? this.findListByParam(query) : Collections.emptyList();");
            bw.newLine();
            bw.write("\t\tresult.setPageNo(page.getPageNo());result.setPageSize(page.getPageSize());result.setPageTotal(page.getTotalPage());result.setList(list);");
            bw.newLine();
            bw.write("\t\treturn result;");
            bw.write("\n\t}");
            bw.newLine();

            BuildComment.createFieldComment(bw,"新增");
            bw.write("\tpublic Integer add(" + tableInfo.getBeanName() + " bean){");
            bw.newLine();
            bw.write("\t\treturn this." + mapperBeanName + ".insert(bean);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            BuildComment.createFieldComment(bw,"批量新增");
            bw.write("\tpublic Integer addBatch(List<" + tableInfo.getBeanName() + "> listBean){");
            bw.newLine();
            bw.write("\t\tif (listBean == null || listBean.isEmpty()){\n\t\t\treturn 0;\n\t\t}");
            bw.newLine();
            bw.write("\t\treturn this." + mapperBeanName + ".insertBatch(listBean);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            BuildComment.createFieldComment(bw,"批量新增或更新");
            bw.write("\tpublic Integer addOrUpdateBatch(List<" + tableInfo.getBeanName() + "> listBean){");
            bw.newLine();
            bw.write("\t\tif (listBean == null || listBean.isEmpty()){\n\t\t\treturn 0;\n\t\t}");
            bw.newLine();
            bw.write("\t\treturn this." + mapperBeanName + ".insertOrUpdateBatch(listBean);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();


            for (Map.Entry<String, List<FieldInfo>> entry : tableInfo.getKeyIndexMap().entrySet()) {
                List<FieldInfo> fieldList = entry.getValue();

                int index = 0;
                StringBuffer methodName = new StringBuffer();
                StringBuffer methodParam = new StringBuffer();
                String fieldName = "";

                for (FieldInfo fieldInfo : fieldList) {
                    index++;
                    methodName.append(StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName()));
                    if (index < fieldList.size()) {
                        methodParam.append("And");
                    }
                    methodParam.append(fieldInfo.getJavaType()).append(" ").append(fieldInfo.getPropertyName());
                    fieldName = fieldInfo.getPropertyName();
                }
                bw.newLine();
                BuildComment.createFieldComment(bw, "根据" + methodName + "查询");
                bw.write("\tpublic " + tableInfo.getBeanName() + " getBy" + methodName + "(" + methodParam + "){");
                bw.newLine();
                bw.write("\t\treturn " + mapperBeanName + ".selectBy" + methodName + "(" + fieldName + ");");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();

                BuildComment.createFieldComment(bw, "根据" + methodName + "更新");
                bw.write("\tpublic Integer updateBy" + methodName + "("+ tableInfo.getBeanName() + " bean," + methodParam + "){");
                bw.newLine();
                bw.write("\t\treturn this." + mapperBeanName + ".updateBy" + methodName + "(bean," +  fieldName + ");");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();

                BuildComment.createFieldComment(bw, "根据" + methodName + "删除");
                bw.write("\tpublic Integer deleteBy" + methodName + "(" + methodParam + "){");
                bw.newLine();
                bw.write("\t\treturn this." + mapperBeanName + ".deleteBy" + methodName + "(" + fieldName + ");");
                bw.newLine();
                bw.write("\t}");
            }


            bw.newLine();
            bw.write("}");
        } catch (Exception e) {
            logger.error("创建po失败", e);
        }
    }
}
