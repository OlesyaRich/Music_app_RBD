package org.olesya.musicaldevapp.utils;

import lombok.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    public static void checkIsFileNull(@NonNull File file) throws CommonException {
        if (!file.exists() || !file.isFile() || !file.canRead())
            throw new CommonException("Файл пуст");
    }

    public static File getFileFromBinaryStream(InputStream stream, String fileExtension) throws IOException {
        File tempFile = null;
        if (stream != null) {
            String tempFileNamePrefix = "";
            while (tempFileNamePrefix.length() <= 2)
                tempFileNamePrefix = "B" + (int) ((Math.random() * 98) + 1);
            tempFile = File.createTempFile(tempFileNamePrefix, fileExtension);
            tempFile.deleteOnExit();
            FileOutputStream out = new FileOutputStream(tempFile);
            stream.transferTo(out);
            stream.close();
            out.close();
        }
        return tempFile;
    }
}
