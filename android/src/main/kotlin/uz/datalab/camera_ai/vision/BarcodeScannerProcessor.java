package uz.datalab.camera_ai.vision;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;


public class BarcodeScannerProcessor {

    private BarcodeScanner barcodeScanner;

    private OnBarcodeProcessResult onBarcodeProcessResult;

    private boolean mBarcodeDetectWork;

    public BarcodeScannerProcessor() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();

        this.barcodeScanner = BarcodeScanning.getClient(options);

        this.mBarcodeDetectWork = false;
    }

    public void setOnBarcodeProcessResult(OnBarcodeProcessResult onBarcodeProcessResult) {
        this.onBarcodeProcessResult = onBarcodeProcessResult;
    }

    public void run(InputImage image) {
        if (mBarcodeDetectWork) {
            return;
        }

        try {
            mBarcodeDetectWork = true;
            this.barcodeScanner.process(image)
                    .addOnSuccessListener(BARCODE_SUCCESS)
                    .addOnFailureListener(BARCODE_FAIL)
                    .addOnCanceledListener(BARCODE_CANCEL);
        } catch (Exception e) {
            mBarcodeDetectWork = false;
        }

    }


    private final OnSuccessListener<List<Barcode>> BARCODE_SUCCESS = new OnSuccessListener<List<Barcode>>() {
        @Override
        public void onSuccess(final List<Barcode> results) {
            try {
                if (onBarcodeProcessResult != null) {
                    onBarcodeProcessResult.result(results);
                }
            } finally {
                mBarcodeDetectWork = false;
            }
        }
    };

    private final OnCanceledListener BARCODE_CANCEL = new OnCanceledListener() {
        @Override
        public void onCanceled() {
            mBarcodeDetectWork = false;
        }
    };

    private final OnFailureListener BARCODE_FAIL = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            mBarcodeDetectWork = false;
        }
    };

    static interface OnBarcodeProcessResult {
        void result(List<Barcode> barcodes);
    }
}