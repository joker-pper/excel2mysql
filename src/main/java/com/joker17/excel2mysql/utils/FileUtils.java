package com.joker17.excel2mysql.utils;

import java.io.File;

public class FileUtils {

    private FileUtils() {
    }

    /**
     * mkdirs
     *
     * @param parentFile
     */
    public static void mkdirs(File parentFile) {
        if (parentFile == null) {
            return;
        }
        if (!parentFile.exists() || !parentFile.isDirectory()) {
            parentFile.mkdirs();
        }
    }

    /**
     * 获取文件是否存在
     *
     * @param file
     * @return
     */
    public static boolean isFileAndExists(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        return true;
    }

    /**
     * 获取输入excel文件对象
     *
     * @param inPath
     * @param fileName
     * @param excelType
     * @return
     */
    public static File getInExcelFile(String inPath, String fileName, String excelType) {
        StringBuilder sb = new StringBuilder();

        if (inPath != null && !inPath.isEmpty()) {
            sb.append(inPath);
            if (!inPath.endsWith("/") && !inPath.endsWith("\\")) {
                sb.append("/");
            }
        }

        sb.append(fileName);

        if (!fileName.contains(".")) {
            //文件名不包含.时
            sb.append(".");
            sb.append(excelType);
        }

        return new File(sb.toString());
    }
}
