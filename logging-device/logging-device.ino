
/*
 * Prototype 2 for device side of the project.
 *
 * Currently using:
 *  Arduino 101
 *  Sparkfun GPS Logger Shield
 *  Onboard gyroscope + accelerometer
 *
 * Not using onboard bluetooth as it is only BLE. Limits to 20 bytes and data rate is low,
 * Better to use hardware serial and use HC-06 bluetooth board.
 *
 */

/* Module Includes */
#include <SPI.h>
#include <SD.h>
#include <CurieIMU.h>
#include <MadgwickAHRS.h>
#include <SoftwareSerial.h>
#include <TinyGPS++.h>
#include <ArduinoJson.h>

/* Module Constants */
#define SERIAL_TYPE Serial1
#define SERIAL_BAUD 9600

/* Settings for sparkfun GPS logging shield (uSD version) */
#define GPS_TX_PIN 9
#define GPS_RX_PIN 8
#define GPS_BAUD 9600

/* Settings for uSD logging */
#define USD_CS 10
#define MAX_LOG_FILES 5000
#define LOG_NAME "trip_"
#define LOG_EXTENSION "json"

/* Settings for inbuilt gyroscope and accelerometer */
#define IMU_FREQUENCY 25
#define ACCEL_RANGE 2
#define GYRO_RANGE 250

#define NUMBER_AXIS 3
#define AXIS_X 0
#define AXIS_Y 1
#define AXIS_Z 2

#define LED_PIN 13

/* Module Variables */
SoftwareSerial serGPS(GPS_RX_PIN, GPS_TX_PIN);
TinyGPSPlus gps;

Madgwick IMUfilter;

StaticJsonBuffer<500> jsonBuffer;
JsonObject& mainJSON = jsonBuffer.createObject();
JsonObject& orientJSON = mainJSON.createNestedObject("orientation");
JsonObject& gpsJSON = mainJSON.createNestedObject("gps");
JsonObject& timeJSON = mainJSON.createNestedObject("time");

char logFileName[30]; /* String to store file name in */

/* Module Code */

/* System setup code */
void setup()
{
  pinMode(LED_PIN, OUTPUT);

  /* Set up serial for data transmission */
  SERIAL_TYPE.begin(SERIAL_BAUD);

  /* Set up uSD card */
  SD.begin(USD_CS);
  generateFileName();

  /* Set up GPS */
  serGPS.begin(GPS_BAUD);

  /* Set up Gyroscope + Accelerometer */
  CurieIMU.begin();
  CurieIMU.setGyroRate(IMU_FREQUENCY);
  CurieIMU.setAccelerometerRate(IMU_FREQUENCY);
  IMUfilter.begin(IMU_FREQUENCY);

  CurieIMU.setAccelerometerRange(ACCEL_RANGE);
  CurieIMU.setGyroRange(GYRO_RANGE);
}

/* Main Code */
void loop()
{
  static const unsigned long PRINT_DELAY = 1000;
  static unsigned long lastMillis = 0;

  getOrientation();

  /* Parse NMEA codes into GPS object */
  while (serGPS.available() > 0)
  {
    gps.encode(serGPS.read());
  }

  /* Print orientation and location information */
  if ((millis() - lastMillis) > PRINT_DELAY)
  {
    digitalWrite(LED_PIN, HIGH);

    addOrientationToJSON();
    addGPSToJSON();
    addTimeToJSON();

    mainJSON.printTo(SERIAL_TYPE);
    SERIAL_TYPE.println();

    lastMillis = millis();
    digitalWrite(LED_PIN, LOW);
  }
}

