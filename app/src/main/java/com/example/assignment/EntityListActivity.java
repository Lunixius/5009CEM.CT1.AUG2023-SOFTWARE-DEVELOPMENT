package com.example.assignment;// EntityListActivity.java

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EntityListActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private List<MenuItem> menuItemList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_list);

        databaseReference = FirebaseDatabase.getInstance().getReference("Menu");
        menuItemList = new ArrayList<>();

        ArrayAdapter<MenuItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItemList);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        fetchMenuItems();
    }

    private void fetchMenuItems() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuItemList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MenuItem menuItem = snapshot.getValue(MenuItem.class);
                    if (menuItem != null) {
                        menuItemList.add(menuItem);
                    }
                }

                ArrayAdapter<MenuItem> adapter = (ArrayAdapter<MenuItem>) ((ListView) findViewById(R.id.listView)).getAdapter();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }


}
