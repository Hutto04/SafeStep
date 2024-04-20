---
sidebar_position: 6
---

# Bluetooth

A guide to setting up Bluetooth on your microcontroller (must have Bluetooth capabilities, of course).

## Introduction

This was by far one of the biggest challenges we faced in our project. 

Luckily for you, we got it working and we're here to share our knowledge with you.

## Requirements

- A microcontroller with Bluetooth capabilities (we used **Pi Pico W**)
- A external Android device with Bluetooth capabilities

## Setting up the microcontroller

1. First, set up your microcontroller with firmware that supports Bluetooth. 
We used the **Pi Pico W** with `soft/micropython-firmware-pico-w-130623.uf2` - which can be found [here](https://datasheets.raspberrypi.com/?_gl=1*4i43tq*_ga*MjA4MjIyODA2OS4xNzEzMzMyNDIw*_ga_22FD70LWDS*MTcxMzY0NzY1NC4xLjEuMTcxMzY0ODgwNC4wLjAuMA..) (scroll to the bottom).

2. Next, just add the following code to your microcontroller:

**ple_ble_mult_sensor.py:**
```python
import bluetooth
import struct
import time
import machine
from machine import Pin, ADC
import ubinascii
from ble_advertising import advertising_payload
from micropython import const

# Constants
_IRQ_CENTRAL_CONNECT = const(1)
_IRQ_CENTRAL_DISCONNECT = const(2)
_FLAG_READ = const(0x0002)
_FLAG_NOTIFY = const(0x0010)
_FLAG_INDICATE = const(0x0020)

# Multiplexer setup
s0 = Pin(0, Pin.OUT)
s1 = Pin(1, Pin.OUT)
s2 = Pin(2, Pin.OUT)
s3 = Pin(3, Pin.OUT)
SIG_pin = ADC(0)

def read_mux(channel):
    control_pin = [s0, s1, s2, s3]
    mux_channel = [
        [0, 0, 0, 0], [1, 0, 0, 0], [0, 1, 0, 0], [1, 1, 0, 0],
        [0, 0, 1, 0], [1, 0, 1, 0], [0, 1, 1, 0], [1, 1, 1, 0],
        [0, 0, 0, 1], [1, 0, 0, 1], [0, 1, 0, 1], [1, 1, 0, 1],
        [0, 0, 1, 1], [1, 0, 1, 1], [0, 1, 1, 1], [1, 1, 1, 1]
    ]
    for i in range(4):
        control_pin[i].value(mux_channel[channel][i])
    return SIG_pin.read_u16()

class BLESensor:
    def __init__(self, ble, name=""):
        self._ble = ble
        self._ble.active(True)
        self._ble.irq(self._irq)
        name = name or 'Pico %s' % ubinascii.hexlify(self._ble.config('mac')[1], ':').decode().upper()
        print('Sensor name %s' % name)

        # Service and characteristics setup
        self._service_uuid = bluetooth.UUID("00001810-0000-1000-8000-00805f9b34fb")
        self._temp_uuid = bluetooth.UUID(0x2A6E)
        self._pressure_uuid = bluetooth.UUID("0000210f-0000-1000-8000-00805f9b34fb")
        self._service = (self._service_uuid, [
            (self._temp_uuid, _FLAG_READ | _FLAG_NOTIFY | _FLAG_INDICATE),
            (self._pressure_uuid, _FLAG_READ | _FLAG_NOTIFY | _FLAG_INDICATE)
        ])
        self._connections = set()  # Initialize the _connections attribute
        self._handles = self._ble.gatts_register_services([self._service])[0]
        self._payload = advertising_payload(name=name, services=[self._service_uuid])
        self._advertise()

    def update_sensors(self):
        temps = []
        pressures = []
        for i in range(8):  # 8 temperature sensors followed by 8 pressure sensors
            temp_reading = read_mux(i)
            pressure_reading = read_mux(i + 8)
            
            # Example conversion...
            converted_temp = round(((temp_reading * 3.3 / 65535) * 100) - 301.89, 2)  # Example
            converted_pressure = round((pressure_reading * 3.3 / 65535 * 1000) - 3294.06, 2)  # Example conversion
            
            if (converted_pressure <= 0):
                converted_pressure = 0
            
            temps.append(converted_temp)
            pressures.append(converted_pressure)
            
            # Print converted values
        print(f"Temperatures: {temps}")  # Print temperature readings in °C
        print(f"Pressures: {pressures}")  # Print pressure readings in Pa or other units

        # If packing data to send over BLE, you would pack converted values
        temp_bytes = struct.pack('<8f', *temps)
        pressure_bytes = struct.pack('<8f', *pressures)
        self._ble.gatts_write(self._handles[0], temp_bytes)
        self._ble.gatts_write(self._handles[1], pressure_bytes)

        # Send notifications to connected clients
        for conn_handle in self._connections:
            self._ble.gatts_notify(conn_handle, self._handles[0])  # Notify temperature characteristic
            self._ble.gatts_notify(conn_handle, self._handles[1])  # Notify pressure characteristic


    def _advertise(self):
        self._ble.gap_advertise(500000, adv_data=self._payload)

    def _irq(self, event, data):
        if event == _IRQ_CENTRAL_CONNECT:
            conn_handle, _, _ = data
            self._connections.add(conn_handle)
        elif event == _IRQ_CENTRAL_DISCONNECT:
            conn_handle, _, _ = data
            self._connections.remove(conn_handle)
            self._advertise()

def demo():
    ble = bluetooth.BLE()
    sensor = BLESensor(ble)
    while True:
        sensor.update_sensors()
        print("--------------")
        time.sleep(15)

if __name__ == "__main__":
    demo()

```

**ble_advertising.py:**
```python
# Helpers for generating BLE advertising payloads.

from micropython import const
import struct
import bluetooth

# Advertising payload types
_ADV_TYPE_FLAGS = const(0x01)
_ADV_TYPE_NAME = const(0x09)
_ADV_TYPE_UUID16_COMPLETE = const(0x3)
_ADV_TYPE_UUID32_COMPLETE = const(0x5)
_ADV_TYPE_UUID128_COMPLETE = const(0x7)
_ADV_TYPE_UUID16_MORE = const(0x2)
_ADV_TYPE_UUID32_MORE = const(0x4)
_ADV_TYPE_UUID128_MORE = const(0x6)
_ADV_TYPE_APPEARANCE = const(0x19)

def advertising_payload(name=None, services=None, appearance=0, manufacturer_data=None):
    """
    Generate a payload to be passed to gap_advertise(adv_data=...).
    """
    payload = bytearray()

    def _append(adv_type, value):
        nonlocal payload
        payload += struct.pack("BB", len(value) + 1, adv_type) + value

    if manufacturer_data:
        _append(0xFF, manufacturer_data)  # AD type for Manufacturer Specific Data

    if services:
        for uuid in services:
            b = bytes(uuid)
            if len(b) == 2:
                _append(0x03, b)  # AD type for 16-bit UUID
            elif len(b) == 4:
                _append(0x05, b)  # AD type for 32-bit UUID
            elif len(b) == 16:
                _append(0x07, b)  # AD type for 128-bit UUID

    if name:
        _append(0x09, name.encode())  # AD type for complete local name

    if appearance:
        _append(0x19, struct.pack("<h", appearance))  # AD type for appearance

    return payload

def decode_field(payload, adv_type):
    """
    Decode fields from the payload.
    """
    i = 0
    result = []
    while i + 1 < len(payload):
        if payload[i + 1] == adv_type:
            result.append(payload[i + 2: i + payload[i] + 1])
        i += 1 + payload[i]
    return result

def decode_name(payload):
    """
    Decode name from the payload.
    """
    n = decode_field(payload, _ADV_TYPE_NAME)
    return str(n[0], "utf-8") if n else ""

def decode_services(payload):
    """
    Decode services from the payload.
    """
    services = []
    for u in decode_field(payload, _ADV_TYPE_UUID16_COMPLETE):
        services.append(bluetooth.UUID(struct.unpack("<h", u)[0]))
    for u in decode_field(payload, _ADV_TYPE_UUID32_COMPLETE):
        services.append(bluetooth.UUID(struct.unpack("<d", u)[0]))
    for u in decode_field(payload, _ADV_TYPE_UUID128_COMPLETE):
        services.append(bluetooth.UUID(u))
    return services

def demo():
    """
    Demo function to test advertising payload.
    """
    payload = advertising_payload(
        name="Pico",
        # services=[bluetooth.UUID(0x181A), bluetooth.UUID("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")],
    )
    print(payload)
    print(decode_name(payload))
    print(decode_services(payload))

if __name__ == "__main__":
    demo()

```

3. Save the files to your microcontroller and run the `ple_ble_mult_sensor.py` file.

4. You should see the following output:
```
MPY: soft reboot
Sensor name Pico 28:CD:C1:06:FC:42
Temperatures: [-3.16, 26.9, 27.87, 26.5, 27.3, 27.06, 27.14, 26.9]
Pressures: [1.91, 0, 1.11, 4.33, 0, 5.94, 2.72, 5.94]
--------------
Temperatures: [26.98, 27.3, 27.38, 27.3, 26.98, 26.82, 27.22, 27.22]
Pressures: [0, 0, 5.13, 1.11, 5.94, 2.72, 2.72, 5.94]
--------------
```

It will update every 15 seconds. To adjust the timing change `time.sleep(15)` to your desired time.