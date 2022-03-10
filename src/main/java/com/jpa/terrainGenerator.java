package com.jpa;

public class terrainGenerator {
    public static void main(String[] args) {
        // heightmap worldHM = new heightmap(90, -180, 180, 360);

        /**
         * Individual tile testing
         */
        heightmap belgiumHM = new heightmap(50, 4, 1, 1, 1);

        model belgium3D = new model(belgiumHM);
        belgium3D.settings.put("smoothing", "true");
        // belgium3D.generate("data/STLs/belgium.stl", "STL");       
        belgium3D.generate("data/OBJs/belgium2.obj", "OBJ");

        /**
         * Testing for individual tile orientation
         */
        // heightmap everestHM = new heightmap(27, 86, 3, 3, 10);

        // model everest3D = new model(everestHM);
        // // everest3D.generate("data/STLs/everest.stl", "STL");
        // everest3D.generate("data/OBJs/everestRealLength.obj", "OBJ");

        /**
         * Test for large surfaces and errors
         */
        // heightmap nileHM = new heightmap(31, 29, 10, 20, 50);

        // model nile3D = new model(nileHM, 20, 20);
        // nile3D.settings.put("smoothing", "true");
        // nile3D.generate("data/OBJs/nile.obj", "OBJ");

        // heightmap nileHMFinerRes = new heightmap(31, 29, 10, 20, 45);

        // model nile3DFinerRes = new model(nileHMFinerRes, 20, 20);
        // nile3DFinerRes.settings.put("smoothing", "true");
        // nile3DFinerRes.generate("data/OBJs/nileFinerRes.obj", "OBJ");

        heightmap nileHMEvenFinerRes = new heightmap(31, 29, 8, 15, 25);

        model nile3DEvenFinerRes = new model(nileHMEvenFinerRes);
        nile3DEvenFinerRes.settings.put("smoothing", "true");
        nile3DEvenFinerRes.generate("data/OBJs/nileEvenFinerRes.obj", "OBJ");


        /**
         * Just because I can
         */
        // heightmap hmWorld = new heightmap(82, -180, 180-(90-82)-(90-83), 360, 200);

        // model world = new model(hmWorld, 20, 20);
        // world.settings.put("smoothing", "true");
        // world.generate("data/OBJs/worldHighRes.obj", "OBJ");
    }
}
