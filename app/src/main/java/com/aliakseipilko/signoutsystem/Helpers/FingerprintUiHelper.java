/*
 * com.aliakseipilko.signoutsystem.Helpers.FingerprintUiHelper was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 23/12/16 13:12
 */

package com.aliakseipilko.signoutsystem.Helpers;

import android.widget.ImageView;
import android.widget.TextView;

import com.aliakseipilko.signoutsystem.R;

public class FingerprintUiHelper {

    private final ImageView mIcon;
    private final TextView mErrorTextView;
    private final Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mErrorTextView.setTextColor(
                    mErrorTextView.getResources().getColor(R.color.hint_color));
            mErrorTextView.setText(
                    mErrorTextView.getResources().getString(R.string.fingerprint_hint));
            mIcon.setImageResource(R.drawable.ic_fp_40px);
        }
    };

    public FingerprintUiHelper(ImageView icon, TextView errorTextView) {

        mIcon = icon;
        mErrorTextView = errorTextView;
        mIcon.setImageResource(R.drawable.ic_fp_40px);
    }

    public void showSuccess() {
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mIcon.setMinimumHeight(80);
        mIcon.setMinimumWidth(80);
        mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.success_color));
        mErrorTextView.setText(
                mErrorTextView.getResources().getString(R.string.fingerprint_success));

    }

    public void showError() {
        mIcon.setMinimumHeight(80);
        mIcon.setMinimumWidth(80);
        mIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mErrorTextView.setText((mIcon.getResources().getString(R.string.fingerprint_not_recognized)));
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.warning_color));
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.postDelayed(mResetErrorTextRunnable, 1600);
    }
}

