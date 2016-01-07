package com.meyersj.mobilesurveyor.app.util;


public class Args {

    public static final String MODE = "mode";
    public static final String RTE = "rte";
    public static final String DIR = "dir";
    public static final String UUID = "uuid";
    public static final String DATE = "date";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String USER_ID = "user_id";

    public static class Login {
        public static final String USER_NAME = "username";
        public static final String PASSWORD = "password";
        public static final String USER_ID = Args.USER_ID;
        public static final String MATCH = "match";
    }

    public static class StopLookup {
        public static final String RTE = Args.RTE;
        public static final String DIR = Args.DIR;
        public static final String LAT = Args.LAT;
        public static final String LON = Args.LON;
    }

    public static class Scans {
        public static final String UUID = Args.UUID;
        public static final String DATE = Args.DATE;
        public static final String USER_ID = Args.USER_ID;
        public static final String RTE = Args.RTE;
        public static final String DIR = Args.DIR;
        public static final String MODE = Args.MODE;
        public static final String LAT = Args.LAT;
        public static final String LON = Args.LON;
    }

    public static class Stops {
        public static final String DATE = Args.DATE;
        public static final String USER_ID = Args.USER_ID;
        public static final String RTE = Args.RTE;
        public static final String DIR = Args.DIR;
        public static final String ON_STOP = "on_stop";
        public static final String OFF_STOP = "off_stop";
        public static final String ON_REVERSED = "on_reversed";
        public static final String OFF_REVERSED = "off_reversed";
    }
}