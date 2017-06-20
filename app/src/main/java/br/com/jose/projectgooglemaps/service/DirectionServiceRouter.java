package br.com.jose.projectgooglemaps.service;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import br.com.jose.projectgooglemaps.domain.Distance;
import br.com.jose.projectgooglemaps.domain.Duration;
import br.com.jose.projectgooglemaps.domain.Router;
import br.com.jose.projectgooglemaps.listener.RoutingListener;

/**
 * Created by jose on 6/19/17.
 */

public class DirectionServiceRouter {

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBXMezvjM1jAbGsEbGpuwkWb0sUVd2JXzU";
    private RoutingListener listener;
    private String origin;
    private String destination;
    private String mode;


    public DirectionServiceRouter(RoutingListener listener, String origin, String destination, String mode) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        this.mode = mode;
    }


    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionStart();
        new DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");
        String urlMode = URLEncoder.encode(mode, "utf-8");

        return DIRECTION_URL_API + "origin=" + urlOrigin
                + "&destination=" + urlDestination + "&mode=" + urlMode;
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];

            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                Log.i("doInBackground", buffer.toString());
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);
            try {
                parseJSON(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSON(String data) throws JSONException {
        if (data == null) {
            return;
        }
        List<Router> routes = new ArrayList<>();
        //List<String> infoPoints = new ArrayList<>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Router route = new Router();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            route.setDistance(new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value")));
            route.setDuration(new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value")));
            route.setEndAddress(jsonLeg.getString("end_address"));
            route.setStartAddress(jsonLeg.getString("start_address"));
            //infoPoints.add(jsonLeg.getString("html_instructions"));
            route.setStartLocation(new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng")));
            route.setEndLocation(new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng")));
            route.setPoits(decodePolyLine(overview_polylineJson.getString("points")));

            Log.i("DirectionService", i + "");

            routes.add(route);
        }

        listener.onDirectionSuccess(routes);
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }
        return decoded;
    }
}
