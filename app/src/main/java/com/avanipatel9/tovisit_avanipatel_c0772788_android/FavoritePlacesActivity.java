package com.avanipatel9.tovisit_avanipatel_c0772788_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

public class FavoritePlacesActivity extends AppCompatActivity {

    DatabaseHelper mDatabase;

    List<FavouritePlace> places;
    SwipeMenuListView swipeMenuListView;
    FavoritePlaceAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_places);

        swipeMenuListView = (SwipeMenuListView) findViewById(R.id.favPlaceList);
        mDatabase = new DatabaseHelper(this);
        places = new ArrayList<>();
        loadPlaces();


        adapter = new FavoritePlaceAdapter(this,R.layout.fav_place_cell_layout,places,mDatabase);
        swipeMenuListView.setAdapter(adapter);
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(300);
                // set item title
                openItem.setTitle("Update Location");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(300);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

// set creator
        swipeMenuListView.setMenuCreator(creator);

        swipeMenuListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Intent intent = new Intent(FavoritePlacesActivity.this, DurationAndDistanceActivity.class);
                        intent.putExtra("id",places.get(position).id);
                        intent.putExtra("lat",places.get(position).longitude);
                        intent.putExtra("longi",places.get(position).latitude);
                        intent.putExtra("edit",true);
                        startActivity(intent);
                        places.clear();

                        swipeMenuListView.setAdapter(adapter);
                        loadPlaces();

                        break;
                    case 1:
                        // delete

                        Toast.makeText(FavoritePlacesActivity.this, "case 1"+places.get(position).id, Toast.LENGTH_SHORT).show();

                        if(mDatabase.deletePlace(places.get(position).id)) {
                            Toast.makeText(FavoritePlacesActivity.this, "case 1", Toast.LENGTH_SHORT).show();
                            // swipeMenuListView.deferNotifyDataSetChanged();
                            places.remove(places.get(position));
                            swipeMenuListView.setAdapter(adapter);
                            loadPlaces();

                        }else {
                            Toast.makeText(FavoritePlacesActivity.this, ""+mDatabase.deletePlace(position), Toast.LENGTH_SHORT).show();
                        }
                        loadPlaces();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }

        });swipeMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(FavoritePlacesActivity.this, "case 1"+places.get(position).id, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(FavoritePlacesActivity.this, DurationAndDistanceActivity.class);
                intent.putExtra("id",places.get(position).id);
                intent.putExtra("lat",places.get(position).longitude);
                intent.putExtra("longi",places.get(position).latitude);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        places.clear();
        swipeMenuListView.setAdapter(adapter);
        loadPlaces();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadPlaces();
    }

    private void loadPlaces() {

        places.clear();
        Cursor cursor = mDatabase.getAllPlace();
        if (cursor.moveToFirst()) {

            do {
                places.add(new FavouritePlace(cursor.getInt(0), cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getDouble(4),
                        cursor.getDouble(5),cursor.getInt(6)));


            } while (cursor.moveToNext());

            cursor.close();

        }

    }
}