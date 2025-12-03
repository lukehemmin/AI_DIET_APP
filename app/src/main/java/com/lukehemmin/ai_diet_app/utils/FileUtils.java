package com.lukehemmin.ai_diet_app.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static File getFileFromUri(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getCacheDir(), "query_image_" + System.currentTimeMillis() + ".jpg");
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return destinationFilename;
    }

    public static File getFileFromBitmap(Context context, Bitmap bitmap) throws IOException {
        File destinationFilename = new File(context.getCacheDir(), "capture_image_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(destinationFilename)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        return destinationFilename;
    }

    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
