package com.rosterloh.ar.sandbox.ui;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.MediaCas;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraException;
import com.rosterloh.ar.sandbox.R;
import com.rosterloh.ar.sandbox.databinding.ActivityMainBinding;

import javax.inject.Inject;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import dagger.android.AndroidInjection;
import timber.log.Timber;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends LifecycleActivity implements GLSurfaceView.Renderer {

    private static final int PERMISSIONS_REQUEST = 0;

    @Inject
    MainViewModelFactory viewModelFactory;

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private Config config;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);

        session = new Session(this);
        config = Config.createDefaultConfig();

        binding.surfaceview.setRenderer(this);
        observeMessage();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(hasPermission()) {
            session.resume(config);
            binding.surfaceview.onResume();
        } else {
            requestPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.surfaceview.onPause();
        session.pause();
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
        session.setCameraTextureName(textures[0]);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        session.setDisplayGeometry(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        try {
            // Obtain the current frame from ARSession
            Frame frame = session.update();

            // Get projection matrix.
            float[] projmtx = new float[16];
            session.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            frame.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            final float lightIntensity = frame.getLightEstimate().getPixelIntensity();
        } catch (CameraException e) {
            Timber.e("Camera exception onDrawFrame");
        }
    }

    private void observeMessage() {
        viewModel.getMessage().observe(this, msg -> Timber.d("Message:" + msg));
    }
}
