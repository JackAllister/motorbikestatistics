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
#include <TinyGPS++.h>

/* Module Constants */
#define GPS_TX_PIN 9
#define GPS_RX_PIN 8
#define GPS_BAUD 9600

/* Module Variables */
SoftwareSerial serGPS(GPS_RX_PIN, GPS_TX_PIN);
TinyGPSPlus gps;

/* Module Prototypes */
String getGPSInfo();
String parseDateTime();

/* Module Code */
void setup() 
{
  serGPS.begin(GPS_BAUD);
  
  Serial.begin(9600);
}

void loop() 
{
  while (serGPS.available() > 0)
    gps.encode(serGPS.read());
    
  Serial.println(getGPSInfo().c_str());
}

String getGPSInfo()
{
  static const char* INVALID_STR = "INVALID";

  /* Get GPS available */
  String result = "GPS Available: ";
  result += gps.satellites.value();
  result += ' ';

  /* Get location */
  result += "Location: ";
  if (gps.location.isValid())
  {
    result += gps.location.lat();
    result += ',';
    result += gps.location.lng();
  }
  else
    result += INVALID_STR;
  result += ' ';

  /* Get speed */
  result += "Velocity: ";
  result += gps.speed.mph();
  result += "mph";
  result += ' ';

  /* Get altitude */
  result += "Altitude: ";
  result += gps.altitude.feet();
  result += "ft";
  result += ' ';

  /* Get Date/Time */
  result += parseDateTime();

  return result;
}

String parseDateTime()
{
  String result = "Date: INVALID";

  if (gps.date.isValid() && gps.time.isValid())
  {
    /* Add date to string */
    result = "Date: ";
    result += gps.date.day();
    result += "/";
    result += gps.date.month();
    result += "/";
    result += gps.date.year();

    /* Add time to string */
    result += " ";
    result += gps.time.hour();
    result += ":";
    result += gps.time.minute();
    result += ":";
    result += gps.time.second();
  }

  return result;
}

