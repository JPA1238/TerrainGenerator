package com.jpa;

public class terrainGenerator {
    public static void main(String[] args) {
        // heightmap worldHM = new heightmap(90, -180, 180, 360);

        /** 
        heightmap belgiumHM = new heightmap(50, 4, 1, 1);

        model belgium3D = new model(belgiumHM, 20, 20);
        belgium3D.settings.put("baseHeight", "1");
        belgium3D.generate();
        belgium3D.export("data/STLs/"); 
        */

        heightmap everestHM = new heightmap(27, 86, 1, 1);

        model everest3D = new model(everestHM, 20, 20);
        everest3D.generate();
        everest3D.export("data/STLs/");
    }
}
