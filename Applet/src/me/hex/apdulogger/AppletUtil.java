package me.hex.apdulogger;

public class AppletUtil {
    public static byte[] short_to_bytearray(short value) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) (value >> 8);
        byteArray[1] = (byte) value;
        return byteArray;
    }

    public static short bytearray_to_short(byte[] buf, short offset, byte length) {
        short dataSize = 0;

        for (short i = 0; i < length; i++) {
            dataSize <<= 8;
            dataSize |= (short) (buf[(short) (offset + i)] & 0xFF);
        }

        return dataSize;
    }
}