package com.jpa;

public class terrainGenerator {
    public static void main(String[] args) {


        heightmap belgiumHM = new heightmap(50, 4, 1, 1);
        // heightmap worldHM = new heightmap(90, -180, 180, 360);

        model belgium3D = new model(belgiumHM, 20, 20);
        belgium3D.settings.put("baseHeight", "1");
        belgium3D.generate();
        belgium3D.export("data/STLs/");
    }
}
