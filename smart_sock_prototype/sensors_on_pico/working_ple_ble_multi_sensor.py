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
        for i in range(8):  # Assuming 8 temperature sensors followed by 8 pressure sensors
            temp_reading = read_mux(i)
            pressure_reading = read_mux(i + 8)
            
            # Example conversion?
            converted_temp = (temp_reading * 3.3 / 65535 - 0.5) * 100  # Example for a typical temp sensor conversion
            converted_pressure = pressure_reading * 3.3 / 65535 * 1000  # Example conversion, adjust accordingly
            
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
        time.sleep(10)

if __name__ == "__main__":
    demo()
