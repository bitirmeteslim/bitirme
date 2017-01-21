package com.android.park_sistemi.Modules;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import javax.xml.datatype.Duration;

/**
 * Created by User on 20.11.2016.
 */
public class Route {
    public Distance distance;
    public com.android.park_sistemi.Modules.Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
}
