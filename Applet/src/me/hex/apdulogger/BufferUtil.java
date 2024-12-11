package me.hex.apdulogger;

public class BufferUtil {
    private final byte[] eeprom_buf;
    private short eeprom_start_index;
    private short eeprom_end_index;
    private boolean is_buf_full;
    private final short storage_size;

    public BufferUtil(byte[] eeprom_buf, short in_storage_size) {
        this.eeprom_buf = eeprom_buf;
        this.eeprom_start_index = 0;
        this.eeprom_end_index = 0;
        this.is_buf_full = false;
        this.storage_size = in_storage_size;
    }

    public void write_to_buffer(byte data) {
        eeprom_buf[eeprom_end_index++] = data;

        if (eeprom_end_index == storage_size) {
            eeprom_end_index = 0;
            is_buf_full = true;
        }
    }

    public short get_size() {
        return storage_size;
    }

    public short get_start_index() {
        return eeprom_start_index;
    }

    public short get_end_index() {
        return eeprom_end_index;
    }

    public boolean is_buffer_full() {
        return is_buf_full;
    }

    public void reset() {
        eeprom_start_index = 0;
        eeprom_end_index = 0;
        is_buf_full = false;
    }
}