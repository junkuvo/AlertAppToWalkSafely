package com.example.okubo.onsenkensaku;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.okubo.onsenkensaku.R.id.btnDetail;

public class ActivityShowOnsenLocationOnMap extends FragmentActivity {

    private GoogleMap mMap;

    private double mLatitude;
    private double mLongitude;
    private String mMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        mLatitude = intent.getDoubleExtra("latitude",0);
        mLongitude = intent.getDoubleExtra("longitude",0);
        mMethod = intent.getStringExtra("method");

        createOnsenMap();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home:
                finish();
                return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createOnsenMap() {
        if (mMap == null) {
            infoWindow=getLayoutInflater().inflate(R.layout.custom_info_contents, null);
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                createMarkersOnMap();
                setCameraPositionOnMap();
            }
        }
    }

    private void createMarkersOnMap() {
        int i;
        OnsenData[] onsens = ActivitySearchOnsen.sOnsens;

        LatLng location;
        MarkerOptions options = new MarkerOptions();
        for(i = 0;i < onsens.length;i++) {
            location = new LatLng(onsens[i].getLatitude(), onsens[i].getLongitude());
            options.position(location);
            options.title(onsens[i].getName() + "\n" + onsens[i].getAddress() + "\n" + onsens[i].getTel());
            mMap.addMarker(options);

            mMap.setInfoWindowAdapter(new CustomInfoAdapter());
            mMap.setOnInfoWindowClickListener(mOnInfoWindowClickListener);
        }

        location = new LatLng(mLatitude, mLongitude);
        if(mMethod.equals("location")) {
            setMyCurrentLocationMarker(location);
        }
    }

    private void setCameraPositionOnMap(){
        LatLng location;
        location = new LatLng(mLatitude, mLongitude);
        CameraPosition cameraPos;
        if(mMethod.equals("location")) {
            cameraPos = new CameraPosition.Builder().target(location).zoom(10.0f).bearing(0).build();
        }else{
            cameraPos = new CameraPosition.Builder().target(location).zoom(8.0f).bearing(0).build();
        }
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
    }


    private View infoWindow;
    class CustomInfoAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoContents(Marker marker) {
            displayView(marker);
            return infoWindow;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }
    }

    public void displayView(Marker marker) {
        Button detail = (Button) infoWindow.findViewById(btnDetail);
        if(marker.getTitle().equals(getString(R.string.currentLocation))){
            detail.setVisibility(View.GONE);
        }else {
            detail.setVisibility(View.VISIBLE);
        }

        ((TextView)infoWindow.findViewById(R.id.title)).setText(marker.getTitle());
    }

    public void setMyCurrentLocationMarker(LatLng location){
        MarkerOptions options = new MarkerOptions();
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        options.icon(icon);
        options.title(getString(R.string.currentLocation));
        options.position(location);
        Marker marker = mMap.addMarker(options);
    }

    private GoogleMap.OnInfoWindowClickListener mOnInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            String id = marker.getId().replace("m","");
            if(Integer.parseInt(id) >= ActivitySearchOnsen.sOnsens.length){
                // my location - do nothing
            }else {
                Intent intent = new Intent(ActivityShowOnsenLocationOnMap.this, ActivityShowOnsenInfo.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        }
    };
}
