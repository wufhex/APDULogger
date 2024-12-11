package me.hex.apdulogger;

public class AppletMath {
    public static short min(short a, short b) {
        return (a < b) ? a : b;
    }

    public static short combine_byte(byte a, byte b) {
        return (short) (((a & 0xFF) << 8) | (b & 0xFF));
    }
}