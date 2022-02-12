package com.jpa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;

// units in cm
// settings stored as strings
public class model {
    float width, height, resolutionX, resolutionY, baseHeight;
    public HashMap<String, String> settings = new HashMap<String, String>();
    heightmap hm;

    int vertexCount = 0;
    int triangleCount = 0;

    model(heightmap hm, int width, int height) {
        this.settings.put("width", String.valueOf(width));
        this.settings.put("height", String.valueOf(height));
        this.settings.put("resolutionX", String.valueOf(Math.floor(width / hm.getWidth())));
        this.settings.put("resolutionY", String.valueOf(Math.floor(height / hm.getHeight())));

        this.settings.put("baseHeight", "2");

        this.hm = hm;

        reloadSettings();
    }

    public void generate(String path, String fileType) {
        reloadSettings();

        switch (fileType) {
            case "STL":
                prepareSTL(path);
                for (int i = 0; i < hm.getWidth(); i++) {
                    for (int j = 0; j < hm.getHeight(); j++) {
                        generateChunk(hm.getHeightValues(i, j), i, j, path);
                    }
                }
                updateTriangleCount(path);
                System.out.println("\nFacecount : " + triangleCount);
                break;
            case "OBJ":
                String name = path.split("/")[path.split("/").length - 1];
                write("o " + name + "\n", path, false);

                int[][][] individualVertexCount = new int[hm.getWidth()][hm.getHeight()][2];

                for (int i = 0; i < hm.getWidth(); i++) {
                    for (int j = 0; j < hm.getHeight(); j++) {
                        individualVertexCount[i][j] = generateVertices(hm.getHeightValues(i, j), i, j, path);
                    }
                }
                int offset = 0;
                for (int i = 0; i < hm.getWidth(); i++) {
                    for (int j = 0; j < hm.getHeight(); j++) {
                        generateFaces(individualVertexCount[i][j][0], individualVertexCount[i][j][1], offset, path);
                        offset += individualVertexCount[i][j][0] * individualVertexCount[i][j][1];
                    }
                }
                System.out.println("\nVertexcount : " + vertexCount);
                System.out.println("Facecount : " + triangleCount);
                break;
            default:
                System.out.println("ERROR : invalid filetype");
                break;
        }
    }

    private void reloadSettings() {
        width = Float.parseFloat(settings.get("width"));
        height = Float.parseFloat(settings.get("height"));
        resolutionX = Float.parseFloat(settings.get("resolutionX"));
        resolutionY = Float.parseFloat(settings.get("resolutionY"));
        baseHeight = Float.parseFloat(settings.get("baseHeight")); // TODO implement
    }

    /*
     * STL generation
     */

    private void prepareSTL(String path) {
        byte[] header = new byte[80];
        byte[] name = path.split("/")[path.split("/").length - 1].getBytes();

        if (name.length <= 80) {
            header = insertByteArray(header, name, 0);
        } else {
            System.out.println("ERROR : STL name too big");
        }

        writeBytes(header, path, false);
        writeBytes(intToBytes(0), path, true);
    }

