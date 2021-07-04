package com.fawry.identification.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fawry.identification.R;
import com.fawry.identification.base.BaseFragment;

import butterknife.ButterKnife;

public class BackIDScan extends BaseFragment {

    private View mView;

    @Override
    public View provideYourFragmentView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_camera, parent, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this, mView);


    }
}
