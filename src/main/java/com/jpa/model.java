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
    /*
     *  Standard settigns
     */
    private float width, height, resolutionX, resolutionY, baseHeight;
    private boolean smoothing;
    public HashMap<String, String> settings = new HashMap<String, String>();

    /*
     *  member variables
     */
    private heightmap hm;

    private int vertexCount = 0;
    private int triangleCount = 0;

    private static int smoothingResolution = 7; // <-- r+1+r | ex: 5 --> 2+1+2 --> 2 neighbours on each side

    model(heightmap hm, int width, int height) {
        this.settings.put("width", String.valueOf(width));
        this.settings.put("height", String.valueOf(height));
        this.settings.put("resolutionX", String.valueOf(Math.floor(width / hm.getWidth())));
        this.settings.put("resolutionY", String.valueOf(Math.floor(height / hm.getHeight())));
        this.settings.put("smoothing", "false");

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
                        if (smoothing) {
                            float[][] heightValues = hm.getHeightValues(i, j);
                            float[][] newHeightValues = heightValues;

                            int radius = (smoothingResolution - 1) / 2;

                            for (int x = radius; x < heightValues.length - radius; x++) {
                                for (int y = radius; y < heightValues[x].length - radius; y++) {
                                    float[][] neighbours = new float[smoothingResolution][smoothingResolution];
                                    for (int sx = - radius; sx <= radius; sx++) {
                                        for (int sy = - radius; sy <= radius; sy++) {
                                            neighbours[sx + radius ][sy + radius] = heightValues[x+sx][y+sy];
                                        }
                                    }
                                    
                                    float[][] weights = new float[smoothingResolution][smoothingResolution];
                                    for (int sx = 0; sx < smoothingResolution; sx++) {
                                        for (int sy = 0; sy < smoothingResolution; sy++) {
                                            weights[sx][sy] = (Math.abs(Math.abs(sx - radius) - radius) + radius) * (Math.abs(Math.abs(sy - radius) - radius) + radius);
                                        }
                                    }

                                    newHeightValues[x][y] = flatten(neighbours, weights);
                                }
                            }
                            heightValues = newHeightValues;

                            generateChunk(heightValues, i, j, path);
                        } else {
                            generateChunk(hm.getHeightValues(i, j), i, j, path);
                        }
                    }
                }
                updateTriangleCount(path);
                System.out.println("\nFacecount : " + triangleCount);
                break;
            case "OBJ":
                // TODO fix naming
                String name = path.split("/")[path.split("/").length - 1];
                write("o " + name + "\n", path, false);

                int[][][] individualVertexCount = new int[hm.getWidth()][hm.getHeight()][2];

                for (int i = 0; i < hm.getWidth(); i++) {
                    for (int j = 0; j < hm.getHeight(); j++) {
                        if (smoothing) {
                            float[][] heightValues = hm.getHeightValues(i, j);
                            float[][] newHeightValues = heightValues;

                            int radius = (smoothingResolution - 1) / 2;

                            for (int x = radius; x < heightValues.length - radius; x++) {
                                for (int y = radius; y < heightValues[x].length - radius; y++) {
                                    float[][] neighbours = new float[smoothingResolution][smoothingResolution];
                                    for (int sx = -radius; sx <= radius; sx++) {
                                        for (int sy = -radius; sy <= radius; sy++) {
                                            neighbours[sx + radius][sy + radius] = heightValues[x + sx][y + sy];
                                        }
                                    }

                                    float[][] weights = new float[smoothingResolution][smoothingResolution];
                                    for (int sx = 0; sx < smoothingResolution; sx++) {
                                        for (int sy = 0; sy < smoothingResolution; sy++) {
                                            weights[sx][sy] = (Math.abs(Math.abs(sx - radius) - radius) + radius) * (Math.abs(Math.abs(sy - radius) - radius) + radius);
                                        }
                                    }

                                    newHeightValues[x][y] = flatten(neighbours, weights);
                                }
                            }
                            heightValues = newHeightValues;

                            individualVertexCount[i][j] = generateVertices(heightValues, i, j, path);
                        } else {
                            individualVertexCount[i][j] = generateVertices(hm.getHeightValues(i, j), i, j, path);
                        }
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
        smoothing = Boolean.parseBoolean(settings.get("smoothing"));
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

    private void generateChunk(float[][] heightValues, int offsetX, int offsetY, String path) {
        offsetX *= heightValues.length;
        offsetY *= heightValues[0].length;

        for (int x = 0; x < heightValues.length - 1; x++) {
            byte[] mesh = new byte[(heightValues[x].length - 1) * 2 * 50];
            for (int y = 0; y < heightValues[x].length - 1; y++) {
                /**
                 * *-* LT - RT
                 * *-* LB - RB
                 */

                vector LB = new vector(x     + offsetX, y     + offsetY, heightValues[heightValues[x].length - 1 - (y    )][x    ]); // LB
                vector RB = new vector(x + 1 + offsetX, y     + offsetY, heightValues[heightValues[x].length - 1 - (y    )][x + 1]); // RB
                vector LT = new vector(x     + offsetX, y + 1 + offsetY, heightValues[heightValues[x].length - 1 - (y + 1)][x    ]); // LT
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

    private int[] generateVertices(float[][] heightValues, int offsetX, int offsetY, String path) {
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
        int[] vc = { heightValues.length, heightValues[0].length };
        return vc;
    }

    private void generateFaces(int vcX, int vcY, int offset, String path) {
        for (int x = 1; x < vcX; x++) {
            String faces = "";
            for (int y = 1; y < vcY; y++) {

                /**
                 * (y - 1) * vcX + x (y - 1) * vcX + x + 1
                 * 
                 * y * vcC + c y * vcX + x + 1
                 */

                faces += "f " + ((y - 1) * vcX + x + 1 + offset) + " " + (y * vcX + x + offset) + " " + ((y - 1) * vcX + x + offset) + "\n";
                faces += "f " + ((y - 1) * vcX + x + 1 + offset) + " " + (y * vcX + x + offset) + " " + (y * vcX + x + 1 + offset) + "\n";

                triangleCount += 2;
            }
            write(faces, path, true);
        }
    }

    /*
     * Algoritms
     */

    private float flatten(float[][] tiles, float[][] weight) {
        float avg = 0;
        float totalWeight = 0;
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                avg += tiles[x][y] * weight[x][y];
                totalWeight += weight[x][y];
            }
        }
        return avg / (totalWeight);
    }

    /*
     * Helper functions
     */

    private void writeBytes(byte[] bytes, String path, boolean append) {
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
