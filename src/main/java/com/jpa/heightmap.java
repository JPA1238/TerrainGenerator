package com.jpa;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class heightmap {
    private HashMap<String, Integer> loc = new HashMap<String, Integer>();
    private HashMap<String, Integer> dim = new HashMap<String, Integer>();
    private int resolution = 5; // TODO memory optimization
    private int count = 0;

    private String[][] map;

    private static String baseUrl = "ASTGTMV003_";
    private static String extension = "_dem.tif";

    public URLGenerator URLGen = new URLGenerator();

    heightmap(Integer lat, Integer lon, Integer width, Integer height) {
        this.loc.put("lat", lat);
        this.loc.put("lon", lon);
        this.dim.put("width", width);
        this.dim.put("height", height);

        URLGen.getPossibleLoc();

        map = new String[width][height];

        System.out.println("Getting URLs\n");
        getURLs();
    }

    private void getURLs() {
        Integer lat = loc.get("lat");
        Integer lon = loc.get("lon");

        String graph = "";

        for (int x = 0; x < dim.get("width"); x++) {
            for (int y = 0; y < dim.get("height"); y++) {
                String tempLat = locLength(lat - x, 3, true);
                String tempLon = locLength(lon + y, 4, false);

                if (URLGen.validLoc(tempLat + tempLon)) {
                    // System.out.println(baseUrl + tempLat + tempLon + extension);
                    map[x][y] = baseUrl + tempLat + tempLon + extension;
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

    public int[][] getHeightValues(int x, int y) {
        int[][] height = null;

        BufferedImage image = getTIF(map[x][y]);
        height = new int[(int) Math.floor(image.getWidth() / resolution)][(int) Math.floor(image.getHeight() / resolution)];
        for (int i = 0; i < image.getWidth() / resolution; i++) {
            for (int j = 0; j < image.getHeight() / resolution; j++) {
                Color c = new Color(image.getRGB(i * resolution, j * resolution));
                height[i][j] = c.getRed();
            }
        }
        System.out.println("Number of height values for image " + x + "-" + y + " : " + height.length * height[0].length);

        return height;
    }

    private BufferedImage getTIF(String path) {
        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(new FileReader("data/private/credentials.json"));
        } catch (Exception e) {
            System.out.println("ERROR : Failed to gather credentials");
            e.printStackTrace();
        }

        BufferedImage image = null;
        try {
            return image = ImageIO.read(new File(json.get("path") + path));
        } catch (IOException e) {
            System.out.println("ERROR : Couldn't read image " + path);
            e.printStackTrace();
        }
        return image;
    }

    private String locLength(Integer loc, int len, boolean type) {
        String res = "";
        // type 1 => lat => N | S
        // type 0 => lon => E | W
        if (type) {
            if (loc < 0) {
                res = "S" + String.valueOf(Math.abs(loc));
            } else {
                res = "N" + String.valueOf(Math.abs(loc));
            }
        } else {
            if (loc < 0) {
                res = "W" + String.valueOf(Math.abs(loc));
            } else {
                res = "E" + String.valueOf(Math.abs(loc));
            }
        }

        while (res.length() < len) {
            res = res.substring(0, 1) + "0" + res.substring(1);
        }
        return res;
    }

    public int getWidth() {
        return dim.get("width");
    }

    public int getHeight(){
        return dim.get("height");
    }
}
