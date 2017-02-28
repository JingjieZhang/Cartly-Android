package com.example.jingjie.maptest;


import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;




public class mapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    GoogleMap mMap;
    private GoogleApiClient mLocationClient;
    //instanciate in inConnected method
    private LocationListener mListener;
    private Marker markerS = null;
    private Marker markerE = null;
    private Polyline line;
    private Intent i;
    private String startPos;
    private String endPos;
    private History h;
    private String path;
    private GroundOverlay imageOverlay;
    private GroundOverlayOptions newarkMap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        if (initMap())
        {
            i = getIntent();
            startPos = i.getStringExtra("start");
            endPos = i.getStringExtra("end");

            addOverLay();
            try
            {
                createPathByPassInPosition();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            mLocationClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mLocationClient.connect();
            Button bt=(Button)findViewById(R.id.button);
            bt.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    try
                    {
                        geoLocate(v);
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            });

        } else
        {
            Toast.makeText(this, "Map not connected!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.addGroundOverlay(newarkMap);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                imageOverlay.remove();
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                imageOverlay.remove();
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                imageOverlay.remove();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean initMap()
    {
        if (mMap == null)
        {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
        {
            @Override
            public void onMapLongClick(LatLng latLng)
            {

                Geocoder gc = new Geocoder(mapActivity.this);
                List<android.location.Address> list = null;

                try
                {
                    list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }

                android.location.Address add = list.get(0);
                //mapActivity.this.addMarker( latLng.latitude, latLng.longitude);
                addMarker(latLng.latitude, latLng.longitude);

            }
        });

        //prepare to write history
        path=this.getFilesDir().getPath().toString();
        try
        {
            h=new History(path);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return (mMap != null);
    }

    private String getUrl(LatLng origin, LatLng dest)
    {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=walking";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + "mode=walking";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e)
        {
            Log.d("Exception", e.toString());
        } finally
        {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... url)
        {

            // For storing data from web service
            String data = "";

            try
            {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e)
            {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>>
    {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData)
        {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try
            {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e)
            {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result)
        {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++)
            {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++)
                {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null)
            {
                line = mMap.addPolyline(lineOptions);
            } else
            {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }


    @Override
    public void onConnected(Bundle bundle)
    {

    }

    public void navigation(MenuItem item)
    {
        mListener = new LocationListener()
        {
            @Override
            //this method will be called every time when location changes
            public void onLocationChanged(Location location)
            {

                if(markerS!=null&&markerE==null)
                {
                    markerE=markerS;
                    markerS.remove();
                    markerS=null;
                }

                if(markerE!=null)
                {
                    //"this" right now will be the listener object,and I'd need to walk up the tree to the MainActivity object.
                    Toast.makeText(mapActivity.this, "Location changed: " + location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    //than change the map
                    //gotoLocation(location.getLatitude(),location.getLongitude(),15);
                    //updata path
                    Double destinationLat=markerE.getPosition().latitude;
                    Double destinationLng=markerE.getPosition().longitude;
                    removeEverything();
                    Double startLat=location.getLatitude();
                    Double startLng=location.getLongitude();
                    //markerS = null;
                    //markerE = null;
                    /*
                    addMarker(startLat,startLng);
                    addMarker(destinationLat,destinationLng);
                    */

                    addMarker(destinationLat,destinationLng);
                    addMarker(startLat,startLng);

                }

            }
        };
        //set up request
        LocationRequest request = LocationRequest.create();
        //change properity of request object
        //set priority
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //set interval,the amount of time between request,5 seconds here
        //set the interval in which you want to get locations
        request.setInterval(1 * 5 * 1000);
        //if a location is available sooner you can get it (i.e. another app is using the location services).
        request.setFastestInterval(1000);

        //register the request object
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, request, mListener);
    }



    //go to current location
    public void showCurrentLocation(MenuItem item)
    {
        if(mListener!=null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, mListener);
        }

        Location currentLocation = LocationServices.FusedLocationApi
                .getLastLocation(mLocationClient);
        if (currentLocation == null)
        {
            Toast.makeText(this, "Couldn't connect! Please activate GPS.", Toast.LENGTH_SHORT).show();
        } else
        {
            LatLng latLng = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            addMarker(currentLocation.getLatitude(),currentLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
                    latLng, 16
            );
            mMap.animateCamera(update);
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }


    protected void onPause()
    {
        super.onPause();
        //when user exit the app,will turn off the request.
        if(mListener!=null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, mListener);

        }
    }


    private void addMarker(double lat, double lng)
    {

        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(lat, lng));


        if (markerE == null)
        {
            markerE = mMap.addMarker(options);
            if (markerS != null)
            {
                createPath();
            }
        }
        else if (markerS == null)
        {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            markerS = mMap.addMarker(options);
            createPath();

        }


        else if(markerE != null && markerS != null)
        {
            removeEverything();
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            markerS = mMap.addMarker(options);
        }
    }

    private void createPath()
    {
        LatLng origin = markerS.getPosition();
        LatLng dest = markerE.getPosition();

        // Getting URL to the Google Directions API
        String url = getUrl(origin, dest);
        Log.d("onMapLongClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
    }

    private void removeEverything()
    {
        if (markerS != null)
        {
            markerS.remove();
            markerS = null;
        }
        if (markerE != null)
        {
            markerE.remove();
            markerE = null;
        }
        if (line != null)
        {
            line.remove();
            line = null;
        }
    }

    private void createPathByPassInPosition() throws IOException
    {
        removeEverything();
        Geocoder gc = new Geocoder(this);
        LocationData loc = new LocationData();
        if (!startPos.equals(""))
        {
            Building building = loc.getLocation(startPos);
            double lat = building.getLat();
            double lng = building.getLng();


            MarkerOptions options = new MarkerOptions()
                    .position(new LatLng(lat, lng));
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            markerS = mMap.addMarker(options);

        }
        if (!endPos.equals(""))
        {
            Building building = loc.getLocation(endPos);
            double lat = building.getLat();
            double lng = building.getLng();


            MarkerOptions options = new MarkerOptions()
                    .position(new LatLng(lat, lng));

            markerE = mMap.addMarker(options);

        }

        if(markerE != null)
        {
            gotoLocation(markerE.getPosition().latitude, markerE.getPosition().longitude, (float)16.5);
        }
        else if(markerS != null)
        {
            gotoLocation(markerS.getPosition().latitude, markerS.getPosition().longitude, (float)16.5);
        }

        if (markerE != null && markerS != null)
        {
            createPath();
        }
    }

    private void hideSoftKeyboard(View v)
    {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void geoLocate(View v) throws IOException
    {
        double lat=0;
        double lng=0;
        boolean isValid=false;
        hideSoftKeyboard(v);

        TextView tv = (TextView) findViewById(R.id.editText);
        String searchString = (tv.getText().toString()).trim();

        //write to history
        try
        {
            if(!h.repeated(searchString))
            {
                h.addHistory(searchString);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }


        LocationData lcd=new LocationData();
        Building building=lcd.getLocation(searchString);
        if(building!=null)
        {
            lat = building.getLat();
            lng = building.getLng();
            Toast.makeText(this, lat + "," + lng, Toast.LENGTH_SHORT).show();
            gotoLocation(lat, lng, (float)16.5);
            addMarker(lat, lng);
        }

        else
        {

            Geocoder gc = new Geocoder(this);
            List<android.location.Address> list = gc.getFromLocationName(searchString, 1, 34.234927, -118.531747, 34.250429, -118.523326);
            if (list.size() > 0)
            {
                //here we just ask for 1 result.so the first Address in the list is the result
                android.location.Address add = list.get(0);
                // getLocality() will return the name of the location in the Address object
                String locality = add.getLocality();
                String feature = add.getFeatureName();

                //ready to change the location that map show
                lat = add.getLatitude();
                lng = add.getLongitude();

                Toast.makeText(this, lat + "," + lng, Toast.LENGTH_SHORT).show();
                gotoLocation(lat, lng, (float)16.5);
                addMarker(lat, lng);
            } else
            {
                Toast.makeText(this, "Can not find the destination", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }
    private void addOverLay()
    {

        LatLngBounds newarkBounds = new LatLngBounds(
                new LatLng(34.235551, -118.533768),       // South west corner
                new LatLng(34.257127, -118.523552));      // North east corner
                newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.map3))
                .positionFromBounds(newarkBounds);

// Add an overlay to the map, retaining a handle to the GroundOverlay object.
        imageOverlay = mMap.addGroundOverlay(newarkMap);
    }
}