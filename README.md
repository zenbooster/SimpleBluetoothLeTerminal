# Форк добавляющий TCP-сервер с возможностью отправки данных с датчика TCP клиентам.
Работая с ЭМГ датчиком ELEMYO MYOblue v1.0, заметил, что если связывать его напрямую с компьютером, связь становится нестабильной уже на расстоянии нескольких метров. Возможно это связано с тем, что рядом расположена точка доступа WiFi и из-за этого условия связи ухудшаются. Этот форк был сделан для того, чтобы иметь возможность использовать смартфон на Android в качестве ретранслятора данных от датчика до компьютера. Конечно, можно найти массу других применений на ваш вкус! )) 

Реализовано подменю с настройками TCP сервера. Данные могут отправляться клиентам даже когда приложение находится в фоне. Если пропадает связь с BlueTooth устройством, запускается механизм восстановления соединения.

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3f9ba45b5c5449179150010659311f57)](https://www.codacy.com/manual/kai-morich/SimpleBluetoothLeTerminal?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=kai-morich/SimpleBluetoothLeTerminal&amp;utm_campaign=Badge_Grade)

# SimpleBluetoothLeTerminal

This Android app provides a line-oriented terminal / console for Bluetooth LE (4.x) devices implementing a custom serial profile

For an overview on Android BLE communication see 
[Android Bluetooth LE Overview](https://developer.android.com/guide/topics/connectivity/bluetooth/ble-overview).

In contrast to classic Bluetooth, there is no predifined serial profile for Bluetooth LE, 
so each vendor uses GATT services with different service and characteristic UUIDs.

This app includes UUIDs for widely used serial profiles:
- Nordic Semiconductor nRF51822  
- Texas Instruments CC254x
- Microchip RN4870/1
- Telit Bluemod

## Motivation

I got various requests asking for help with Android development or source code for my
[Serial Bluetooth Terminal](https://play.google.com/store/apps/details?id=de.kai_morich.serial_bluetooth_terminal) app.
Here you find a simplified version of my app.
