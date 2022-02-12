package com.jpa;

public class terrainGenerator {
    public static void main(String[] args) {
        // heightmap worldHM = new heightmap(90, -180, 180, 360);

        /**
         * Individual tile testing
         */
        // heightmap belgiumHM = new heightmap(50, 4, 1, 1, 1);

        // model belgium3D = new model(belgiumHM, 20, 20);
        // belgium3D.generate("data/STLs/belgium.stl", "STL");       
        // belgium3D.generate("data/OBJs/belgium.obj", "OBJ");

        /**
         * Testing for individual tile orientation
         */
        // heightmap everestHM = new heightmap(27, 86, 3, 3, 10);

        // model everest3D = new model(everestHM, 20, 20);
        // everest3D.generate("data/STLs/everest.stl", "STL");
        // everest3D.generate("data/OBJs/everest.obj", "OBJ");

        /**
         * Test for large surfaces and errors
         */
        heightmap nileHM = new heightmap(31, 29, 4, 6, 15);

        model nile3D = new model(nileHM, 20, 20);
        // nile3D.generate("data/STLs/nileFlipped.stl", "STL");
        nile3D.generate("data/OBJs/nile.obj", "OBJ");
    }
}
