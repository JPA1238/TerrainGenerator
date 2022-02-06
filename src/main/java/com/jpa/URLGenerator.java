package com.jpa;

import java.io.File;
import java.io.FileReader;
import java.util.HashSet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class URLGenerator {
    private static String baseUrl = "ASTGTMV003_";
    private static String extension = "_dem.tif";

    private HashSet<String> possibleLoc = new HashSet<String>();

    public void getPossibleLoc() {
        JSONParser parser = new JSONParser();
        JSONObject json;
        try {
            json = (JSONObject) parser.parse(new FileReader("data/private/credentials.json"));

            File folder = new File((String) json.get("path"));
            for (String file : folder.list()) {
                file = file.replace(baseUrl, "").replace(extension, "");
                possibleLoc.add(file);
            }
        } catch (Exception e) {
            System.out.println("ERROR : Failed to gather credentials");
            e.printStackTrace();
        }
    }

    public boolean validLoc(String loc) {
        if (possibleLoc.contains(loc)) {
            return true;
        } else {
            return false;
        }
    }
}
