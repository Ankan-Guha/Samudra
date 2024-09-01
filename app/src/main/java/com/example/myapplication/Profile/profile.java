package com.example.myapplication.Profile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myapplication.R;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;



import android.graphics.Bitmap;
import android.net.Uri;

import android.provider.MediaStore;

import android.widget.Button;
import android.widget.ImageView;


import java.io.IOException;

import android.graphics.BitmapFactory;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class profile extends AppCompatActivity {
    ImageView profilePhoto;
    EditText name,age,weight,height,sex;
    Button saveImg,selectImg;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Bitmap selectedImageBitmap;
    private ProfileDatabase dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        name=findViewById(R.id.name);
        age=findViewById(R.id.age);
        weight=findViewById(R.id.weight);
        height=findViewById(R.id.height);
        sex=findViewById(R.id.sex);
        profilePhoto=findViewById(R.id.imageView);

        saveImg=findViewById(R.id.buttonSaveImage);
        selectImg=findViewById(R.id.buttonSelectImage);

        dbHelper = new ProfileDatabase(this);

        selectImg.setOnClickListener(v -> openImagePicker());
        saveImg.setOnClickListener(v -> saveUserProfile());
        loadUserProfile();

    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImageBitmap = BitmapFactory.decodeStream(imageStream);
                profilePhoto.setImageBitmap(selectedImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveUserProfile() {
        String name1 = name.getText().toString().trim();
        float weight1;
        int age1;
        float height1;
        String sex1 = sex.getText().toString().trim();

        // Validate inputs
        if (name1.isEmpty() || sex1.isEmpty() || selectedImageBitmap == null) {
            Toast.makeText(this, "Please fill all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            weight1 = Float.parseFloat(weight.getText().toString().trim());
            age1 = Integer.parseInt(age.getText().toString().trim());
            height1 = Float.parseFloat(height.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for weight, age, and height.", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();

        // Create UserProfile object
        UserProfile userProfile = new UserProfile(name1, weight1, age1, height1, imageBytes, sex1);

        // Use insertUserProfile method
        long newRowId = dbHelper.insertUserProfile(userProfile);

        if (newRowId != -1) {
            Toast.makeText(this, "User profile saved.", Toast.LENGTH_SHORT).show();
            clearInputFields(); // Optional: Clear input fields after saving
            loadUserProfile(); // Reload user profile to show saved data
        } else {
            Log.e("ProfileActivity", "Insert failed, newRowId is -1");
            Toast.makeText(this, "Error saving user profile.", Toast.LENGTH_SHORT).show();
        }
    }


    private void clearInputFields() {
        name.setText("");
        weight.setText("");
        age.setText("");
        height.setText("");
        sex.setText("");
        profilePhoto.setImageResource(R.drawable.baseline_account_circle_24); // Set a default image if needed
    }
    private void loadUserProfile() {
        UserProfile userProfile = dbHelper.getUserProfile();
        if (userProfile != null) {
            name.setText(userProfile.getName());
            weight.setText(String.valueOf(userProfile.getWeight()));
            age.setText(String.valueOf(userProfile.getAge()));
            height.setText(String.valueOf(userProfile.getHeight()));
            sex.setText(userProfile.getSex());
            if (userProfile.getImage() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(userProfile.getImage(), 0, userProfile.getImage().length);
                profilePhoto.setImageBitmap(bitmap);
            }
        } else {
            Toast.makeText(this, "No user profile found.", Toast.LENGTH_SHORT).show();
        }
    }


}
