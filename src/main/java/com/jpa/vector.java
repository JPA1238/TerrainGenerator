package com.jpa;

import java.nio.ByteBuffer;

public class vector {
    public float x, y, z;

    vector () {
        setLocation(0f, 0f, 0f);
    }

    vector (float x, float y, float z) {
        setLocation(x, y, z);
    }

    vector (vector a) {
        setLocation(a.x, a.y, a.z);
    }

    public void setLocation (float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private byte[] floatToBytes(float i) {
        return ByteBuffer.allocate(4).putFloat(i).array();
    }

    public byte[] toBytes() {
        byte[] res = new byte[12];

        // reversed because little-endian
        res = insertByteArray(res, reverseByteArray(floatToBytes(this.x)), 0);
        res = insertByteArray(res, reverseByteArray(floatToBytes(this.y)), 4);
        res = insertByteArray(res, reverseByteArray(floatToBytes(this.z)), 8);

        // System.out.println(Arrays.toString(res));

        return res;
    }

    private byte[] insertByteArray(byte[] res, byte[] a, int location) {
        for (int i = 0; i < a.length; i++) {
            res[location + i] = a[i];
        }

        return res;
    }

    private byte[] reverseByteArray(byte[] a) {
        byte[] res = new byte[a.length];

        for (int i = 0; i < res.length; i++) {
            res[i] = a[a.length - i - 1];
        }

        return res;
    }
}
