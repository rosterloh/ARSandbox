package com.rosterloh.ar.sandbox.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.rosterloh.ar.sandbox.R;
import com.rosterloh.ar.sandbox.databinding.ActivityMainBinding;

import javax.inject.Inject;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import dagger.android.support.DaggerAppCompatActivity;
import timber.log.Timber;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends DaggerAppCompatActivity implements GLSurfaceView.Renderer {

    private static final int PERMISSIONS_REQUEST = 0;

    @Inject
    MainViewModelFactory viewModelFactory;

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private Session session;
    private Display display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);

        display = getSystemService(WindowManager.class).getDefaultDisplay();
        binding.surfaceview.setRenderer(this);
        try {
            session = new Session(this);
        } catch (UnavailableArcoreNotInstalledException e) {
            Timber.e("Please install ARCore");
        } catch (UnavailableApkTooOldException e) {
            Timber.e("Please update ARCore");
        } catch (UnavailableSdkTooOldException e) {
            Timber.e("Please update this app");
        } catch (Exception e) {
            Timber.e("This device does not support AR", e);
        }
        Config config = new Config(session);
        if (!session.isSupported(config)) {
            Timber.e("This device does not support AR");
        }
        session.configure(config);

        observeMessage();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(hasPermission()) {
            if (session != null) {
                session.resume();
            }
            binding.surfaceview.onResume();
        } else {
            requestPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.surfaceview.onPause();
        if (session != null) {
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!hasPermission()) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean hasPermission() {
        return (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        requestPermissions(new String[]{CAMERA}, PERMISSIONS_REQUEST);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        int textures[] = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        if (session != null) {
            session.setCameraTextureName(textures[0]);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        session.setDisplayGeometry(display.getRotation(), width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }

        try {
            // Obtain the current frame from ARSession
            Frame frame = session.update();
            Camera camera = frame.getCamera();

            // If not tracking, don't draw 3d objects.
            if (camera.getTrackingState() == Trackable.TrackingState.PAUSED) {
                return;
            }

            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            final float lightIntensity = frame.getLightEstimate().getPixelIntensity();

            // Visualize tracked points.
            PointCloud pointCloud = frame.acquirePointCloud();

            // Application is responsible for releasing the point cloud resources after
            // using it.
            pointCloud.release();
        } catch (Throwable t) {
            Timber.e("Exception on the OpenGL thread", t);
        }
    }

    private void observeMessage() {
        viewModel.getMessage().observe(this, msg -> Timber.d("Message:" + msg));
    }
}
