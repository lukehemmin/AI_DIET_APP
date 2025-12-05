package com.lukehemmin.ai_diet_app.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static File getFileFromUri(Context context, Uri uri) throws IOException {
        ContentResolver cR = context.getContentResolver();
        String mime = cR.getType(uri);
        String ext = ".jpg"; // default
        if (mime != null) {
            String detectedExt = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
            if (detectedExt != null) {
                ext = "." + detectedExt;
            }
        }

        File destinationFilename = new File(context.getCacheDir(), "query_image_" + System.currentTimeMillis() + ext);
        try (InputStream ins = cR.openInputStream(uri)) {
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

    public static File compressImage(Context context, File imageFile) {
        try {
            // 1. Get image dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

            // 2. Calculate inSampleSize
            int maxWidth = 1920;
            int maxHeight = 1920;
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);

            // 3. Decode bitmap with inSampleSize
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

            if (bitmap == null) {
                return imageFile; // Failed to decode, return original
            }

            // 4. Handle Rotation (Exif)
            try {
                ExifInterface ei = new ExifInterface(imageFile.getAbsolutePath());
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                Bitmap rotatedBitmap = rotateBitmap(bitmap, orientation);
                if (rotatedBitmap != bitmap) {
                    bitmap.recycle();
                    bitmap = rotatedBitmap;
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Continue even if rotation fails
            }

            // 5. Compress to file (Standardize to JPEG for API)
            File compressedFile = new File(context.getCacheDir(), "compressed_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(compressedFile);
            
            // Compress with 80% quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            
            // Recycle bitmap to free memory
            bitmap.recycle();

            Log.d("FileUtils", "Compressed image size: " + compressedFile.length() / 1024 + "KB");
            return compressedFile;

        } catch (Exception e) {
            e.printStackTrace();
            return imageFile; // Return original if compression fails
        }
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