    private void generateChunk(int[][] heightValues, int offsetX, int offsetY, String path) {
        offsetX *= heightValues.length;
        offsetY *= heightValues[0].length;

        for (int x = 0; x < heightValues.length - 1; x++) {
            byte[] mesh = new byte[(heightValues[x].length - 1) * 2 * 50];
            for (int y = 0; y < heightValues[x].length - 1; y++) {
                /**
                 * *-* LT - RT
                 * *-* LB - RB
                 */

                vector LB = new vector(x +     offsetX, y     + offsetY, heightValues[heightValues[x].length - 1 - (y    )][x    ]); // LB
                vector RB = new vector(x + 1 + offsetX, y     + offsetY, heightValues[heightValues[x].length - 1 - (y    )][x + 1]); // RB
                vector LT = new vector(x +     offsetX, y + 1 + offsetY, heightValues[heightValues[x].length - 1 - (y + 1)][x    ]); // LT
                vector RT = new vector(x + 1 + offsetX, y + 1 + offsetY, heightValues[heightValues[x].length - 1 - (y + 1)][x + 1]); // RT

                /**
                 * System.out.println("\n");
                 * System.out.println("LB " + LB.x + " " + LB.y + " " + LB.z);
                 * System.out.println("RB " + RB.x + " " + RB.y + " " + RB.z);
                 * System.out.println("LT " + LT.x + " " + LT.y + " " + LT.z);
                 * System.out.println("RT " + RT.x + " " + RT.y + " " + RT.z);
                 */

                triangle L = new triangle(LB, RB, LT);
                triangle R = new triangle(LT, RT, RB);

                triangleCount += 2;

                mesh = insertByteArray(mesh, L.toBytes(), 2 * 50 * y);
                mesh = insertByteArray(mesh, R.toBytes(), 2 * 50 * y + 50);
            }
            writeBytes(mesh, path, true);
        }
    }

    private void updateTriangleCount(String path) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(path, "rw");
        } catch (FileNotFoundException e) {
            System.out.println("ERROR : Couldn't find file " + path);
            e.printStackTrace();
        }
        byte[] trCB = intToBytes(triangleCount);
        for (int i = 80; i < 4; i++) {
            try {
                raf.seek(80 + i);
                raf.write(trCB[trCB.length - i - 1]);
            } catch (IOException e) {
                System.out.println("ERROR : Couldn't write to file " + path);
                e.printStackTrace();
            }
        }
        try {
            raf.close();
        } catch (IOException e) {
            System.out.println("ERROR : Coudln't close file " + path);
            e.printStackTrace();
        }
    }

    /*
     * OBJ generation
     */

    private int[] generateVertices(int[][] heightValues, int offsetX, int offsetY, String path) {
        offsetX *= heightValues.length;
        offsetY *= heightValues[0].length;

        for (int x = 0; x < heightValues.length; x++) {
            String vertices = "";
            for (int y = 0; y < heightValues[x].length; y++) {
                vector v = new vector(x + offsetX, y + offsetY, heightValues[heightValues[x].length - 1 - (y)][x]);
                vertices += v.toString() + "\n";
                vertexCount += 1;
            }
            write(vertices, path, true);
        }
        int[] vc = {heightValues.length, heightValues[0].length};
        return vc;
    }

    private void generateFaces(int vcX, int vcY, int offset, String path) {
        for (int x = 1; x < vcX; x++) {
            String faces = "";
            for (int y = 1; y < vcY; y++) {

                /**
                 *  (y - 1) * vcX + x           (y - 1) * vcX + x + 1
                 * 
                 *  y * vcC + c                 y * vcX + x + 1
                 */

                faces += "f " + ((y - 1) * vcX + x + 1 + offset) + " " + (y * vcX + x + offset) + " " + ((y - 1) * vcX + x     + offset) + "\n";
                faces += "f " + ((y - 1) * vcX + x + 1 + offset) + " " + (y * vcX + x + offset) + " " + (y       * vcX + x + 1 + offset) + "\n";

                triangleCount += 2;
            }
            write(faces, path, true);
        }
    }

    /*
     * Helper functions
     */

    public void writeBytes(byte[] bytes, String path, boolean append) {
        try {
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file, append);

            fos.write(bytes);

            fos.close();
        } catch (IOException e) {
            System.out.println("ERROR : Failed to write file");
            e.printStackTrace();
        }
    }

    private void write(String str, String path, boolean append) {
        try {
            File f = new File(path);
            FileWriter fw = new FileWriter(f, append);

            fw.write(str);

            fw.close();
        } catch (IOException e) {
            System.out.println("ERROR : Couldn't read file " + path);
        }
    }

    private byte[] intToBytes(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    private byte[] insertByteArray(byte[] res, byte[] a, int location) {
        for (int i = 0; i < a.length; i++) {
            res[location + i] = a[i];
        }
        return res;
    }
}
