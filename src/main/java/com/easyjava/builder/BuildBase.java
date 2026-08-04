package com.easyjava.builder;

import com.easyjava.bean.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class BuildBase {
    public static final Logger logger = LoggerFactory.getLogger(BuildBase.class);

    public static void execute(){
        List<String> headerInfoList = new ArrayList<>();

        //生成date枚举
        headerInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headerInfoList,"DateTimePatternEnum" , Constants.PATH_ENUMS);
        //生成utils包
        headerInfoList.clear();
        headerInfoList.add("package " + Constants.PACKAGE_UTILS);
        build(headerInfoList,"DateUtils" , Constants.PATH_UTILS );
        //生成Mapper
        headerInfoList.clear();
        headerInfoList.add("package " + Constants.PACKAGE_MAPPERS);
        build(headerInfoList,"BaseMapper" , Constants.PATH_MAPPERS);
        //生成BaseQuery
        headerInfoList.clear();
        headerInfoList.add("package " + Constants.PACKAGE_QUERY);
        build(headerInfoList,"BaseQuery" , Constants.PATH_QUERY);
        //生成分页计算
        headerInfoList.clear();
        headerInfoList.add("package " + Constants.PACKAGE_QUERY);
        build(headerInfoList,"SimplePage" , Constants.PATH_QUERY);
        //生成分页返回
        headerInfoList.clear();
        headerInfoList.add("package " + Constants.PACKAGE_VO);
        headerInfoList.add("import " + Constants.PACKAGE_QUERY + ".SimplePage");
        headerInfoList.add("import java.util.List");
        build(headerInfoList,"PaginationResultVO" , Constants.PATH_VO);
        //生成ResponseEnum
        headerInfoList.clear();
        headerInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headerInfoList,"ResponseCodeEnum" , Constants.PATH_ENUMS);
        //生成ResponseEnum
        headerInfoList.clear();
        headerInfoList.add("package " + Constants.PACKAGE_EXCEPTION);
        headerInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        build(headerInfoList,"BusinessException" , Constants.PATH_EXCEPTION);
        //生成ResponseVO
        headerInfoList.clear();
        headerInfoList.add("package " + Constants.PACKAGE_VO);
        headerInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        build(headerInfoList,"ResponseVO" , Constants.PATH_VO);
        //生成BaseController
        headerInfoList.clear();
        headerInfoList.add("package " + Constants.PACKAGE_CONTROLLER);
        headerInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        headerInfoList.add("import " + Constants.PACKAGE_VO+ ".ResponseVO");
        build(headerInfoList,"BaseController" , Constants.PATH_CONTROLLER);
        //生成GlobalExceptionHandler
        headerInfoList.clear();
        headerInfoList.add("package " + Constants.PACKAGE_CONTROLLER);
        headerInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        headerInfoList.add("import " + Constants.PACKAGE_EXCEPTION + ".BusinessException");
        headerInfoList.add("import " + Constants.PACKAGE_VO+ ".ResponseVO");
        build(headerInfoList,"GlobalExceptionHandler" , Constants.PATH_CONTROLLER);
    }

    private static void build(List<String> headerInfoList,String fileName, String outPutPath){
        File file = new File(outPutPath);
        if(!file.exists()){
            file.mkdirs();
        }
        File javaFile = new File(outPutPath ,fileName + ".java");
        try (
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                Objects.requireNonNull(BuildBase.class.getClassLoader().getResourceAsStream("template/" + fileName + ".txt")),
                                StandardCharsets.UTF_8));

                BufferedWriter bw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(javaFile),
                                StandardCharsets.UTF_8))
        ) {
            for(String head : headerInfoList){
                bw.write(head + ";");
                bw.newLine();
                if(head.contains("package")){
                    bw.newLine();
                }
            }
            bw.newLine();
            String lineinfo = null;
            while ((lineinfo = br.readLine()) != null) {
                bw.write(lineinfo);
                bw.newLine();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
