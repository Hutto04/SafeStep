import bluetooth
import random
import struct
import time
import machine
import ubinascii
from ble_advertising import advertising_payload
from micropython import const
from machine import Pin

# UUIDs for the environmental sensing service and temperature characteristic
#_ENV_SENSE_UUID = bluetooth.UUID(0x181A)
#_TEMP_CHAR_UUID = bluetooth.UUID(0x2A6E)
#_PRESSURE_CHAR_UUID = bluetooth.UUID(0x2A6D)


_ENV_SENSE_UUID = bluetooth.UUID("00001810-0000-1000-8000-00805f9b34fb")
_TEMP_CHAR_UUID = bluetooth.UUID("00002a6e-0000-1000-8000-00805f9b34fb")
_PRESSURE_CHAR_UUID = bluetooth.UUID("0000210f-0000-1000-8000-00805f9b34fb")

# Definition of the environmental sensing service
_ENV_SENSE_SERVICE = (
    _ENV_SENSE_UUID,
    (
        (_TEMP_CHAR_UUID, bluetooth.FLAG_READ | bluetooth.FLAG_NOTIFY),
        (_PRESSURE_CHAR_UUID, bluetooth.FLAG_READ | bluetooth.FLAG_NOTIFY),
    )
)

class BLESensors:
    def __init__(self, ble, name=""):
        self._sensor_temp = machine.ADC(4) # ADC for temperature reading on pin ADC 4
        self._sensor_pressure = machine.ADC(machine.Pin(26))  # Pressure sensor on ADC 0 (GPIO 26)
        self._ble = ble
        self._ble.active(True)
        self._ble.irq(self._irq)
        ((self._temp_handle, self._pressure_handle),) = self._ble.gatts_register_services((_ENV_SENSE_SERVICE,))
        self._connections = set()

        # set device name
        if len(name) == 0:
            name = 'Pico-%s' % ubinascii.hexlify(self._ble.config('mac')[1], ':').decode().upper()
        self._name = name
        self._advertise()

    def _irq(self, event, data):
        # Handle BLE events
        if event == 1:  # Central connected
            conn_handle, _, _ = data
            self._connections.add(conn_handle)
        elif event == 2:  # Central disconnected
            conn_handle, _, _ = data
            if conn_handle in self._connections:
                self._connections.remove(conn_handle)
        elif event == 11:  # Characteristic write request
            conn_handle, value_handle = data
            if conn_handle in self._connections and value_handle == self._handle:
                self.update_temperature()

    def update_temperature(self, notify=False):
        temp_deg_c = self._get_temp()
        print("Temperature: %.2f degC" % temp_deg_c)
        self._ble.gatts_write(self._temp_handle, struct.pack("<h", int(temp_deg_c * 100)))
        if notify:
            for conn_handle in self._connections:
                self._ble.gatts_notify(conn_handle, self._temp_handle)
        # Optionally notify connected clients
        if notify:
            for conn_handle in self._connections:
                self._ble.gatts_notify(conn_handle, self._temp_handle)
                
    def update_pressure(self, notify=False):
        pressure_value = self._get_pressure()
        print("Pressure: %d" % pressure_value)
        self._ble.gatts_write(self._pressure_handle, struct.pack("<h", pressure_value))
        if notify:
            for conn_handle in self._connections:
                self._ble.gatts_notify(conn_handle, self._pressure_handle)

    def _advertise(self, interval_us=500000):
        # start advertising with specified name and service
        self._ble.gap_advertise(interval_us, adv_data=advertising_payload(name=self._name, services=[_ENV_SENSE_UUID]))

    def _get_temp(self):
        # Convert ADC reading to temperature based on a calibrated formula
        conversion_factor = 3.3 / 65535
        reading = self._sensor_temp.read_u16() * conversion_factor
        return 27 - (reading - 0.706) / 0.001721
    
    def _get_pressure(self):
        conversion_factor = 3.3 / 65535
        reading = self._sensor_pressure.read_u16() * conversion_factor
        return int(reading * 100)  # Example conversion


def demo():
    ble = bluetooth.BLE()
    sensors = BLESensors(ble)
    led = Pin('LED', Pin.OUT)
    
    # Continuously update temperature and pressure
    while True:
        time.sleep_ms(1000)
        sensors.update_temperature(notify=True)
        sensors.update_pressure(notify=True)
        led.toggle()

if __name__ == "__main__":
    demo()