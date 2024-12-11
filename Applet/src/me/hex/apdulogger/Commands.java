package me.hex.apdulogger;

import javacard.framework.*;

public class Commands {
    public static final byte CLA_APPLET = (byte) 0xEE;
    public static final byte INS_INIT_BUFF = (byte) 0xA0;
    public static final byte INS_READ_DATA = (byte) 0xF0;
    public static final byte INS_CLEAR_BUFFER = (byte) 0xF1;
    public static final byte INS_GET_SIZE_BUFFER = (byte) 0xF2;

    private BufferUtil buffer_util;
    private byte[] eeprom_buf;
    private short data_size;

    private boolean is_init = false;

    public void init_dump_buffer(APDU apdu) {
        byte[] buf = apdu.getBuffer();

        byte lc = buf[ISO7816.OFFSET_LC];
        if (lc <= 0 || lc > (short) (buf.length - ISO7816.OFFSET_CDATA)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        data_size = AppletUtil.bytearray_to_short(buf, ISO7816.OFFSET_CDATA, lc);
        if (data_size <= 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        this.eeprom_buf = new byte[data_size];
        this.buffer_util = new BufferUtil(this.eeprom_buf, data_size);

        is_init = true;

        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }

    public void read_data(APDU apdu) {
        short max_response_size = 256;

        byte[] buf = apdu.getBuffer();
        byte P1 = buf[ISO7816.OFFSET_P1];
        byte P2 = buf[ISO7816.OFFSET_P2];

        short offset = AppletMath.combine_byte(P1, P2);
        short start_index = (short) (offset * max_response_size);

        if (start_index >= data_size) {
            apdu.setOutgoing();
            apdu.setOutgoingLength((short) 0);
            return;
        }

        short length = AppletMath.min(max_response_size, (short) (data_size - start_index));

        apdu.setOutgoing();
        apdu.setOutgoingLength(length);
        apdu.sendBytesLong(eeprom_buf, start_index, length);

        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }

    public void clear_buffer() {
        Util.arrayFillNonAtomic(eeprom_buf, (short) 0, data_size, (byte) 0x00);

        buffer_util.reset();

        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }

    public void log_command(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short bytes_to_write = (short) (5 + apdu.setIncomingAndReceive() + Constants.DELIMITER_SIZE);

        if (bytes_to_write > (short) ((short) (data_size * 3) / 4)) {
            for (short j = 0; j < Constants.APDU_TOO_LONG_SIZE; j++) {
                buffer_util.write_to_buffer(Constants.APDU_TOO_LONG[j]);
            }
            return;
        }

        buffer_util.write_to_buffer(buf[ISO7816.OFFSET_CLA]);
        buffer_util.write_to_buffer(buf[ISO7816.OFFSET_INS]);
        buffer_util.write_to_buffer(buf[ISO7816.OFFSET_P1]);
        buffer_util.write_to_buffer(buf[ISO7816.OFFSET_P2]);
        buffer_util.write_to_buffer(buf[ISO7816.OFFSET_LC]);

        short data_len = apdu.getIncomingLength();
        for (short i = 0; i < data_len; i++) {
            buffer_util.write_to_buffer(buf[(short) (i + ISO7816.OFFSET_CDATA)]);
        }

        for (short j = 0; j < Constants.DELIMITER_SIZE; j++) {
            buffer_util.write_to_buffer(Constants.DELIMITER[j]);
        }

        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }

    public void get_size_command(APDU apdu) {
        short buffer_size = buffer_util.get_size();
        byte[] bufferSizeArray = AppletUtil.short_to_bytearray(buffer_size);
        short length = (short) bufferSizeArray.length;

        apdu.setOutgoing();
        apdu.setOutgoingLength(length);
        apdu.sendBytesLong(bufferSizeArray, (short) 0, length);

        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }

    public boolean is_logger_cla(byte cla) {
        return cla == CLA_APPLET;
    }

    public boolean is_init_buffer_command(byte cla, byte ins) {
        return cla == CLA_APPLET && ins == INS_INIT_BUFF;
    }

    public boolean is_initialized() {
        return is_init;
    }
}
