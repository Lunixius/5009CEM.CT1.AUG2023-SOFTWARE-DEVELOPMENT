package com.example.assignment;

import static android.content.ContentValues.TAG;

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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class update extends AppCompatActivity {

    private EditText etUpdateName, etUpdatePrice;
    private ImageView imgGallery, newImgGallery;
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
                        newImgGallery.setImageURI(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        etUpdateName = findViewById(R.id.etUpdateName);
        etUpdatePrice = findViewById(R.id.etUpdatePrice);
        imgGallery = findViewById(R.id.imgGallery);
        newImgGallery = findViewById(R.id.newImgGallery);
        findViewById(R.id.name);
        findViewById(R.id.price);

        Button selectEntityBtn = findViewById(R.id.select_entity);
        Button updateImageBtn = findViewById(R.id.update_image);
        Button updateEntityBtn = findViewById(R.id.update_entity);
        Button backBtn = findViewById(R.id.back);

        databaseReference = FirebaseDatabase.getInstance().getReference("Menu");
        storageReference = FirebaseStorage.getInstance().getReference();

        selectEntityBtn.setOnClickListener(v -> selectEntity());
        updateImageBtn.setOnClickListener(v -> activityResultLauncher.launch(createImagePickerIntent()));
        updateEntityBtn.setOnClickListener(v -> updateEntity());
        backBtn.setOnClickListener(v -> finish());
    }

    private Intent createImagePickerIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return intent;
    }

    private void selectEntity() {
        // Launch a new activity to display a list of entity names
        Intent intent = new Intent(update.this, EntityListActivity.class);
        activityResultLauncher.launch(intent);
    }

    // Handle the result from the entity selection activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Retrieve the selected entity name from the result
            String selectedEntityName = data.getStringExtra("selectedEntityName");

            // Query the database to get the corresponding entity details based on the selected name
            databaseReference.orderByChild("name").equalTo(selectedEntityName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Assuming direct field access in MenuItem class
                                MenuItem selectedEntity = dataSnapshot.getChildren().iterator().next().getValue(MenuItem.class);

                                // Update UI with the details of the chosen entity
                                if (selectedEntity != null) {
                                    imgGallery.setImageURI(Uri.parse(selectedEntity.imageUrl));
                                    etUpdateName.setText(selectedEntity.name);
                                    etUpdatePrice.setText(String.valueOf(selectedEntity.price));
                                }
                            } else {
                                Toast.makeText(update.this, "Failed to retrieve entity details", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors
                            Toast.makeText(update.this, "Database error", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void updateEntity() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select a new image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a random UUID for the image filename
        String imageName = UUID.randomUUID().toString();
        StorageReference imageReference = storageReference.child(imageName);

        // Upload image to Firebase Storage
        imageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded image
                    imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String name = etUpdateName.getText().toString().trim();
                        double price = Double.parseDouble(etUpdatePrice.getText().toString().trim());

                        // Assuming you have a variable called selectedEntityId representing the chosen entity's ID
                        String selectedEntityId = "..."; // Replace with actual ID

                        // Update the entity with the new name, price, and image URL
                        databaseReference.child(selectedEntityId).child("name").setValue(name);
                        databaseReference.child(selectedEntityId).child("price").setValue(price);
                        databaseReference.child(selectedEntityId).child("image").setValue(uri.toString());

                        Toast.makeText(update.this, "Entity updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(update.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    // Log the error for debugging purposes
                    Log.e(TAG, "Failed to upload image", e);
                });
    }
}
