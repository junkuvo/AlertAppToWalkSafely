package apps.junkuvo.lunchtimelog;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Okubo on 2/1/2016 001.
 */
public class MapsUtility {
    private Context mContext;
    private GoogleMap mMap;

    public MapsUtility(Context context) {
        mContext = context;
    }

    public void setMap(GoogleMap map) {
        if (mMap == null) {
            mMap = map;
            if (mMap != null) {
//                createMarkersOnMap();
//                setCameraPositionOnMap();
            }
        }
    }

    public void setCurrentLocationMarker(LatLng location){

        MarkerOptions options = new MarkerOptions();
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        options.icon(icon);
        options.title("test");
        options.position(location);
        mMap.addMarker(options);
    }
}
