package com.jpa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    byte[] header = new byte[80];
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

    public void generate(String path) {
        prepareSTL(path);

        reloadSettings();

        for (int i = 0; i < hm.getWidth(); i++) {
            for (int j = 0; j < hm.getHeight(); j++) {
                generateChunk(hm.getHeightValues(i, j), 0, path); // TODO offset
            }
        }

        updateTriangleCount(path);
        System.out.println("\nFacecount : " + triangleCount);
    }

    private void generateChunk(int[][] heightValues, int offset, String path) {
        for (int x = 0; x < heightValues.length - 1; x++) {
            byte[] mesh = new byte[(heightValues[x].length - 1) * 2 * 50];
            for (int y = 0; y < heightValues[x].length - 1; y++) {
                /**
                 * *-* LT - RT
                 * *-* LB - RB
                 */

                vector LB = new vector(x, y, heightValues[x][y]); // LB
                vector RB = new vector(x + 1, y, heightValues[x + 1][y]); // RB
                vector LT = new vector(x, y + 1, heightValues[x][y + 1]); // LT
                vector RT = new vector(x + 1, y + 1, heightValues[x + 1][y + 1]); // RT

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

    private void reloadSettings() {
        width = Float.parseFloat(settings.get("width"));
        height = Float.parseFloat(settings.get("height"));
        resolutionX = Float.parseFloat(settings.get("resolutionX"));
        resolutionY = Float.parseFloat(settings.get("resolutionY"));
        baseHeight = Float.parseFloat(settings.get("baseHeight"));
    }

    public void prepareSTL(String path) {
        byte[] name = path.split("/")[path.split("/").length - 1].getBytes();

        if (name.length <= 80) {
            header = insertByteArray(header, name, 0);
        } else {
            System.out.println("ERROR : STL name too big");
        }

        writeBytes(header, path, false);
        writeBytes(intToBytes(0), path, true);
    }

    public void updateTriangleCount(String path) {
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
