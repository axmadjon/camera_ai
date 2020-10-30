package uz.datalab.camera_ai.vision;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;

import com.google.mlkit.vision.common.InputImage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class VisionUtil {

    public static Bitmap getImage2Bitmap(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

    private static Matrix getCameraToViewTransform(final InputImage image) {
        final Matrix transform = new Matrix();
        transform.postRotate(image.getRotationDegrees());
        return transform;
    }

    public static Bitmap getBitmapFromInputImage(final Image image, int rotation) {
        final Matrix transform = new Matrix();
        transform.postRotate(rotation);

        Image.Plane[] planes = image.getPlanes();
        ByteBuffer byteBuffer = planeToBuffer(planes, image.getWidth(), image.getHeight());
        final int width = image.getWidth();
        final int height = image.getHeight();
        final YuvImage yuvImage = new YuvImage(
                byteBuffer.array(),
                ImageFormat.NV21, width, height, null);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, outputStream);

        final byte[] jpegArray = outputStream.toByteArray();
        final Bitmap rawBitmap = BitmapFactory.decodeByteArray(
                jpegArray, 0, jpegArray.length);

        final int bw = rawBitmap.getWidth();
        final int bh = rawBitmap.getHeight();

        return Bitmap.createBitmap(rawBitmap, 0, 0, bw, bh, transform, false);
    }

    public static Bitmap getBitmapFromInputImage(final InputImage image) {
        Matrix transform = getCameraToViewTransform(image);

        if (image.getBitmapInternal() != null) {
            return image.getBitmapInternal();
        } else if (image.getByteBuffer() != null) {
            ByteBuffer byteBuffer = image.getByteBuffer();
            final int width = image.getWidth();
            final int height = image.getHeight();
            final YuvImage yuvImage = new YuvImage(
                    byteBuffer.array(),
                    ImageFormat.NV21, width, height, null);

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, outputStream);

            final byte[] jpegArray = outputStream.toByteArray();
            final Bitmap rawBitmap = BitmapFactory.decodeByteArray(
                    jpegArray, 0, jpegArray.length);

            final int bw = rawBitmap.getWidth();
            final int bh = rawBitmap.getHeight();

            return Bitmap.createBitmap(rawBitmap, 0, 0, bw, bh, transform, false);
        } else {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer byteBuffer = planeToBuffer(planes, image.getWidth(), image.getHeight());
            byte[] bytes = new byte[byteBuffer.capacity()];
            byteBuffer.get(bytes);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        }
    }


    @TargetApi(19)
    public static ByteBuffer planeToBuffer(Image.Plane[] var0, int var1, int var2) {
        int var3;
        byte[] var4 = new byte[(var3 = var1 * var2) + 2 * (var3 / 4)];
        int var9 = var1 * var2;
        ByteBuffer var10 = var0[1].getBuffer();
        ByteBuffer var11;
        int var12 = (var11 = var0[2].getBuffer()).position();
        int var13 = var10.limit();
        var11.position(var12 + 1);
        var10.limit(var13 - 1);
        boolean var14 = var11.remaining() == 2 * var9 / 4 - 2 && var11.compareTo(var10) == 0;
        var11.position(var12);
        var10.limit(var13);
        if (var14) {
            var0[0].getBuffer().get(var4, 0, var3);
            ByteBuffer var5 = var0[1].getBuffer();
            var0[2].getBuffer().get(var4, var3, 1);
            var5.get(var4, var3 + 1, 2 * var3 / 4 - 1);
        } else {
            zza(var0[0], var1, var2, var4, 0, 1);
            zza(var0[1], var1, var2, var4, var3 + 1, 2);
            zza(var0[2], var1, var2, var4, var3, 2);
        }

        return ByteBuffer.wrap(var4);
    }

    @TargetApi(19)
    private static void zza(Image.Plane var0, int var1, int var2, byte[] var3, int var4, int var5) {
        ByteBuffer var6;
        int var7 = (var6 = var0.getBuffer()).position();
        int var8 = (var6.remaining() + var0.getRowStride() - 1) / var0.getRowStride();
        int var9 = var2 / var8;
        int var10 = var1 / var9;
        int var11 = var4;
        int var12 = 0;

        for (int var13 = 0; var13 < var8; ++var13) {
            int var14 = var12;

            for (int var15 = 0; var15 < var10; ++var15) {
                var3[var11] = var6.get(var14);
                var11 += var5;
                var14 += var0.getPixelStride();
            }

            var12 += var0.getRowStride();
        }

        var6.position(var7);
    }

}
