package org.pixelexperience.faceunlock;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.face.FaceManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;

import org.pixelexperience.faceunlock.util.NotificationUtils;
import org.pixelexperience.faceunlock.util.Util;

public class SetupFaceIntroActivity extends FaceBaseActivity {
    public static final String EXTRA_ENROLL_SUCCESS = "extra_enroll_success";
    private static final String TAG = SetupFaceIntroActivity.class.getSimpleName();
    private LottieAnimationView mVideo;

    public static boolean hasAllPermissionsGranted(int[] iArr) {
        for (int i : iArr) {
            if (i == -1) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        NotificationUtils.cancelNotification(this);
        FaceManager faceManager = getSystemService(FaceManager.class);
        if (faceManager.hasEnrolledTemplates()) {
            Log.e(TAG, "Has enrolled face! return!");
            showFinishActivity();
        }
        preInit();
    }

    private void showFinishActivity() {
        setResult(1, null);
        Intent intent = new Intent();
        intent.setClass(this, FaceUpgradeFinish.class);
        startActivityForResult(intent, 1);
    }

    private void preInit() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != 0) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
            return;
        }
        init();
    }

    private void init() {
        setContentView(R.layout.face_intro);
        boolean z = !Util.isFaceUnlockDisabledByDPM(this);
        if (Util.DEBUG) {
            Log.i(TAG, "isDevicePolicyEnabled: " + z);
        }
        if (!z) {
            showPolicydDialog();
        }
        mVideo = (LottieAnimationView) findViewById(R.id.video);
        mVideo.setAnimation(Util.isNightModeEnabled(this) ? R.raw.video_value_prop_dark : R.raw.video_value_prop);
        Button buttonNext = findViewById(R.id.info_next);
        buttonNext.setOnClickListener(view -> {
            Intent intent = new Intent(SetupFaceIntroActivity.this, FaceEnrollActivity.class);
            SetupFaceIntroActivity.this.parseIntent(intent);
            SetupFaceIntroActivity.this.startActivityForResult(intent, 1);
        });
        Button buttonCancel = findViewById(R.id.info_skip);
        buttonCancel.setOnClickListener(view -> {
            SetupFaceIntroActivity.this.setResult(0);
            SetupFaceIntroActivity.this.finish();
        });
    }

    @Override
    protected void onActivityResult(int i, int i2, Intent intent) {
        if (Util.DEBUG) {
            Log.i(TAG, "requestCode: " + i + ", resultCode: " + i2);
        }
        if (i == 1) {
            if (i2 == -1 || Util.isFaceUnlockEnrolled(this)) {
                setResult(1, null);
                finish();
            }
        } else if (i == 2) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != 0) {
                if (Util.DEBUG) {
                    Log.i(TAG, "REQUEST_CAMERA finish");
                }
                finish();
                return;
            }
            if (Util.DEBUG) {
                Log.i(TAG, "REQUEST_CAMERA init");
            }
            init();
        } else if (i == 3) {
            if (i2 == 0) {
                setResult(0, null);
                finish();
            }
        }
    }

    private void showPermissionRequiredDialog() {
        boolean required = true;
        try {
            if (Settings.Secure.getInt(getContentResolver(), "user_setup_complete") != 1) {
                required = false;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        AlertDialog.Builder message = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setTitle(R.string.perm_required_alert_title)
                .setMessage(R.string.perm_required_alert_msg)
                .setOnCancelListener(dialogInterface -> SetupFaceIntroActivity.this.finish());
        if (required) {
            message.setPositiveButton(R.string.perm_required_alert_button_app_info, (dialogInterface, i) -> Util.jumpToAppInfo(SetupFaceIntroActivity.this, 2));
        }
        message.show();
    }

    private void showPolicydDialog() {
        new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setMessage(R.string.policy_disabled_alert_msg)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> SetupFaceIntroActivity.this.finish())
                .setOnCancelListener(dialogInterface -> SetupFaceIntroActivity.this.finish()).show();
    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 0 && hasAllPermissionsGranted(iArr)) {
            init();
        } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            showPermissionRequiredDialog();
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideo.playAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideo.cancelAnimation();
    }
}
