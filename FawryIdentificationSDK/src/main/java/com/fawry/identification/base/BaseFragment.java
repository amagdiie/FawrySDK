package com.fawry.identification.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;

import static android.content.Context.INPUT_METHOD_SERVICE;

public abstract class BaseFragment extends Fragment {


    private ProgressDialog mProgressDialog;

    private View mView;

    protected static String FACEBOOK_URL = "https://www.facebook.com/YourPageName";
    protected static String FACEBOOK_PAGE_ID = "YourPageName";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanseState) {
        mView = provideYourFragmentView(inflater,parent,savedInstanseState);
        ButterKnife.bind(this, mView);
        return mView;
    }

    public abstract View provideYourFragmentView(LayoutInflater inflater,ViewGroup parent, Bundle savedInstanceState);


    protected void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            if (getActivity().getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("MultiBackStack", "Failed to add fragment to back stack", e);
        }
    }

    public void showProgressDialog(String title, @NonNull String message, int icon) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
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
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(tit);
        dialogBuilder.setIcon(s);
        dialogBuilder.setMessage(msg);
        dialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.cancel());

        dialogBuilder.setCancelable(false);
        dialogBuilder.show();
    }

    protected void showToast(Context ctx, String mToastMsg) {
        Toast.makeText(ctx, mToastMsg, Toast.LENGTH_LONG).show();
    }

    protected void goToNext(Context ctx, Class c){
        Intent intent = new Intent(ctx, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
//        getActivity().overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void makeCall(String phone){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    protected void showCustomToast(Context ctx, String message, ViewGroup viewGroup) {
//        LayoutInflater inflater = getLayoutInflater();
//        View layout = inflater.inflate(R.layout.toast, viewGroup.findViewById(R.id.toast_layout_root));
//
//        TextView text = (TextView) layout.findViewById(R.id.textToast);
//        text.setText(message);
//
//        text.setTypeface(ResourcesCompat.getFont(ctx, R.font.cairo_semi_bold));
//
//
//        Toast toast = new Toast(ctx);
//        toast.setGravity(Gravity.BOTTOM, 0, 150);
//        toast.setDuration(Toast.LENGTH_SHORT);
//        toast.setView(layout);
//        toast.show();
    }

    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }
}
