# This example demonstrates a simple temperature sensor peripheral.
#
# The sensor's local value is updated, and it will notify
# any connected central every 10 seconds.

import bluetooth
import random
import struct
import time
import machine
import ubinascii
from ble_advertising import advertising_payload
from micropython import const
from machine import Pin

_IRQ_CENTRAL_CONNECT = const(1)
_IRQ_CENTRAL_DISCONNECT = const(2)
_IRQ_GATTS_INDICATE_DONE = const(20)

_FLAG_READ = const(0x0002)
_FLAG_NOTIFY = const(0x0010)
_FLAG_INDICATE = const(0x0020)

# org.bluetooth.service.environmental_sensing
#_ENV_SENSE_UUID = bluetooth.UUID(0x181A)

_ENV_SENSE_UUID = bluetooth.UUID("00001810-0000-1000-8000-00805f9b34fb")

# org.bluetooth.characteristic.temperature
_TEMP_CHAR = (
    bluetooth.UUID(0x2A6E),
    _FLAG_READ | _FLAG_NOTIFY | _FLAG_INDICATE,
)

# org.bluetooth.characteristic.pressure
_PRESSURE_CHAR = (
    bluetooth.UUID("0000210f-0000-1000-8000-00805f9b34fb"),
    _FLAG_READ | _FLAG_NOTIFY | _FLAG_INDICATE,
)

_ENV_SENSE_SERVICE = (
    _ENV_SENSE_UUID,
    (_TEMP_CHAR, _PRESSURE_CHAR),
)

# org.bluetooth.characteristic.gap.appearance.xml
_ADV_APPEARANCE_GENERIC_THERMOMETER = const(768)

class BLESensor:
    def __init__(self, ble, name=""):
        self._sensor_temp = machine.ADC(4)
        self._sensor_pressure = machine.ADC(machine.Pin(26))
        self._ble = ble
        self._ble.active(True)
        self._ble.irq(self._irq)
        ((self._temp_handle, self._pressure_handle),) = self._ble.gatts_register_services((_ENV_SENSE_SERVICE,))
        self._connections = set()
        if len(name) == 0:
            name = 'Pico %s' % ubinascii.hexlify(self._ble.config('mac')[1],':').decode().upper()
        print('Sensor name %s' % name)
        self._payload = advertising_payload(
            name=name, services=[_ENV_SENSE_UUID]
        )
        self._advertise()

    def _irq(self, event, data):
        if event == _IRQ_CENTRAL_CONNECT:
            conn_handle, _, _ = data
            self._connections.add(conn_handle)
        elif event == _IRQ_CENTRAL_DISCONNECT:
            conn_handle, _, _ = data
            self._connections.remove(conn_handle)
            self._advertise()
        elif event == _IRQ_GATTS_INDICATE_DONE:
            conn_handle, value_handle, status = data

    def update_temperature(self, notify=False, indicate=False):
        temp_deg_c = self._get_temp()
        print("Temperature: %.2f°C" % temp_deg_c)
        self._ble.gatts_write(self._temp_handle, struct.pack("<h", int(temp_deg_c * 100)))

        if notify or indicate:
            for conn_handle in self._connections:
                if notify:
                    self._ble.gatts_notify(conn_handle, self._temp_handle)
                if indicate:
                    self._ble.gatts_indicate(conn_handle, self._temp_handle)

    def update_pressure(self, notify=False, indicate=False):
        pressure_pascal = self._get_pressure()
        print("Pressure: %.2f Pa" % pressure_pascal)
        self._ble.gatts_write(self._pressure_handle, struct.pack("<i", int(pressure_pascal)))

        if notify or indicate:
            for conn_handle in self._connections:
                if notify:
                    self._ble.gatts_notify(conn_handle, self._pressure_handle)
                if indicate:
                    self._ble.gatts_indicate(conn_handle, self._pressure_handle)

    def _advertise(self, interval_us=500000):
        self._ble.gap_advertise(interval_us, adv_data=self._payload)

    def _get_temp(self):
        conversion_factor = 3.3 / 65535
        reading = self._sensor_temp.read_u16() * conversion_factor
        return 27 - (reading - 0.706) / 0.001721

    def _get_pressure(self):
        conversion_factor = 3.3 / 65535
        reading = self._sensor_pressure.read_u16() * conversion_factor
        return reading * 1000  # Convert to Pascals?

def demo():
    ble = bluetooth.BLE()
    env_sensor = BLESensor(ble)
    counter = 0
    led = Pin('LED', Pin.OUT)
    while True:
        if counter % 10 == 0:
            env_sensor.update_temperature(notify=True, indicate=False)
            env_sensor.update_pressure(notify=True, indicate=False)
        led.toggle()
        time.sleep_ms(1000)
        counter += 1

if __name__ == "__main__":
    demo()