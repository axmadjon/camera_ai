package uz.datalab.vision;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import uz.datalab.face.FaceUtil;

public class VisionUtil {

    public static Bitmap resizeByMaximum(Bitmap bitmap, int size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > height) return resizeByWidth(bitmap, size);
        else return resizeByHeight(bitmap, size);
    }

    private static Bitmap resizeByWidth(Bitmap bitmap, int size) {
        if (size == 0) throw new RuntimeException("size == 0");
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width < size) return bitmap;

        double resizePercent = 100D - ((size * 100D) / width);
        int h = (int) Math.round(height - ((height / 100D) * resizePercent));
        return Bitmap.createScaledBitmap(bitmap, size, h, true);
    }

    private static Bitmap resizeByHeight(Bitmap bitmap, int size) {
        if (size == 0) throw new RuntimeException("size == 0");
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (height < size) return bitmap;

        double resizePercent = 100 - ((size * 100D) / height);
        int w = (int) Math.round(width - ((width / 100F) * resizePercent));
        return Bitmap.createScaledBitmap(bitmap, w, size, true);
    }

    public static void copyBigDataToSD(Context ctx, String name) throws IOException {
        File file = new File(FaceUtil.getModelFolder(ctx), name);
        if (file.exists()) {
            return;
        }
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream(file);
        myInput = ctx.getAssets().open(name);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    public static Rect rectWithMargin(Rect rect, float marginScale) {
        int widthMargin = (int) (rect.width() * marginScale);
        int heightMargin = (int) (rect.height() * marginScale);
        return new Rect(rect.left - widthMargin,
                rect.top - heightMargin,
                rect.right + widthMargin,
                rect.bottom + heightMargin);
    }

    public static Bitmap cropImage(Bitmap bitmap, Rect rect, float marginScale) {
        Rect nRect = rectWithMargin(rect, marginScale);
        return Bitmap.createBitmap(bitmap,
                Math.max(nRect.left, 0),
                Math.max(nRect.top, 0),
                Math.min(nRect.width(), bitmap.getWidth() - rect.left),
                Math.min(nRect.height(), bitmap.getHeight() - rect.top)
        );
    }

    public static byte[] toBytes(Bitmap bitmap, int quality) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }
}

