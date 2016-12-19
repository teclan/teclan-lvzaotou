package teclan.lvzaotou.core.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

@Singleton
public class FileUtils {
    private final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 压缩目录 {@code root} 至文件 {@code target}.zip
     * 
     * @param root
     *            压缩目录
     * @param target
     *            压缩后的文件名
     * @throws FileNotFoundException
     */
    public void zip(String root, String target) throws FileNotFoundException {
        File zipDir;
        ZipOutputStream zipOut;
        zipDir = new File(root);
        String zipFileName = target + ".zip";
        try {
            zipOut = new ZipOutputStream(new BufferedOutputStream(
                    new FileOutputStream(zipFileName)));
            zipOut.setEncoding("GBK");
            zipDir(root, zipDir, zipOut);
            zipOut.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * 压缩目录 {@code root} 至文件 {@code root}.zip
     * 
     * @param root
     *            压缩目录
     * @throws FileNotFoundException
     */
    public void zip(String root) throws FileNotFoundException {
        File zipDir;
        ZipOutputStream zipOut;
        zipDir = new File(root);
        String zipFileName = zipDir.getName() + ".zip";
        try {
            zipOut = new ZipOutputStream(new BufferedOutputStream(
                    new FileOutputStream(zipFileName)));
            zipOut.setEncoding("GBK");
            zipDir(root, zipDir, zipOut);
            zipOut.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void zipDir(String root, File dir, ZipOutputStream zipOut)
            throws IOException {
        FileInputStream fileIn;
        File[] files;
        byte[] buf = new byte[1024];
        int readedBytes;
        files = dir.listFiles();

        if (files.length == 0) {// 如果目录为空,则单独创建之.
            zipOut.putNextEntry(new ZipEntry(dir.toString() + "/"));
            zipOut.closeEntry();
        } else {
            for (File fileName : files) {
                if (fileName.isDirectory()) {
                    zipDir(root, fileName, zipOut);
                } else {
                    fileIn = new FileInputStream(fileName);
                    ZipEntry zipEntry = new ZipEntry(new String(fileName.toURI()
                            .getPath().replace(root, "").getBytes(), "GBK"));
                    zipEntry.setUnixMode(755);
                    zipOut.putNextEntry(zipEntry);

                    while ((readedBytes = fileIn.read(buf)) > 0) {
                        zipOut.write(buf, 0, readedBytes);
                    }
                    zipOut.closeEntry();
                }
            }
        }
    }
}
