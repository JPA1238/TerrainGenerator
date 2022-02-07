package com.jpa;

public class terrainGenerator {
    public static void main(String[] args) {
        // heightmap worldHM = new heightmap(90, -180, 180, 360);

        // heightmap belgiumHM = new heightmap(50, 4, 1, 1);

        // model belgium3D = new model(belgiumHM, 20, 20);
        // belgium3D.generate("data/STLs/belgium.stl", "stl");       

        // heightmap everestHM = new heightmap(27, 86, 3, 3, 10);

        // model everest3D = new model(everestHM, 20, 20);
        // everest3D.generate("data/STLs/everest.stl", "STL");
        // everest3D.generate("data/OBJs/everest.obj", "OBJ");

        // heightmap nileHM = new heightmap(24, 29, 7, 3, 15);
        heightmap nileHM = new heightmap(31, 29, 5, 6, 15);

        model nile3D = new model(nileHM, 20, 20);
        nile3D.generate("data/STLs/nile2.stl", "STL");
    }
}
