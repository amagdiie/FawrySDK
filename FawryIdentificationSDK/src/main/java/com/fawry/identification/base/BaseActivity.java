package com.fawry.identification.base;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fawry.identification.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        ButterKnife.bind(this);
        onViewReady(savedInstanceState, getIntent());
    }

    @CallSuper
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        //To be used by child activities
    }

    @Override
    protected void onDestroy() {
        ButterKnife.bind(this);
        super.onDestroy();
    }

    protected void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("MultiBackStack", "Failed to add fragment to back stack", e);
        }
    }


    protected void showBackArrow() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }
    }

    public void showProgressDialog(String title, @NonNull String message, int icon) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            if (title != null)
                mProgressDialog.setTitle(title);
            mProgressDialog.setIcon(icon);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }


    public void hideDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    protected void showAlertDialog(String tit, String msg, int s) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(tit);
        dialogBuilder.setIcon(s);
        dialogBuilder.setMessage(msg);
        dialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.cancel());

        dialogBuilder.setCancelable(false);
        dialogBuilder.show();
    }

    protected void showToast(String mToastMsg) {
        Toast.makeText(this, mToastMsg, Toast.LENGTH_LONG).show();
    }

    protected void goToNext(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
//        overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
    }

    protected abstract int getContentView();

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void startNewActivity(String packageName) {
        Intent intent = this.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    public void makeCall(String phone) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }

    protected void showCustomToast(String message) {
//        LayoutInflater inflater = getLayoutInflater();
//        View layout = inflater.inflate(R.layout.toast,
//                (ViewGroup) findViewById(R.id.toast_layout_root));
//
//        TextView text = (TextView) layout.findViewById(R.id.textToast);
//        text.setText(message);
//
//        text.setTypeface(ResourcesCompat.getFont(this, R.font.cairo_semi_bold));
//
//
//        Toast toast = new Toast(getApplicationContext());
//        toast.setGravity(Gravity.BOTTOM, 0, 150);
//        toast.setDuration(Toast.LENGTH_SHORT);
//        toast.setView(layout);
//        toast.show();
    }

    protected  boolean checkAndRequestPermissions() {
        int internet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int storageWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int storageRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int loc = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int loc2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int call = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        int cam = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int networkState = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        int record = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (cam != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (networkState != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        if (record != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }

        if (internet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (storageWrite != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (storageRead != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (call != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
}