package com.fawry.identification;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.fawry.identification.tflite.OpjectsModels;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.fawry.identification.env.ImageUtils;
import com.fawry.identification.env.Logger;
import com.fawry.identification.tflite.Classifier.Device;
import com.fawry.identification.tflite.Classifier.Model;
import com.fawry.identification.tflite.Classifier.Recognition;

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.fawry.identification.tflite.OpjectsModels.FrontConfig;
import static com.fawry.identification.tflite.OpjectsModels.FrontIDImage;

public abstract class CameraActivity extends AppCompatActivity
    implements OnImageAvailableListener,
        Camera.PreviewCallback,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener {


  // Storage Permissions
  private static final int REQUEST_EXTERNAL_STORAGE = 1;
  private static final String[] PERMISSIONS_STORAGE = {
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
  };


  private TextView mTextTess;


  public int i;
  public ArrayList<Uri> mArrayUri;
  private Bitmap bitmap;

//  private static final String TAG = "OCVSample::AddFace";
//  private Mat mRgba;
//  private Mat mRgbaF;
//  private Mat mRgbaT;


  private static final Logger LOGGER = new Logger();

  private static final int PERMISSIONS_REQUEST = 1;

  private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
  protected int previewWidth = 0;
  protected int previewHeight = 0;
  private Handler handler;
  private HandlerThread handlerThread;
  private boolean useCamera2API;
  private boolean isProcessingFrame = false;
  private byte[][] yuvBytes = new byte[3][];
  private int[] rgbBytes = null;
  private int yRowStride;
  private Runnable postInferenceCallback;
  private Runnable imageConverter;

//  private LinearLayout bottomSheetLayout;
//  private LinearLayout gestureLayout;
//  private BottomSheetBehavior sheetBehavior;
//  protected TextView recognitionTextView,
//      recognition1TextView,
//      recognitionValueTextView,
//      recognition1ValueTextView;
//  protected TextView frameValueTextView,
//      cropValueTextView,
//      cameraResolutionTextView,
//      rotationTextView,
//      inferenceTimeTextView;
//  protected ImageView bottomSheetArrowImageView;
//  private ImageView plusImageView, minusImageView;
//  private Spinner modelSpinner;
//  private Spinner deviceSpinner;
//  private TextView threadsTextView;
//   private Model model = Model.QUANTIZED;

  private Model model = Model.FLOAT;
  private Device device = Device.CPU;
  private int numThreads = -1;
//   MediaPlayer mp,mp1,mp2;
  private FrameLayout mFrame;

  private LottieAnimationView lottieAnimationView;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    LOGGER.d("onCreate " + this);
    super.onCreate(null);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    setContentView(R.layout.activity_camera);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    if (hasPermission()) {
      setFragment();
    } else {
      requestPermission();
    }

    verifyStoragePermissions(CameraActivity.this);

    mTextTess = findViewById(R.id.testTextTess);
    lottieAnimationView = findViewById(R.id.animationView);
    mFrame = findViewById(R.id.container);
//    threadsTextView = findViewById(R.id.threads);
//    plusImageView = findViewById(R.id.plus);
//    minusImageView = findViewById(R.id.minus);
//    modelSpinner = findViewById(R.id.model_spinner);
//    deviceSpinner = findViewById(R.id.device_spinner);
//    bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
//    gestureLayout = findViewById(R.id.gesture_layout);
//    sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
//    bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);

//    ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
//    vto.addOnGlobalLayoutListener(
//        new ViewTreeObserver.OnGlobalLayoutListener() {
//          @Override
//          public void onGlobalLayout() {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//              gestureLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//            } else {
//              gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//            }
//            //                int width = bottomSheetLayout.getMeasuredWidth();
//            int height = gestureLayout.getMeasuredHeight();
//
//            sheetBehavior.setPeekHeight(height);
//          }
//        });
//    sheetBehavior.setHideable(false);
//
//    sheetBehavior.setBottomSheetCallback(
//        new BottomSheetBehavior.BottomSheetCallback() {
//          @Override
//          public void onStateChanged(@NonNull View bottomSheet, int newState) {
//            switch (newState) {
//              case BottomSheetBehavior.STATE_HIDDEN:
//                break;
//              case BottomSheetBehavior.STATE_EXPANDED:
//                {
//                  bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
//                }
//                break;
//              case BottomSheetBehavior.STATE_COLLAPSED:
//                {
//                  bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
//                }
//                break;
//              case BottomSheetBehavior.STATE_DRAGGING:
//                break;
//              case BottomSheetBehavior.STATE_SETTLING:
//                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
//                break;
//            }
//          }
//
//          @Override
//          public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
//        });
//
//    recognitionTextView = findViewById(R.id.detected_item);
//    recognitionValueTextView = findViewById(R.id.detected_item_value);
//    recognition1TextView = findViewById(R.id.detected_item1);
//    recognition1ValueTextView = findViewById(R.id.detected_item1_value);
//
//    frameValueTextView = findViewById(R.id.frame_info);
//    cropValueTextView = findViewById(R.id.crop_info);
//    cameraResolutionTextView = findViewById(R.id.view_info);
//    rotationTextView = findViewById(R.id.rotation_info);
//    inferenceTimeTextView = findViewById(R.id.inference_info);
//
//    modelSpinner.setOnItemSelectedListener(this);
//    deviceSpinner.setOnItemSelectedListener(this);
//
//    plusImageView.setOnClickListener(this);
//    minusImageView.setOnClickListener(this);
//
//    model = Model.valueOf(modelSpinner.getSelectedItem().toString().toUpperCase());
//    device = Device.valueOf(deviceSpinner.getSelectedItem().toString());
//    numThreads = Integer.parseInt(threadsTextView.getText().toString().trim());
  }

  protected int[] getRgbBytes() {
    imageConverter.run();
    return rgbBytes;
  }

  protected int getLuminanceStride() {
    return yRowStride;
  }

  protected byte[] getLuminance() {
    return yuvBytes[0];
  }

  /** Callback for android.hardware.Camera API */
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void onPreviewFrame(final byte[] bytes, final Camera camera) {
    if (isProcessingFrame) {
      LOGGER.w("Dropping frame!");
      return;
    }

    try {
      // Initialize the storage bitmaps once when the resolution is known.
      if (rgbBytes == null) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        previewHeight = previewSize.height;
        previewWidth = previewSize.width;
        rgbBytes = new int[previewWidth * previewHeight];
        onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
      }
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      return;
    }

    isProcessingFrame = true;
    yuvBytes[0] = bytes;
    yRowStride = previewWidth;

    imageConverter =
        new Runnable() {
          @Override
          public void run() {
            ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
          }
        };

    postInferenceCallback =
        new Runnable() {
          @Override
          public void run() {
            camera.addCallbackBuffer(bytes);
            isProcessingFrame = false;
          }
        };
    processImage();
  }

  /** Callback for Camera2 API */
  @Override
  public void onImageAvailable(final ImageReader reader) {
    // We need wait until we have some size from onPreviewSizeChosen
    if (previewWidth == 0 || previewHeight == 0) {
      return;
    }
    if (rgbBytes == null) {
      rgbBytes = new int[previewWidth * previewHeight];
    }
    try {
      final Image image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (isProcessingFrame) {
        image.close();
        return;
      }
      isProcessingFrame = true;
      Trace.beginSection("imageAvailable");
      final Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);
      yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();

      imageConverter =
              () -> ImageUtils.convertYUV420ToARGB8888(
                  yuvBytes[0],
                  yuvBytes[1],
                  yuvBytes[2],
                  previewWidth,
                  previewHeight,
                  yRowStride,
                  uvRowStride,
                  uvPixelStride,
                  rgbBytes);

      postInferenceCallback =
              () -> {
                image.close();
                isProcessingFrame = false;
              };

      processImage();

      ByteBuffer buffer = image.getPlanes()[0].getBuffer();
      byte[] bytes = new byte[buffer.capacity()];
      buffer.get(bytes);
      bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      Trace.endSection();
      return;
    }
    Trace.endSection();
  }

  @Override
  public synchronized void onStart() {
    LOGGER.d("onStart " + this);
    super.onStart();
  }

  @Override
  public synchronized void onResume() {
    LOGGER.d("onResume " + this);
    super.onResume();

    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());

  }

  @Override
  public synchronized void onPause() {
    LOGGER.d("onPause " + this);

    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }

    super.onPause();
  }

  @Override
  public synchronized void onStop() {
    LOGGER.d("onStop " + this);
    super.onStop();
  }

  @Override
  public synchronized void onDestroy() {
    LOGGER.d("onDestroy " + this);
    super.onDestroy();
  }

  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) {
      handler.post(r);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void onRequestPermissionsResult(
      final int requestCode, final String[] permissions, final int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSIONS_REQUEST) {
      if (allPermissionsGranted(grantResults)) {
        setFragment();
      } else {
        requestPermission();
      }
    }
  }

  private static boolean allPermissionsGranted(final int[] grantResults) {
    for (int result : grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    } else {
      return true;
    }
  }

  private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
        Toast.makeText(
                CameraActivity.this,
                "Camera permission is required for this demo",
                Toast.LENGTH_LONG)
            .show();
      }
      requestPermissions(new String[] {PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
    }
  }

  // Returns true if the device supports the required hardware level, or better.
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private boolean isHardwareLevelSupported(
      CameraCharacteristics characteristics, int requiredLevel) {
    int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
      return requiredLevel == deviceLevel;
    }
    // deviceLevel is not LEGACY, can use numerical sort
    return requiredLevel <= deviceLevel;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private String chooseCamera() {
    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    try {
      for (final String cameraId : manager.getCameraIdList()) {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // We don't use a front facing camera in this sample.
        final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        final StreamConfigurationMap map =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        if (map == null) {
          continue;
        }

        // Fallback to camera1 API for internal cameras that don't have full support.
        // This should help with legacy situations where using the camera2 API causes
        // distorted or otherwise broken previews.
        useCamera2API =
            (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                || isHardwareLevelSupported(
                    characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
        LOGGER.i("Camera API lv2?: %s", useCamera2API);
        return cameraId;
      }
    } catch (CameraAccessException e) {
      LOGGER.e(e, "Not allowed to access camera");
    }

    return null;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  protected void setFragment() {
    String cameraId = chooseCamera();

    Fragment fragment;
    if (useCamera2API) {
      CameraConnectionFragment camera2Fragment =
          CameraConnectionFragment.newInstance(
                  (size, rotation) -> {
                    previewHeight = size.getHeight();
                    previewWidth = size.getWidth();
                    CameraActivity.this.onPreviewSizeChosen(size, rotation);
                  },
              this,
              getLayoutId(),
              getDesiredPreviewFrameSize());

      camera2Fragment.setCamera(cameraId);
      fragment = camera2Fragment;
    } else {
      fragment =
          new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
    }

    getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
  }

  protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  protected void readyForNextImage() {
    if (postInferenceCallback != null) {
      postInferenceCallback.run();
    }
  }

  protected int getScreenOrientation() {
    switch (getWindowManager().getDefaultDisplay().getRotation()) {
      case Surface.ROTATION_270:
        return 270;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_90:
        return 90;
      default:
        return 0;
    }
  }
  boolean frontID = false;
  boolean backID = false;
  @UiThread
  protected void showResultsInBottomSheet(List<Recognition> results) {

    String recognitionTextView = "";
    if (results != null && results.size() >= 2) {
      Recognition recognition = results.get(0);
      if (recognition != null) {
        if (recognition.getTitle() != null) recognitionTextView = (recognition.getTitle());
        if (recognition.getConfidence() != null){
          Log.e("Ahmed", String.valueOf(recognition.getConfidence()));
//          recognitionValueTextView.setText(String.format("%.2f", (100 * recognition.getConfidence())) + "%");
          float confi = 100 * recognition.getConfidence();
          if (!frontID && recognitionTextView.equalsIgnoreCase("FrontIDCard") && confi > 98 ) {
            frontID =true;
            backID = false;
            Log.e("Ahmed", "FrontIDCard: " +String.valueOf(confi));

            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.setSpeed(1);
            lottieAnimationView.playAnimation();
            lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
              @Override
              public void onAnimationStart(Animator animation) {

              }

              @Override
              public void onAnimationEnd(Animator animation) {

                mTextTess.setText(recognition.getTitle());

                Intent intent = new Intent(CameraActivity.this, ReadTextFromImageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
              }

              @Override
              public void onAnimationCancel(Animator animation) {

              }

              @Override
              public void onAnimationRepeat(Animator animation) {

              }
            });

          }
        }
      }
    }
  }

  private Bitmap takeScreenshot(View v1) {
    Date now = new Date();
    android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

    try {
      // image naming and path  to include sd card  appending name you choose for file
      String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

      // create bitmap screen capture
      v1.setDrawingCacheEnabled(true);
      v1.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
      v1.buildDrawingCache();
      Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
      v1.setDrawingCacheEnabled(false);
      v1.destroyDrawingCache();

      File imageFile = new File(mPath);

      FileOutputStream outputStream = new FileOutputStream(imageFile);
      int quality = 100;
      bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
      outputStream.flush();
      outputStream.close();
      return bitmap;
    } catch (Throwable e) {
      // Several error may come out with file handling or DOM
      e.printStackTrace();
      return null;
    }
  }

//  protected void showFrameInfo(String frameInfo) {
//    frameValueTextView.setText(frameInfo);
//  }
//
//  protected void showCropInfo(String cropInfo) {
//    cropValueTextView.setText(cropInfo);
//  }
//
//  protected void showCameraResolution(String cameraInfo) {
//    cameraResolutionTextView.setText(cameraInfo);
//  }
//
//  protected void showRotationInfo(String rotation) {
//    rotationTextView.setText(rotation);
//  }
//
//  protected void showInference(String inferenceTime) {
//    inferenceTimeTextView.setText(inferenceTime);
//  }

  protected Model getModel() {
    return model;
  }

  private void setModel(Model model) {
    if (this.model != model) {
      LOGGER.d("Updating  model: " + model);
      this.model = model;
      onInferenceConfigurationChanged();
    }
  }

  protected Device getDevice() {
    return device;
  }

  private void setDevice(Device device) {
    if (this.device != device) {
      LOGGER.d("Updating  device: " + device);
      this.device = device;
      final boolean threadsEnabled = device == Device.CPU;
//      plusImageView.setEnabled(threadsEnabled);
//      minusImageView.setEnabled(threadsEnabled);
//      threadsTextView.setText(threadsEnabled ? String.valueOf(numThreads) : "N/A");
      onInferenceConfigurationChanged();
    }
  }

  protected int getNumThreads() {
    return numThreads;
  }

  private void setNumThreads(int numThreads) {
    if (this.numThreads != numThreads) {
      LOGGER.d("Updating  numThreads: " + numThreads);
      this.numThreads = numThreads;
      onInferenceConfigurationChanged();
    }
  }

  protected abstract void processImage();

  protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

  protected abstract int getLayoutId();

  protected abstract Size getDesiredPreviewFrameSize();

  protected abstract void onInferenceConfigurationChanged();

  @Override
  public void onClick(View v) {
//    if (v.getId() == R.id.plus) {
//      String threads = threadsTextView.getText().toString().trim();
//      int numThreads = Integer.parseInt(threads);
//      if (numThreads >= 9) return;
//      setNumThreads(++numThreads);
//      threadsTextView.setText(String.valueOf(numThreads));
//    } else if (v.getId() == R.id.minus) {
//      String threads = threadsTextView.getText().toString().trim();
//      int numThreads = Integer.parseInt(threads);
//      if (numThreads == 1) {
//        return;
//      }
//      setNumThreads(--numThreads);
//      threadsTextView.setText(String.valueOf(numThreads));
//    }
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
//    if (parent == modelSpinner) {
//      setModel(Model.valueOf(parent.getItemAtPosition(pos).toString().toUpperCase()));
//    } else if (parent == deviceSpinner) {
//      setDevice(Device.valueOf(parent.getItemAtPosition(pos).toString()));
//    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing.
  }

  public static void verifyStoragePermissions(Activity activity) {
    // Check if we have write permission
    int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    if (permission != PackageManager.PERMISSION_GRANTED) {
      // We don't have permission so prompt the user
      ActivityCompat.requestPermissions(
              activity,
              PERMISSIONS_STORAGE,
              REQUEST_EXTERNAL_STORAGE
      );
    }
  }


}
