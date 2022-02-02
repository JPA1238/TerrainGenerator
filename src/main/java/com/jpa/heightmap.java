package com.jpa;

import java.util.HashMap;

public class heightmap {
    public HashMap<String, Integer> loc = new HashMap<String, Integer>();
    public HashMap<String, Integer> dim = new HashMap<String, Integer>();
    public int resolution = 50;
    public int count = 0;

    private static String baseUrl = "https://data.lpdaac.earthdatacloud.nasa.gov/lp-prod-protected/ASTGTM.003/ASTGTMV003_";
    private static String extension = "_dem.tif";

    public URLGenerator URLGen = new URLGenerator();

    heightmap(Integer lat, Integer lon, Integer width, Integer height) {
        this.loc.put("lat", lat); // N40 // S40
        this.loc.put("lon", lon); // W30 // E30
        this.dim.put("width", width); // 3 // 2
        this.dim.put("height", height); // 1 // 5

        URLGen.getPossibleLoc("data/elevationData/");

        getHeightvalues();
    }

    private void getHeightvalues() {
        Integer lat = loc.get("lat");
        Integer lon = loc.get("lon");

        String graph = "";

        for (int x = 0; x < dim.get("width"); x++) {
            for (int y = 0; y < dim.get("height"); y++) {
                String tempLat = locLength(lat - x, 3, true);
                String tempLon = locLength(lon + y, 4, false);

                if (URLGen.validLoc(tempLat + tempLon)) {
                    // System.out.println(baseUrl + tempLat + tempLon + extension);
                    graph += "X";
                    count++;
                } else {
                    // System.out.println("Invalid location : " + tempLat + " " + tempLon);
                    graph += "-";
                }
            }
            graph += "\n";
        }
        System.out.println(graph);
        System.out.println("number of images needed : " + count);
    }

    private String locLength(Integer loc, int len, boolean type) {
        String res = "";
        // type 1 => lat => N | S
        // type 0 => lon => E | W
        if (type) {
            if (loc < 0) {
                res = "S" + String.valueOf(abs(loc));
            } else {
                res = "N" + String.valueOf(abs(loc));
            }
        } else {
            if (loc < 0) {
                res = "W" + String.valueOf(abs(loc));
            } else {
                res = "E" + String.valueOf(abs(loc));
            }
        }

        while (res.length() < len) {
            res = res.substring(0, 1) + "0" + res.substring(1);
        }
        return res;
    }

    private Integer abs (Integer n) {
        if (n < 0){
            return -n;
        } else {
            return n;
        }
    }
}
