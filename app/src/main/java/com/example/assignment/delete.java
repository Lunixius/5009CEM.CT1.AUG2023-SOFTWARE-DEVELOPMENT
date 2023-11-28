package com.example.assignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class delete extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::onActivityResult
    );

    private TextView nameTextView, priceTextView;
    private ImageView imgGallery;
    private DatabaseReference databaseReference;
    private String selectedEntityName; // Variable to store the selected entity name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        nameTextView = findViewById(R.id.name);
        priceTextView = findViewById(R.id.price);
        imgGallery = findViewById(R.id.imgGallery);

        Button selectEntityBtn = findViewById(R.id.select_entity);
        Button deleteEntityBtn = findViewById(R.id.delete_entity);
        Button backBtn = findViewById(R.id.back);

        databaseReference = FirebaseDatabase.getInstance().getReference("Menu");

        selectEntityBtn.setOnClickListener(v -> selectEntity());
        deleteEntityBtn.setOnClickListener(v -> deleteEntity());
        backBtn.setOnClickListener(v -> finish());
    }

    private void selectEntity() {
        // Launch a new activity to display a list of entity names
        Intent intent = new Intent(delete.this, EntityListActivity.class);
        activityResultLauncher.launch(intent);
    }

    private void updateUI(String selectedEntityName) {
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
                                nameTextView.setText(selectedEntity.name);
                                priceTextView.setText(String.valueOf(selectedEntity.price));
                            }
                        } else {
                            Toast.makeText(delete.this, "Failed to retrieve entity details", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                        Toast.makeText(delete.this, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteEntity() {
        if (selectedEntityName != null) {
            // Query the database to find the entity ID based on the selected name
            databaseReference.orderByChild("name").equalTo(selectedEntityName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Assuming direct field access in MenuItem class
                                String selectedEntityId = dataSnapshot.getChildren().iterator().next().getKey();

                                if (selectedEntityId != null) {
                                    // Delete the selected entity from Firebase Realtime Database
                                    databaseReference.child(selectedEntityId).removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                // Delete the corresponding image from Firebase Storage if needed
                                                // (Not implemented in this basic example)
                                                Toast.makeText(delete.this, "Entity deleted successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(delete.this, "Failed to delete entity", Toast.LENGTH_SHORT).show();
                                                // Log the error for debugging purposes
                                                // Log.e(TAG, "Failed to delete entity", e);
                                            });
                                } else {
                                    Toast.makeText(delete.this, "Entity ID not found in the database", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(delete.this, "Entity not found in the database", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors
                            Toast.makeText(delete.this, "Database error", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please select an entity to delete", Toast.LENGTH_SHORT).show();
        }
    }

    private void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            if (data.getStringExtra("selectedEntityName") != null) {
                selectedEntityName = data.getStringExtra("selectedEntityName");
                updateUI(selectedEntityName);
            }
        }
    }
}
