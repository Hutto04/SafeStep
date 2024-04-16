
// the value of the 'other' resistor
#define SERIESRESISTOR 10000    
 
// What pin to connect the sensor to
#define THERMISTORPIN A1
 
 
int fsrPin = 0;     // the FSR and 10K pulldown are connected to a0
int fsrReading;     // the analog reading from the FSR resistor divider
int fsrVoltage;     // the analog reading converted to voltage
unsigned long fsrResistance;  // The voltage converted to resistance, can be very big so make "long"
unsigned long fsrConductance; 
long fsrForce;       // Finally, the resistance converted to force
 
void setup(void) {
  Serial.begin(9600);   // We'll send debugging information via the Serial monitor
}
 
void loop(void) {
  //temp reading
  float reading;
 
  reading = analogRead(THERMISTORPIN);
 
 
  // convert the value to resistance
  reading = (1023 / reading)  - 1;     // (1023/ADC - 1) 
  reading = SERIESRESISTOR / reading;  // 10K / (1023/ADC - 1)
  Serial.print("Temp resistance = "); 
  Serial.println(reading);
 
  delay(1000);


  //force reading
  fsrReading = analogRead(fsrPin);  
  Serial.print("Force reading = ");
  Serial.println(fsrReading);
  Serial.println("--------------------");
  delay(1000);
}
