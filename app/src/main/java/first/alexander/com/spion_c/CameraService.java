package first.alexander.com.spion_c;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * CameraService.java - a service class to capture images trough a background process
 *
 * @author Alexander Julianto (no131614)
 */

public class CameraService extends Service implements
        SurfaceHolder.Callback {

    private Camera mCamera;
    private Camera.Parameters parameters;
    private Bitmap bmp;
    private String FLASH_MODE;
    private int QUALITY_MODE = 0;
    private int width = 0, height = 0;

    private boolean isFrontCamRequest = false;
    private Camera.Size pictureSize;

    private SurfaceHolder sHolder;
    private WindowManager windowManager;

    public Intent cameraIntent;

    FileOutputStream fo;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    SurfaceView sv;
    WindowManager.LayoutParams params;


    @Override
    public void onCreate() {
        super.onCreate();
    }


    /**
     * Find and open available camera for use
     *
     * @return openedCamera - An opened and available Camera Object
     */
    private Camera openFrontFacingCamera() {

        // Begin: Need to stop any current working camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        // End: Need to stop any current working camera

        int cameraCount = Camera.getNumberOfCameras();
        Camera openedCamera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        // Begin: Start finding any available camera and open it
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    openedCamera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("Camera",
                            "Camera failed to open: " + e.getLocalizedMessage());

                    // Toast currently disabled for current version 0.5
                     /*Toast.makeText(getApplicationContext(),
                      "Front Camera failed to open", Toast.LENGTH_LONG)
                      .show();*/
                }
            }
        }
        // End: Start finding any available camera and open it

        return openedCamera;
    }

    /**
     * Set the image resolution taken to best resolution
     */
    private void setBestPictureResolution() {
        // Get biggest picture size
        width = pref.getInt("Picture_Width", 0);
        height = pref.getInt("Picture_height", 0);

        if (width == 0 | height == 0) {
            pictureSize = getBiggestPictureSize(parameters);
            if (pictureSize != null)
                parameters
                        .setPictureSize(pictureSize.width, pictureSize.height);
            // Save width and height in shared preferences
            width = pictureSize.width;
            height = pictureSize.height;
            editor.putInt("Picture_Width", width);
            editor.putInt("Picture_height", height);
            editor.commit();

        } else {
            if (pictureSize != null)
                parameters.setPictureSize(width, height);
        }
    }


    /**
     * Get the biggest picture size that is supported by the camera
     *
     * @param parameters - Camera.Parameters object use to get camera
     *                   information of supported capabilities
     * @return result - The biggest supported picture size (Camera.Size)
     */
    private Camera.Size getBiggestPictureSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }
        return (result);
    }

    /**
     * Check if the device has a camera
     *
     * @param context - Context object to get information regarding
     *                the camera hardware in the device
     * @return boolean - Return true if device has a camera and
     * false otherwise
     */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    /**
     * Check if the device has a front camera
     *
     * @param context - Context object to get information regarding
     *                the camera hardware in the device
     * @return boolean - Return true if device has a front camera and
     * false otherwise
     */
    private boolean checkFrontCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY);
    }

    Handler handler = new Handler();

    /**
     * TakeImage - A class that inherited AsyncTask to allows this class to perform
     * image capture by executing takeImage method as a background operation
     * without having to manipulate threads and/or handlers.
     */
    private class TakeImage extends AsyncTask<Intent, Void, Void> {

        @Override
        protected Void doInBackground(Intent... params) {
            // Start takeImage method as a background process
            takeImage(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }


    /**
     * Synchronized method (prevents other threads from locking the same object at the same time)
     * use to take an image using the camera
     *
     * @param intent - Current Android intent
     */
    private synchronized void takeImage(Intent intent) {

        if (checkCameraHardware(getApplicationContext())) {
            // Begin: Get camera settings
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String flash_mode = extras.getString("FLASH");
                FLASH_MODE = flash_mode;

                boolean front_cam_req = extras.getBoolean("Front_Request");
                isFrontCamRequest = front_cam_req;

                int quality_mode = extras.getInt("Quality_Mode");
                QUALITY_MODE = quality_mode;
            }
            // End: Get camera settings


            if (isFrontCamRequest) {

                // Set the camera flash
                FLASH_MODE = "off";

                // Make sure the version is GINGERBREAD or above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {

                    mCamera = openFrontFacingCamera();
                    if (mCamera != null) {

                        try {
                            mCamera.setPreviewDisplay(sv.getHolder());
                        } catch (IOException e) {
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    System.out.println("API doesn't support front camera");
                                    // Toast currently disabled for current version 0.5
                                    /*Toast.makeText(getApplicationContext(),
                                            "API doesn't support front camera",
                                            Toast.LENGTH_LONG).show();*/
                                }
                            });

                            stopSelf();
                        }

                        Camera.Parameters parameters = mCamera.getParameters();
                        pictureSize = getBiggestPictureSize(parameters);

                        if (pictureSize != null) {
                            parameters.setPictureSize(pictureSize.width, pictureSize.height);
                        }

                        // Begin: Set camera parameters and take picture
                        mCamera.setParameters(parameters);
                        mCamera.startPreview();
                        mCamera.takePicture(null, null, mCall);
                        // End: Set camera parameters and take picture

                    } else {
                        mCamera = null;
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                System.out.println("Device doesn't have Front/Back Camera");
                                // Toast currently disabled for current version 0.5
                                /*Toast.makeText(
                                        getApplicationContext(),
                                        "Device doesn't have Front/Back Camera",
                                        Toast.LENGTH_LONG).show();*/
                            }
                        });

                        stopSelf();
                    }

                    /* sHolder = sv.getHolder(); // Tells Android that this
                     * surface will have its data constantly replaced if
                     * (Build.VERSION.SDK_INT < 11)
                     *
                     * sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
                     */
                } else { // If the version is not GINGERBREAD or above check for
                    // camera first (old device might not have camera at all)
                    if (checkFrontCamera(getApplicationContext())) {
                        mCamera = openFrontFacingCamera();

                        if (mCamera != null) {

                            try {
                                mCamera.setPreviewDisplay(sv.getHolder());
                            } catch (IOException e) {
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                "API doesn't support front camera",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                stopSelf();
                            }

                            Camera.Parameters parameters = mCamera.getParameters();
                            pictureSize = getBiggestPictureSize(parameters);

                            if (pictureSize != null) {
                                parameters.setPictureSize(pictureSize.width, pictureSize.height);
                            }

                            // Begin: Set camera parameters and take picture
                            mCamera.setParameters(parameters);
                            mCamera.startPreview();
                            mCamera.takePicture(null, null, mCall);
                            // End: Set camera parameters and take picture

                        } else {
                            mCamera = null;

                            System.out.println("API doesn't support Front/Back Camera");
                            // Toast currently disabled for current version 0.5
                            /* Toast.makeText(getApplicationContext(),
                             "API doesn't support front camera",
                             Toast.LENGTH_LONG).show();*/
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    System.out.println("Device doesn't have Front/Back Camera");
                                    // Toast currently disabled for current version 0.5
                                    /*Toast.makeText(
                                            getApplicationContext(),
                                            "Device doesn't have Front/Back Camera",
                                            Toast.LENGTH_LONG).show();*/
                                }
                            });
                            stopSelf();

                        }
                    }

                }

            } else {

                if (mCamera != null) {
                    // Begin: Disable camera if on
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = Camera.open();
                    // End: Disable camera if on
                } else {
                    mCamera = getCameraInstance();
                }

                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(sv.getHolder());
                        parameters = mCamera.getParameters();
                        if (FLASH_MODE == null || FLASH_MODE.isEmpty()) {
                            FLASH_MODE = "auto";
                        }
                        parameters.setFlashMode(FLASH_MODE);

                        // Set picture resolution
                        setBestPictureResolution();

                        // Log quality and image format
                        Log.d("Quality", parameters.getJpegQuality() + "");
                        Log.d("Format", parameters.getPictureFormat() + "");

                        // Begin: Set camera parameters and take picture
                        mCamera.setParameters(parameters);
                        mCamera.startPreview();
                        Log.d("Image Taking", "OnTake()");
                        mCamera.takePicture(null, null, mCall);
                        // End: Set camera parameters and take picture

                    } else {
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                System.out.println("Camera is unavailable");
                                // Toast currently disabled for current version 0.5
                                /*Toast.makeText(getApplicationContext(),
                                        "Camera is unavailable",
                                        Toast.LENGTH_LONG).show();*/
                            }
                        });

                    }

                } catch (IOException e) {
                    Log.e("TAG", "CameraHeadService()::takePicture", e);
                }

            }

        } else {

            handler.post(new Runnable() {

                @Override
                public void run() {
                    System.out.println("Device doesn't have a Camera");
                    // Toast currently disabled for current version 0.5
                    /*Toast.makeText(getApplicationContext(),
                            "Device doesn't have a Camera",
                            Toast.LENGTH_LONG).show();*/
                }
            });
            stopSelf();
        }

    }

    @SuppressWarnings("deprecation")

    /**
     * Method inherited from Service class. Called by the system every time a client explicitly
     * starts the service by calling startService(Intent)
     * @param intent - Current Android intent
     * @param flags  - Additional flag data about this start request
     * @param startId - A unique integer representing this specific request to start
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        cameraIntent = intent;
        Log.d("Image Taking", "StartCommand()");
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();

        // Begin: Create Window and Surface to start surfaceCreated()
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                // Changed LayoutParams from TYPE_PHONE to Type_Toast to work for API > 21
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.width = 1;
        params.height = 1;
        params.x = 0;
        params.y = 0;
        sv = new SurfaceView(getApplicationContext());

        windowManager.addView(sv, params);
        sHolder = sv.getHolder();
        sHolder.addCallback(this);
        // End: Create Window and Surface to start surfaceCreated()

         /*Tells Android that this surface will have its data constantly
         replaced if API < 11*/
        if (Build.VERSION.SDK_INT < 11) {
            sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        /* START_STICKY: Return value of 1. if this service's process is killed while it is started
        (after returning from onStartCommand()), then leave it in the started state
         but don't retain this delivered intent. Later the system will try
         to re-create the service.*/
        return START_STICKY;
    }

    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        /**
         * Method called when image data is available after a picture is taken.
         * @param data -  A byte array of the picture data
         * @param camera  - The Camera service object
         */
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Begin: Decode the data obtained by the camera into a Bitmap
            Log.d("Image Taking", "Done");
            if (bmp != null)
                bmp.recycle();
            System.gc();
            bmp = decodeBitmap(data);
            // End: Decode the data obtained by the camera into a Bitmap

            // Begin: Upload the image to the remote DB
            ImageProcess imgProcess = new ImageProcess();
            imgProcess.uploadImage(bmp);
            // End: Upload the image to the remote DB

            // Disable image storing feature for version 0.5
           /* // Begin: Create image and store in directory of device
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            if (bmp != null && QUALITY_MODE == 0)
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
            else if (bmp != null && QUALITY_MODE != 0)
                bmp.compress(Bitmap.CompressFormat.JPEG, QUALITY_MODE, bytes);

            File imagesFolder = new File(
                    Environment.getExternalStorageDirectory(), "MYGALLERY");// Folder name
            if (!imagesFolder.exists())
                imagesFolder.mkdirs(); // <----
            File image = new File(imagesFolder, System.currentTimeMillis()
                    + ".jpg");

            // Write the bytes in file
            try {
                fo = new FileOutputStream(image);
            } catch (FileNotFoundException e) {
                Log.e("TAG", "FileNotFoundException", e);
            }
            try {
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                Log.e("TAG", "fo.write::PictureTaken", e);
            }

            // Close the FileOutput
            try {
                fo.close();
                if (Build.VERSION.SDK_INT < 19)
                    sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_MOUNTED,
                            Uri.parse("file://"
                                    + Environment.getExternalStorageDirectory())));
                else {
                    MediaScannerConnection
                            .scanFile(
                                    getApplicationContext(),
                                    new String[]{image.toString()},
                                    null,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(
                                                String path, Uri uri) {
                                            Log.i("ExternalStorage", "Scanned "
                                                    + path + ":");
                                            Log.i("ExternalStorage", "-> uri="
                                                    + uri);
                                        }
                                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }


              *//*Toast.makeText(getApplicationContext(),
              "Picture Taken !", Toast.LENGTH_LONG).show();*//*

            if (bmp != null) {
                bmp.recycle();
                bmp = null;
                System.gc();
            }
            mCamera = null;
            handler.post(new Runnable() {

                @Override
                public void run() {

                    *//*Toast.makeText(getApplicationContext(),
                            "Picture Taken !", Toast.LENGTH_SHORT)
                            .show();*//*
                }
            });
            // End: Create image and store in directory of device*/

            stopSelf();
        }
    };

    /**
     * Empty onBind method needed due to SurfaceHolder.Callback inheritance
     */
    @Override
    public IBinder onBind(Intent intent) {
        // Nothing TO DO here
        return null;
    }

    /**
     * Get the camera instance by opening the camera on the device
     *
     * @return Camera - Opened camera instance
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            // Get a Camera instance
            c = Camera.open();
        } catch (Exception e) {
            System.out.println("Camera is not available (in use or does not exist)");
        }
        return c;
    }

    /**
     * Stop and release camera and remove window view upon activity destroy
     */
    @Override
    public void onDestroy() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (sv != null)
            windowManager.removeView(sv);
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("message", "Extra message");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        super.onDestroy();
    }

    /**
     * Empty surfaceChanged method needed due to SurfaceHolder.Callback inheritance
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // Nothing TO DO here
    }

    /**
     * Take image by executing TakeImage method upon surface created
     *
     * @param holder -  Current android surface holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (cameraIntent != null)
            new TakeImage().execute(cameraIntent);

    }

    /**
     * Stop and release camera upon surface destroyed
     *
     * @param holder -  Current android surface holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * Method to decode bitmap data
     *
     * @param data -  A byte array of the picture data
     * @return Bitmap  - The decoded bitmap data
     */
    public static Bitmap decodeBitmap(byte[] data) {

        Bitmap bitmap = null;
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        bfOptions.inDither = false; // Disable Dithering mode
        bfOptions.inPurgeable = true; // Tell to gc that whether it needs free
        // memory, the Bitmap can be cleared
        bfOptions.inInputShareable = true; // Which kind of reference will be
        /* Used to recover the Bitmap data after being clear, when it will
         be used in the future*/
        bfOptions.inTempStorage = new byte[32 * 1024];

        if (data != null)
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    bfOptions);

        return bitmap;
    }

}