from machine import Pin, ADC
from time import sleep

# connected to GPIO 26 ( analog ADC0 )
temp_pin = 26

temp_sensor = ADC(Pin(temp_pin))

while True: 
    temp_value = temp_sensor.read_u16() # reads value, 0 - 65535 across voltage range 0.0v - 3.3v
    print(temp_value)
    sleep(0.1)





