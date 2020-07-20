package com.avanipatel9.tovisit_avanipatel_c0772788_android;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {


    @BindView(R.id.ic_magnify)
    ImageView icMagnify;
    @BindView(R.id.input_search)
    EditText inputSearch;
    @BindView(R.id.rel_layout1)
    RelativeLayout relLayout1;
    @BindView(R.id.ic_gps)
    ImageView icGps;

    GoogleMap mMap;
    double lat, longi, dest_lat, dest_long;
    final int radious = 1000;
    List<Address> addresses;
    String address;
    boolean isOk;
    Geocoder geocoder;
    Location location;
    boolean isMrkerClick = false;
    Marker mMarker;

    DatabaseHelper mDatabase;
    //get user lopcation

    private FusedLocationProviderClient mFusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    private static final String TAG = "MapsActivity";
    private static final float DEFAULT_ZOOM = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        icGps.setVisibility(View.GONE);

        initMap();


        getUserLocation();
        mDatabase = new DatabaseHelper(this);

        if (!checkPermission()) {
            requestPermission();
        } else {
            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            //getUserLocation();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_type, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setHomeMarker();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }
    }

    private boolean checkPermission() {
        int status = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return status == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void initMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void init(){
        inputSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });
    }

    private void geoLocate(){

        String searchString = inputSearch.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        init();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {

                location = new Location("You Will Be Here Soon");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                dest_lat = latLng.latitude;
                dest_long = latLng.longitude;
                try {
                    setMarker(location);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    getAddress(location);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        mMap.setOnInfoWindowClickListener(this);


    }

    private void getUserLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
        setHomeMarker();

    }

    private void getAddress(Location location) throws IOException {
        System.out.println("In Get Address");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String addDate = simpleDateFormat.format(calendar.getTime());

        geocoder = new Geocoder(this, Locale.getDefault());

        System.out.println("in geocoder");

        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

        if (!addresses.isEmpty()) {
            address = addresses.get(0).getLocality() + " " + addresses.get(0).getAddressLine(0);
            System.out.println(addresses.get(0).getAddressLine(0));

            //Toast.makeText(MainActivity.this, "Employee is not addaed", Toast.LENGTH_SHORT).show();
        }
        //Toast.makeText(this, "Address:"+addresses.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
    }


    private void setMarker(Location location) throws IOException {
        System.out.println("In SetMarker");
        geocoder = new Geocoder(this, Locale.getDefault());
        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        LatLng userlatlong = new LatLng(location.getLatitude(), location.getLongitude());
        if (!addresses.isEmpty()) {
            address = addresses.get(0).getLocality() + " " + addresses.get(0).getAddressLine(0);
            System.out.println(addresses.get(0).getAddressLine(0));
        }
        MarkerOptions markerOptions = new MarkerOptions().position(userlatlong).title(addresses.get(0).getLocality());
        markerOptions.snippet(addresses.get(0).getAddressLine(0));
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMap.addMarker(markerOptions);

    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectordrawableResourse) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectordrawableResourse);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);


    }

    public void btnClick(View view) {
        Object[] dataTransfer = new Object[3];
        String url;
        GetNearByPlaces getNearByPlaceData = new GetNearByPlaces(this);
        switch (view.getId()) {

            case R.id.btn_restaurants:
                mMap.clear();
                url = getUrl(lat, longi, "restaurant");
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = "resturent";
                getNearByPlaceData.execute(dataTransfer);
                break;

            case R.id.btn_museum:
                mMap.clear();
                url = getUrl(lat, longi, "museum");
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = "museum";
                getNearByPlaceData.execute(dataTransfer);
                break;

            case R.id.btn_cafe:
                mMap.clear();
                url = getUrl(lat, longi, "cafe");
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = "cafe";
                getNearByPlaceData.execute(dataTransfer);
                break;

            case R.id.btn_library:
                mMap.clear();
                url = getUrl(lat, longi, "library");
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = "library";
                getNearByPlaceData.execute(dataTransfer);
                break;
            case R.id.btn_school:
                mMap.clear();
                url = getUrl(lat, longi, "school");
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = "school";
                getNearByPlaceData.execute(dataTransfer);
                break;
            case R.id.btn_hospital:
                mMap.clear();
                url = getUrl(lat, longi, "hospital");
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = "hospital";
                getNearByPlaceData.execute(dataTransfer);
                break;
            case R.id.btn_clear:
                mMap.clear();
                break;
            case R.id.btn_Fav_place:
                Intent intent = new Intent(this, FavoritePlacesActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_direction:
                Intent intent2 = new Intent(this, DurationAndDistanceActivity.class);
                intent2.putExtra("isMain",true);
                startActivity(intent2);
                break;
            case R.id.btn_getlocatiomn:
                getUserLocation();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                setHomeMarker();
                break;
        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + radious);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&key=" + getString(R.string.api_key_places));
        Log.d("", "getUrl: "+googlePlaceUrl);
        return googlePlaceUrl.toString();

    }





    private void setHomeMarker() {
        locationCallback = new LocationCallback() {
            // @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    lat = location.getLatitude();
                    longi = location.getLongitude();
                    LatLng userLoaction = new LatLng(location.getLatitude(), location.getLongitude());

                    CameraPosition cameraPosition = CameraPosition.builder().target(userLoaction).zoom(15).bearing(0).tilt(45).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    mMap.addMarker(new MarkerOptions().position(userLoaction).title("Your Destination").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.marker)));
                }
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.maptypeHYBRID:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    return true;
                }
            case R.id.maptypeNONE:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    return true;
                }
            case R.id.maptypeNORMAL:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    return true;
                }
            case R.id.maptypeSATELLITE:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    return true;
                }
            case R.id.maptypeTERRAIN:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    return true;
                }


                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadPlaces() {
        Cursor cursor = mDatabase.getAllPlace();
        if (cursor.moveToFirst()) {

            do {
                System.out.println(cursor.getString(1));


            } while (cursor.moveToNext());

            cursor.close();
        }

    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        System.out.println("MARKER: "+ marker.getTitle());
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Do you want to add this place as Favourite?");
        builder1.setCancelable(true);
        mMarker = marker;
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isOk = true;
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                        String addDate = simpleDateFormat.format(calendar.getTime());
                        if (isOk && mDatabase.addFavPlace(mMarker.getTitle(), addDate, mMarker.getSnippet(), mMarker.getPosition().latitude, mMarker.getPosition().longitude)) {
                            isOk = false;
                        }
                    }
                });
        builder1.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}