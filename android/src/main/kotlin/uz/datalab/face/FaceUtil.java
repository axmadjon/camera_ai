package uz.datalab.face;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.nio.ByteBuffer;

public class FaceUtil {

    public static File getMainFileDir(Context ctx) {
        File file = new File(ctx.getFilesDir(), "vision");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static String getModelFolder(Context context) {
        File file = new File(getMainFileDir(context), "models");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }


    public static boolean isModelFileExists(Context ctx, String name) {
        File file = new File(getModelFolder(ctx), name);
        return file.exists();
    }

    public static byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the

        return temp;
    }
}
