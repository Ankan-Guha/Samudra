package com.example.myapplication.Profile;


import android.net.Uri;

import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private Uri imageUri;

    public void setImageUri(Uri uri) {
        this.imageUri = uri;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}
