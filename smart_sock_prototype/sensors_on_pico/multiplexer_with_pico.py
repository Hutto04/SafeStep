from machine import Pin, ADC
from machine import Pin, PWM
import time


#For the code example I just took the binary table, made it into a simple array, and put it in a function that does this: You give the function a number 0-15. It looks up that number in the binary array, then it loops through those 4 numbers and sets S0, S1, S2, and S3 appropriately. (In the arduino software HIGH is the same as1 & LOW is the same as 0). After it sets the pins so that SIG is connected to the correct channel, it then reads analog 0 (where SIG is connected to) and returns that value. So all you need to do is something like this

#switch mux to channel 15 and read the value
#  int val = readMux(15);

s0 = Pin(0, Pin.OUT)
s1 = Pin(1, Pin.OUT)
s2 = Pin(2, Pin.OUT)
s3 = Pin(3, Pin.OUT)


#pressure variables
#fsr_reading = 0
fsr_voltage = 0 
fsr_conductance = 0  
# Mux in "SIG" pin
SIG_pin = ADC(0)  # Replace 0 with the actual ADC pin number

def setup():
    s0.value(0)
    s1.value(0)
    s2.value(0)
    s3.value(0)

def loop():
    # Loop through and read all 16 values
    # Reports back Value at channel 6 is: 346
    for i in range(16):
        fsr_reading = read_mux(i)
        fsr_reading = raw_pressure_to_newtons(fsr_reading)
        print("sensor ", i, " : ", fsr_reading)
        #time.sleep(1)  # Delay for 1 second
    time.sleep(.5)

def read_mux(channel):
    control_pin = [s0, s1, s2, s3]
    mux_channel = [
        [0, 0, 0, 0],  # channel 0
        [1, 0, 0, 0],  # channel 1
        [0, 1, 0, 0],  # channel 2
        [1, 1, 0, 0],  # channel 3
        [0, 0, 1, 0],  # channel 4
        [1, 0, 1, 0],  # channel 5
        [0, 1, 1, 0],  # channel 6
        [1, 1, 1, 0],  # channel 7
        [0, 0, 0, 1],  # channel 8
        [1, 0, 0, 1],  # channel 9
        [0, 1, 0, 1],  # channel 10
        [1, 1, 0, 1],  # channel 11
        [0, 0, 1, 1],  # channel 12
        [1, 0, 1, 1],  # channel 13
        [0, 1, 1, 1],  # channel 14
        [1, 1, 1, 1]   # channel 15
    ]

    # Set the control pins for the selected channel
    for i in range(4):
        control_pin[i].value(mux_channel[channel][i])

    # Read the value from the SIG pin
    val = SIG_pin.read_u16()

    return val

def map(val, loval, hival, tolow, tohigh):
     if loval <= val <= hival:
         return (val - loval)/(hival-loval)*(tohigh-tolow) + tolow
     else:
         raise(ValueError)

def raw_pressure_to_newtons(raw_pressure):
    # voltage is 3.3v or 3300mV
    fsr_voltage = map(raw_pressure, 0, 65535, 0 , 3300)

    # The voltage = Vcc * R / (R + FSR) where R = 10K and Vcc = 5V
    # so FSR = ((Vcc - V) * R) / V

    fsr_resistance = 3300 - fsr_voltage

    #10k resistor in microMhos = 1000000
    fsr_conductance = 1000000/fsr_resistance

    if (fsr_voltage <= 1000):
        return 0
    else:
        return fsr_voltage




##### TO DO #########
# need to assign which channels are pressure sensors and temp sensors
# need to convert raw pressure to newton units and raw temp to F*



def main():
    setup()

    while True:
        loop()

main()

