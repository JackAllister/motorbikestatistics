/*
 * Prototype 1 for device side of the project.
 * 
 * Currently using:
 *  Arduino UNO
 *  Sparkfun GPS Logger Shield
 *  HC-06 BT receiver/transmitter
 *  MPU-6050 gyroscope
 *  
 *  In future will need to port to Arduino 101
 *  this will remove the need for MPU-6050 and BT receiver
 */

/* Module Includes */
#include <SoftwareSerial.h>

/* Module Constants */
#define GPS_TX_PIN 9
#define GPS_RX_PIN 8
#define GPS_BAUD 9600

/* Module Variables */
SoftwareSerial gps(GPS_RX_PIN, GPS_TX_PIN);

/* Module Prototypes */

/* Module Code */
void setup() 
{
  gps.begin(GPS_BAUD);
  
  Serial.begin(9600);
}

void loop() 
{
  if (gps.available())
    Serial.write(gps.read());
}



