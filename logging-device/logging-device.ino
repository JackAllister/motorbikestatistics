/*
 * Prototype 2 for device side of the project.
 * 
 * Currently using:
 *  Arduino 101
 *  Sparkfun GPS Logger Shield
 *  Onboard gyroscope + accelerometer
 *  Onboard BLE for data transmission
 *  
 */

/* Module Includes */
#include <SoftwareSerial.h>
#include <TinyGPS++.h>

/* Module Constants */
#define SERIAL_BAUD 9600

/* Settings for EM-506 GPS shield (borrowed from Peter) */
//#define GPS_TX_PIN 3
//#define GPS_RX_PIN 2
//#define GPS_BAUD 4800

/* Settings for sparkfun GPS logging shield (uSD version) */
#define GPS_TX_PIN 9
#define GPS_RX_PIN 8
#define GPS_BAUD 9600

/* Module Variables */
SoftwareSerial serGPS(GPS_RX_PIN, GPS_TX_PIN);
TinyGPSPlus gps;


/* Module Code */

/* System setup code */
void setup() 
{
  /* Set up serial for debugging */
  Serial.begin(SERIAL_BAUD);
  
  /* Set up GPS */
  serGPS.begin(GPS_BAUD);

  /* Set up Gyroscope + Accelerometer */


}

/* Main Code */
void loop() 
{
  static const unsigned long PRINT_DELAY = 2000;
  static unsigned long lastMillis = 0;

  bool isRead = false;
  /* Parse NMEA codes into GPS object */
  while (serGPS.available() > 0)
  {
    unsigned char tmpData = serGPS.read();
    Serial.print(tmpData);
    gps.encode(tmpData);
    isRead = true;
  }
  if (isRead == true)
  {
    Serial.println();
  }
  


  /* Print orientation and location information */
  if ((millis() - lastMillis) > PRINT_DELAY)
  {
//    Serial.print("Yaw: ");
//    Serial.print(yprAngle[0]);
//    Serial.print("\tPitch: ");
//    Serial.print(yprAngle[1]);
//    Serial.print("\tRoll: ");
//    Serial.print(yprAngle[2]);
//    Serial.println();

    /* Print out GPS information */
    //Serial.println(getGPSInfo());
    lastMillis = millis();
  }
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
    result += String(gps.location.lat(), 6);
    result += ',';
    result += String(gps.location.lng(), 6);
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
    if (gps.time.hour() < 10)
      result += '0';
    result += gps.time.hour();
    result += ":";
    if (gps.time.minute() < 10)
      result += '0';
    result += gps.time.minute();
    result += ":";
    if (gps.time.second() < 10)
      result += '0';
    result += gps.time.second();
  }

  return result;
}

