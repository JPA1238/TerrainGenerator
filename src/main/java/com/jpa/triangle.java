package com.jpa;

public class triangle {
    vector[] face = new vector[3];
    vector normal = null;

    triangle(vector a, vector b, vector c) {
        this.face[0] = a;
        this.face[1] = b;
        this.face[2] = c;

        // System.out.println(a.x + "\t" + a.y + "\t" + a.z);
        // System.out.println(b.x + "\t" + b.y + "\t" + b.z);
        // System.out.println(c.x + "\t" + c.y + "\t" + c.z);

        calculateNormal();
    }

    public void setVertex(vector x, int position) {
        if (position < 3) {
            this.face[position] = x;
        } else {
            System.out.println("ERROR : Specify vertex 0, 1 or 2");
        }
    }

    public void calculateNormal() {
        vector U = new vector(subtract(this.face[0], this.face[1]));
        vector V = new vector(subtract(this.face[2], this.face[1]));

        float x = (U.y * V.z) - (U.z * V.y);
        float y = (U.z * V.x) - (U.x * V.z);
        float z = (U.x * V.y) - (U.y * V.x);

        /**
         * // normalization
         * double magnitude = Math.sqrt(x*x + y*y + z*z);
         * 
         * x /= magnitude;
         * y /= magnitude;
         * z /= magnitude;
         */

        // System.out.println(x + "\t" + y + "\t" + z);

        normal = new vector(x, y, z);
    }
    
    public vector subtract(vector a, vector b) {
        
        return new vector(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public byte[] toBytes() {
        byte[] res = new byte[50];

        res = insertByteArray(res, normal.toBytes(), 0);
        res = insertByteArray(res, this.face[0].toBytes(), 12);
        res = insertByteArray(res, this.face[1].toBytes(), 24);
        res = insertByteArray(res, this.face[2].toBytes(), 36);
        res = insertByteArray(res, new byte[2], 48);

        return res;
    }

    private byte[] insertByteArray(byte[] res, byte[] a, int location) {
        for (int i = 0; i < a.length; i++) {
            res[location + i] = a[i];
        }
        return res;
    }
}
