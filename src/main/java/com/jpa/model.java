package com.jpa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

// units in cm
// settings stored as strings
public class model {
    float width, height, resolutionX, resolutionY, baseHeight;

    public HashMap<String, String> settings = new HashMap<String, String>();
    heightmap hm;

    byte[] header = new byte[80];
    byte[] triangleCount = new byte[4];
    ArrayList<triangle> mesh = new ArrayList<triangle>();

    model(heightmap hm, int width, int height) {
        this.settings.put("width", String.valueOf(width));
        this.settings.put("height", String.valueOf(height));
        this.settings.put("resolutionX", String.valueOf(Math.floor(width / hm.getWidth())));
        this.settings.put("resolutionY", String.valueOf(Math.floor(height / hm.getHeight())));

        this.settings.put("baseHeight", "2");

        this.hm = hm;

        reloadSettings();
    }

    public void generate() {
        reloadSettings();

        for (int i = 0; i < hm.getWidth(); i++) {
            for (int j = 0; j < hm.getHeight(); j++) {
                generateChunk(hm.getHeightValues(i, j), 0); // TODO offset
            }
        }

        System.out.println("\nFacecount : " + mesh.size());
        this.triangleCount = intToBytes(mesh.size());
    }

    private void generateChunk(int[][] heightValues, int offset) {
        for (int x = 0; x < heightValues.length - 1; x++) {
            for (int y = 0; y < heightValues[x].length - 1; y++) {
                /**
                 *  *-* LT - RT
                 *  *-* LB - RB
                 */

                vector LB = new vector(x    , y    , heightValues[x    ][y    ]); // LB
                vector RB = new vector(x + 1, y    , heightValues[x + 1][y    ]); // RB
                vector LT = new vector(x    , y + 1, heightValues[x    ][y + 1]); // LT
                vector RT = new vector(x + 1, y + 1, heightValues[x + 1][y + 1]); // RT

                /**
                System.out.println("\n");
                System.out.println("LB " + LB.x + " " + LB.y + " " + LB.z);
                System.out.println("RB " + RB.x + " " + RB.y + " " + RB.z);
                System.out.println("LT " + LT.x + " " + LT.y + " " + LT.z);
                System.out.println("RT " + RT.x + " " + RT.y + " " + RT.z);
                */
                

                triangle L = new triangle(LB, RB, LT);
                triangle R = new triangle(LT, RT, RB);

                this.mesh.add(L);
                this.mesh.add(R);
            }
        }
    }

    private void reloadSettings() {
        width = Float.parseFloat(settings.get("width"));
        height = Float.parseFloat(settings.get("height"));
        resolutionX = Float.parseFloat(settings.get("resolutionX"));
        resolutionY = Float.parseFloat(settings.get("resolutionY"));
        baseHeight = Float.parseFloat(settings.get("baseHeight"));
    }

    public void export(String path) {
        byte[] stl = new byte[80 + 4 + 50 * mesh.size()];

        byte[] toAdd = "Heightmap generator".getBytes(); // TODO Create name
        if (toAdd.length <= 80) {
            for (int i = 0; i < toAdd.length; i++) {
                this.header[i] = toAdd[i];
            }
        } else {
            System.out.println("ERROR : STL name too big");
        }

        stl = insertByteArray(stl, header, 0);
        stl = insertByteArray(stl, reverseByteArray(triangleCount), 80);

        for (int i = 0; i < mesh.size(); i++) {
            stl = insertByteArray(stl, mesh.get(i).toBytes(), 80 + 4 +50*i);
        }

        writeBytes(stl, path + "heightmap.stl");
    }

    public void writeBytes(byte[] bytes, String path) {
        try {
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);

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

    private byte[] insertByteArray(byte[] res, byte[] a, int location){
        for (int i = 0; i < a.length; i++) {
            res[location + i] = a[i];
        }

        return res;
    }

    private byte[] reverseByteArray(byte[] a){
        byte[] res = new byte[a.length];

        for (int i = 0; i < res.length; i++) {
            res[i] = a[a.length - i - 1];            
        }

        return res;
    }
}
