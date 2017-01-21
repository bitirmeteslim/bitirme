package com.android.park_sistemi.Modules;

import java.util.List;

/**
 * Created by User on 20.11.2016.
 */

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}

