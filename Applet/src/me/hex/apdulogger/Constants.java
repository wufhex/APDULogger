package me.hex.apdulogger;

public class Constants {
    public static final byte[] DELIMITER = { (byte) 0xDE, (byte) 0xAD };
    public static final short DELIMITER_SIZE = 2;

    public static final byte[] APDU_TOO_LONG = { (byte) 0xCL, (byte) 0xAC };
    public static final short APDU_TOO_LONG_SIZE = 2;
}