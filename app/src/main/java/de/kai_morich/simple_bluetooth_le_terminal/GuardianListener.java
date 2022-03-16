package de.kai_morich.simple_bluetooth_le_terminal;

interface GuardianListener extends SerialListener {
    @Override
    default void onSerialConnect () {};
    @Override
    default void onSerialConnectError (Exception e) {};
    @Override
    default void onSerialIoError (Exception e) {};

    void onStatus(String str);
    void onConnect();
}
