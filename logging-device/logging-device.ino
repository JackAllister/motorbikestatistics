/*
 * Prototype 1 for device side of the project.
 * 
 * Currently using:
 *  Arduino UNO
 *  Sparkfun GPS Logger Shield
 *  HC-06 BT receiver/transmitter
 *  MPU-6050 gyroscope
 *  
 *  MPU-6050 Pin Config:
 *    SDA = A4
 *    SCL = A5
 *    GND = GND
 *    VCC = 5V
 *  
 *  In future will need to port to Arduino 101
 *  this will remove the need for MPU-6050 and BT receiver
 */

/* Module Includes */
#include <Wire.h>
#include <SoftwareSerial.h>
#include <TinyGPS++.h>
#include <I2Cdev.h>
#include <MPU6050_6Axis_MotionApps20.h>

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

/* MPU6050 constants */
#define MPU_ADDR 0x68
#define MPU_INT_PIN 2

#define GYRO_X_OFFSET 206
#define GYRO_Y_OFFSET -31
#define GYRO_Z_OFFSET -23
#define ACCL_X_OFFSET -2495
#define ACCL_Y_OFFSET -1075
#define ACCL_Z_OFFSET 1515

/* Module Variables */
SoftwareSerial serGPS(GPS_RX_PIN, GPS_TX_PIN);
TinyGPSPlus gps;

MPU6050 mpu(MPU_ADDR);

/* Module Prototypes */
String getGPSInfo();
String parseDateTime();

/* Module Code */

/* Interrupt Code */
volatile bool mpuInterrupt = false;
void dmpDataReady()
{
  mpuInterrupt = true;
}

/* System setup code */
void setup() 
{
  /* Set up GPS */
  serGPS.begin(GPS_BAUD);

  /* Set up MPU6050 */
  Wire.begin();
  Wire.setClock(400000);
  pinMode(MPU_INT_PIN, INPUT);

  /* Load and configure the DMP within MPU6050 chip */
  mpu.dmpInitialize();

  /* Calibration offsets for gyro/accel */
  mpu.setXGyroOffset(GYRO_X_OFFSET);
  mpu.setYGyroOffset(GYRO_Y_OFFSET);
  mpu.setZGyroOffset(GYRO_Z_OFFSET);
  mpu.setXAccelOffset(ACCL_X_OFFSET);
  mpu.setYAccelOffset(ACCL_Y_OFFSET);
  mpu.setZAccelOffset(ACCL_Z_OFFSET);
  
  
  Serial.begin(SERIAL_BAUD);
}

/* Main Code */
void loop() 
{
  static unsigned long lastMillis = 0;
  
  while (serGPS.available() > 0)
    gps.encode(serGPS.read());

  if (millis() - lastMillis > 1000)
  {
    Serial.println(getGPSInfo().c_str());
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

