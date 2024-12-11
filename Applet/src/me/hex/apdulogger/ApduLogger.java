package me.hex.apdulogger;

import javacard.framework.*;

public class ApduLogger extends Applet {
    private Commands commands;

    protected ApduLogger() {
        commands = new Commands();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new ApduLogger().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
    }

    public void process(APDU apdu) {
        if (selectingApplet()) {
            return;
        }

        byte[] buf = apdu.getBuffer();
        byte cla = buf[ISO7816.OFFSET_CLA];
        byte ins = buf[ISO7816.OFFSET_INS];

        if (commands.is_init_buffer_command(cla, ins)) {
            commands.init_dump_buffer(apdu);
        }

        if (!commands.is_initialized()) {
            ISOException.throwIt(AppletException.ERROR_NOT_INITIALIZED);
            return;
        }

        if (commands.is_logger_cla(cla)) {
            switch (ins) {
                case Commands.INS_READ_DATA:
                    commands.read_data(apdu);
                    return;
                case Commands.INS_CLEAR_BUFFER:
                    commands.clear_buffer();
                    return;
                case Commands.INS_GET_SIZE_BUFFER:
                    commands.get_size_command(apdu);
                    return;
                default:
                    break;
            }
        }

        commands.log_command(apdu);
    }
}