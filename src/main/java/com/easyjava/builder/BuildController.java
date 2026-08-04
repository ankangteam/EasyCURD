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

public class BuildController {
    private static final Logger logger = LoggerFactory.getLogger(BuildService.class);
    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_CONTROLLER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String className = tableInfo.getBeanName() + "Controller";
        String serviceName = tableInfo.getBeanName() +"Service";
        String serviceBeanName = StringUtils.lowerCaseFirstLetter(serviceName);


        File poFile = new File(folder, className + ".java");


        try (OutputStream out = new FileOutputStream(poFile);
             OutputStreamWriter outw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(outw)
        ) {
            bw.write("package " + Constants.PACKAGE_CONTROLLER + ";");
            bw.newLine();
            bw.newLine();

            //导包
            bw.write("""
                    import org.springframework.web.bind.annotation.*;
                    import jakarta.annotation.Resource;
                    import java.util.List;""");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_PO + "." + tableInfo.getBeanName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_QUERY + "." + tableInfo.getBeanParamName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_VO + ".ResponseVO;");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_SERVICE + "." + serviceName + ";");
            bw.newLine();

            bw.newLine();
            BuildComment.createClassComment(bw,className);
            bw.write("@RestController\n@RequestMapping(\"/"+ StringUtils.lowerCaseFirstLetter((tableInfo.getBeanName())) + "\")");
            bw.newLine();
            bw.write("public class " + className + " extends BaseController {");
            bw.newLine();
            bw.write("\t@Resource\n" + "\tprivate " + serviceName +" "+ serviceBeanName +";");
            bw.newLine();

            bw.write("\t@GetMapping\n");
            bw.write("\tpublic ResponseVO list(" + tableInfo.getBeanParamName() + " query){");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".findListByPage(query));\n\t}");
            bw.newLine();

            BuildComment.createFieldComment(bw,"新增");
            bw.write("\t@PostMapping\n");
            bw.write("\tpublic ResponseVO add(" + tableInfo.getBeanName() + " bean){");
            bw.newLine();
            bw.write("\t\tthis." + serviceBeanName + ".add(bean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(null);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            BuildComment.createFieldComment(bw,"批量新增");
            bw.write("\t@PostMapping(\"/batch\")\n");
            bw.write("\tpublic ResponseVO addBatch(@RequestBody List<" + tableInfo.getBeanName() + "> listBean){");
            bw.newLine();
            bw.write("\t\tthis." + serviceBeanName + ".addBatch(listBean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(null);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            BuildComment.createFieldComment(bw,"批量新增或更新");
            bw.write("\t@PostMapping(\"/addorupdatebatch\")\n");
            bw.write("\tpublic ResponseVO addOrUpdateBatch(@RequestBody List<" + tableInfo.getBeanName() + "> listBean){");
            bw.newLine();
            bw.write("\t\tthis." + serviceBeanName + ".addOrUpdateBatch(listBean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(null);");
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
                bw.write("\t@GetMapping(\"/" + fieldName + "/{" + fieldName + "}\")\n");
                bw.write("\tpublic ResponseVO" + " getBy" + methodName + "(@PathVariable " + methodParam + "){");
                bw.newLine();
                bw.write("\t\treturn getSuccessResponseVO(" + serviceBeanName + ".getBy" + methodName + "(" + fieldName + "));");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();

                BuildComment.createFieldComment(bw, "根据" + methodName + "更新");
                bw.write("\t@PutMapping(\"/" + fieldName + "/{" + fieldName + "}\")\n");
                bw.write("\tpublic ResponseVO updateBy" + methodName + "(@RequestBody "+ tableInfo.getBeanName() + " bean,@PathVariable " + methodParam + "){");
                bw.newLine();
                bw.write("\t\tthis." + serviceBeanName + ".updateBy" + methodName + "(bean," +  fieldName + ");");
                bw.newLine();
                bw.write("\t\treturn getSuccessResponseVO(null);");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();

                BuildComment.createFieldComment(bw, "根据" + methodName + "删除");
                bw.write("\t@DeleteMapping(\"/" + fieldName + "/{" + fieldName + "}\")\n");
                bw.write("\tpublic ResponseVO deleteBy" + methodName + "(@PathVariable " + methodParam + "){");
                bw.newLine();
                bw.write("\t\tthis." + serviceBeanName + ".deleteBy" + methodName + "(" + fieldName + ");");
                bw.newLine();
                bw.write("\t\treturn getSuccessResponseVO(null);");
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
