package com.jpa;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

public class URLGenerator {
    private static String baseUrl = "https://data.lpdaac.earthdatacloud.nasa.gov/lp-prod-protected/ASTGTM.003/ASTGTMV003_";
    private static String extension = ".tif";

    private HashSet<String> possibleLoc = new HashSet<String>();

    public void getPossibleLoc(String path) {       
        try {
            File inputURLs = new File(path + "downloadURLs.txt");
            // System.out.println(inputURLs.getAbsolutePath());

            Scanner sc = new Scanner(inputURLs);

            while (sc.hasNextLine()) {
                String data = sc.nextLine().replaceAll(baseUrl, "").replaceAll(extension, "").split("_")[0];

                possibleLoc.add(data);
            }

            sc.close();

        } catch (FileNotFoundException e) {
            System.out.println("Could not read file in : " + path);
        }

        // System.out.println(possibleLat);
        // System.out.println(possibleLon);
    }

    public boolean validLoc(String loc){
        if (possibleLoc.contains(loc)) {
            return true;
        } else {
            return false;
        }
    }
}
