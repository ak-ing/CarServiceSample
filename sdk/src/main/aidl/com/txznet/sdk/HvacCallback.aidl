package com.txznet.sdk;

interface HvacCallback {

    oneway void onTemperatureChanged(in double temperature);

}