package com.joker17.excel2mysql.utils;

import org.springframework.util.DigestUtils;

import java.io.*;

public class FileUtils {

    private FileUtils() {
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

    /**
     * 获取MD5值
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String md5DigestAsHex(File file) throws IOException {
        return md5DigestAsHex(new BufferedInputStream(new FileInputStream(file)));
    }

    /**
     * 获取MD5值
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String md5DigestAsHex(InputStream inputStream) throws IOException {
        try {
            return DigestUtils.md5DigestAsHex(inputStream);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }

}
