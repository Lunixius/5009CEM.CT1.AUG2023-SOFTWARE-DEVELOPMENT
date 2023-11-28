package com.example.assignment;

import static android.widget.Toast.makeText;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class add extends AppCompatActivity {

    private static final String TAG = "AddActivity";

    private EditText etAddName, etAddPrice;
    private ImageView imgGallery;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getData() != null) {
                        imageUri = data.getData();
                        imgGallery.setImageURI(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        etAddName = findViewById(R.id.etAddName);
        etAddPrice = findViewById(R.id.etAddPrice);
        imgGallery = findViewById(R.id.imgGallery);
        Button selectImageBtn = findViewById(R.id.select_image);
        Button uploadEntityBtn = findViewById(R.id.uploadentity);
        Button backBtn = findViewById(R.id.back);

        databaseReference = FirebaseDatabase.getInstance().getReference("Menu");
        storageReference = FirebaseStorage.getInstance().getReference();

        selectImageBtn.setOnClickListener(v -> activityResultLauncher.launch(createImagePickerIntent()));

        uploadEntityBtn.setOnClickListener(v -> uploadEntity());

        // Handle back button click
        backBtn.setOnClickListener(v -> finish());
    }

    private Intent createImagePickerIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return intent;
    }
    private void uploadEntity() {
        if (imageUri != null) {
            // Generate a random UUID for the image filename
            String imageName = UUID.randomUUID().toString();
            StorageReference imageReference = storageReference.child(imageName);

            // Upload image to Firebase Storage
            imageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded image
                        imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String name = etAddName.getText().toString().trim();
                            double price = Double.parseDouble(etAddPrice.getText().toString().trim());

                            // Create a new entity with name, price, and image URL
                            MenuItem menuItem = new MenuItem(name, uri.toString(), price);

                            // Push the entity to the Firebase Realtime Database
                            databaseReference.child(imageName).setValue(menuItem);

                            makeText(add.this, "Entity uploaded successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to upload image", e);
                        makeText(add.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        } else {
            makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

}
