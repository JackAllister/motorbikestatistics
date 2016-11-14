/*
 * Basic prototype for the sparkfun GPS shield which uses
 * the EM-506 gps receiver.
 * 
 * Basic prototype just prints out the GPS coordinates over serial.
 * 
 * Reference:
 * https://learn.sparkfun.com/tutorials/gps-shield-hookup-guide
 */
#include <TinyGPS++.h>
#include <SoftwareSerial.h>

/* UART pins for GPS to Arduino */
static const int RXPin = 2;
static const int TXPin = 3;

/* Baud rates needed for communication on the device */
static const int ArduinoBaud = 9600;
static const int GPSBaud = 4800;

TinyGPSPlus gps;

SoftwareSerial gpsSerial(RXPin, TXPin);

void setup() {
  /* Start serial from Arduino to PC */
  Serial.begin(ArduinoBaud);

  /* Start serial from GPS to Arduino */
  gpsSerial.begin(GPSBaud);
}

void loop()
{
  // This sketch displays information every time a new sentence is correctly encoded.
  while (gpsSerial.available() > 0)
    if (gps.encode(gpsSerial.read()))
      displayInfo();

  // If 5000 milliseconds pass and there are no characters coming in
  // over the software serial port, show a "No GPS detected" error
  if (millis() > 5000 && gps.charsProcessed() < 10)
  {
    Serial.println(F("No GPS detected"));
    while(true);
  }
}

void displayInfo()
{
  Serial.print(F("GPS Available: "));
  Serial.print(gps.satellites.value());
  Serial.print(" ");
  
  Serial.print(F("Location: ")); 
  if (gps.location.isValid())
  {
    Serial.print(gps.location.lat(), 6);
    Serial.print(F(","));
    Serial.print(gps.location.lng(), 6);
  }
  else
  {
    Serial.print(F("INVALID"));
  }

  Serial.print(F("  Date/Time: "));
  if (gps.date.isValid())
  {
    Serial.print(gps.date.month());
    Serial.print(F("/"));
    Serial.print(gps.date.day());
    Serial.print(F("/"));
    Serial.print(gps.date.year());
  }
  else
  {
    Serial.print(F("INVALID"));
  }

  Serial.print(F(" "));
  if (gps.time.isValid())
  {
    if (gps.time.hour() < 10) Serial.print(F("0"));
    Serial.print(gps.time.hour());
    Serial.print(F(":"));
    if (gps.time.minute() < 10) Serial.print(F("0"));
    Serial.print(gps.time.minute());
    Serial.print(F(":"));
    if (gps.time.second() < 10) Serial.print(F("0"));
    Serial.print(gps.time.second());
    Serial.print(F("."));
    if (gps.time.centisecond() < 10) Serial.print(F("0"));
    Serial.print(gps.time.centisecond());
  }
  else
  {
    Serial.print(F("INVALID"));
  }

  Serial.println();
}

