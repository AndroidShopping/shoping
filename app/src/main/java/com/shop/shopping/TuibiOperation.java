package com.shop.shopping;

public class TuibiOperation {
    public static final int HEAD = 0x5;
    public static final int MASTER = 0x10;
    public static final int MACHIEID_0 = 0X0;
    public static final int MACHIEID_1 = 0X1;
    public static final int MACHIEID_2 = 0X2;
    public static final int MACHIEID_3 = 0X3;
    public static final byte COMMAND_PAY_OUT = 0x14;

    public static byte[] buildRequest(byte command, byte data) {
        byte[] commandData = new byte[6];
        commandData[0] = HEAD;
        commandData[1] = MASTER;
        commandData[2] = MACHIEID_3;
        commandData[3] = command;
        commandData[4] = data;
        commandData[5] = (byte) (commandData[0] + commandData[1] + commandData[2] + commandData[3] + +commandData[4]);
        return commandData;
    }

}
