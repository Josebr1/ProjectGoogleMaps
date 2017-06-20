package br.com.jose.projectgooglemaps.domain;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by jose on 6/19/17.
 *
 */

public class Router {

    private Distance distance;
    private Duration duration;
    private String endAddress;
    private LatLng endLocation;
    private String startAddress;
    private LatLng startLocation;
    private List<LatLng> poits;
    //private List<String> inforPoint;

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public List<LatLng> getPoits() {
        return poits;
    }

    public void setPoits(List<LatLng> poits) {
        this.poits = poits;
    }

    //public List<String> getInforPoint() {
     //   return inforPoint;
    //}

    //public void setInforPoint(List<String> setInforPoint) {
    //    this.inforPoint = setInforPoint;
    //}
}