void getOrientation()
{
  static const unsigned long US_PER_READING = 1000000 / IMU_FREQUENCY;
  static unsigned long usPrevious = micros();

  int accel_raw[NUMBER_AXIS];
  int gyro_raw[NUMBER_AXIS];
  float accel_g[NUMBER_AXIS];
  float gyro_ds[NUMBER_AXIS];
  unsigned long usNow;

  /* Ensures we stick to the sample rate */
  usNow = micros();
  if (usNow - usPrevious >= US_PER_READING)
  {
    /* Read raw data from the IMU */
    CurieIMU.readMotionSensor(accel_raw[AXIS_X], accel_raw[AXIS_Y], accel_raw[AXIS_Z],
                              gyro_raw[AXIS_X], gyro_raw[AXIS_Y], gyro_raw[AXIS_Z]);

    accel_g[AXIS_X] = convertRawAcceleration(accel_raw[AXIS_X]);
    accel_g[AXIS_Y] = convertRawAcceleration(accel_raw[AXIS_Y]);
    accel_g[AXIS_Z] = convertRawAcceleration(accel_raw[AXIS_Z]);
    gyro_ds[AXIS_X] = convertRawGyro(gyro_raw[AXIS_X]);
    gyro_ds[AXIS_Y] = convertRawGyro(gyro_raw[AXIS_Y]);
    gyro_ds[AXIS_Z] = convertRawGyro(gyro_raw[AXIS_Z]);

    /* Update the filter. Orientation is calculated here */
    IMUfilter.updateIMU(gyro_ds[AXIS_X], gyro_ds[AXIS_Y], gyro_ds[AXIS_Z],
                        accel_g[AXIS_X], accel_g[AXIS_Y], accel_g[AXIS_Z]);

    /* Increment previous counter */
    usPrevious += US_PER_READING;
  }
}

float convertRawAcceleration(int aRaw) {
  // since we are using 2G range
  // -2g maps to a raw value of -32768
  // +2g maps to a raw value of 32767

  float a = (aRaw * (float)ACCEL_RANGE) / 32768.0;
  return a;
}

float convertRawGyro(int gRaw) {
  // since we are using 250 degrees/seconds range
  // -250 maps to a raw value of -32768
  // +250 maps to a raw value of 32767

  float g = (gRaw * (float)GYRO_RANGE) / 32768.0;
  return g;
}

void addOrientationToJSON()
{
  orientJSON["yaw"] = IMUfilter.getYaw();
  orientJSON["pitch"] = IMUfilter.getPitch();
  orientJSON["roll"] = IMUfilter.getRoll();
}

void addGPSToJSON()
{
  /* Add location information */
  gpsJSON["gps_valid"] = gps.location.isUpdated();
  gpsJSON["lat"] = double_with_n_digits(gps.location.lat(), 6);
  gpsJSON["lng"] = double_with_n_digits(gps.location.lng(), 6);

  /* Other crucial GPS information */
  gpsJSON["available"] = gps.satellites.value();
  gpsJSON["vel_mph"] = gps.speed.mph();
  gpsJSON["alt_ft"] = gps.altitude.feet();
}

void addTimeToJSON()
{
  /* Add time information to JSON */
  timeJSON["time_valid"] = gps.date.isValid() && gps.time.isValid();
  timeJSON["day"] = gps.date.day();
  timeJSON["month"] = gps.date.month();
  timeJSON["year"] = gps.date.year();

  timeJSON["hour"] = gps.time.hour();
  timeJSON["minute"] = gps.time.minute();
  timeJSON["second"] = gps.time.second();
  timeJSON["centiseconds"] = gps.time.centisecond();
}

bool generateFileName()
{
  bool result = false;
  int i = 0;

  for (i = 0; i < MAX_LOG_FILES; i++)
  {
    /* Clear name of log file */
    memset(logFileName, 0, strlen(logFileName));

    /* Set the new log file name to: trip_XXXXX.json */
    sprintf(logFileName, "%s%d.%s", LOG_NAME, i, LOG_EXTENSION);

    if (!SD.exists(logFileName))
    {
      /* If a file doesn't exist */
      result = true;
      break;
    }
  }

  return result;
}
