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
#include <CurieIMU.h>
#include <MadgwickAHRS.h>
#include <SoftwareSerial.h>
#include <TinyGPS++.h>

/* Module Constants */
#define SERIAL_BAUD 115200

/* Settings for EM-506 GPS shield (borrowed from Peter) */
//#define GPS_TX_PIN 3
//#define GPS_RX_PIN 2
//#define GPS_BAUD 4800

/* Settings for sparkfun GPS logging shield (uSD version) */
#define GPS_TX_PIN 9
#define GPS_RX_PIN 8
#define GPS_BAUD 9600

/* Settings for inbuilt gyroscope and accelerometer */
#define IMU_FREQUENCY 25
#define ACCEL_RANGE 2
#define GYRO_RANGE 250

#define NUMBER_AXIS 3
#define AXIS_X 0
#define AXIS_Y 1
#define AXIS_Z 2

/* Module Variables */
SoftwareSerial serGPS(GPS_RX_PIN, GPS_TX_PIN);
TinyGPSPlus gps;

Madgwick IMUfilter;
float accelScale, gyroScale;


/* Module Code */

/* System setup code */
void setup() 
{  
  /* Set up serial for debugging */
  Serial.begin(SERIAL_BAUD);
  while (!Serial)
  {
    
  }
  
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
  static const unsigned long PRINT_DELAY = 2000;
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
    Serial.print("Yaw: ");
    Serial.print(IMUfilter.getYaw());
    Serial.print("\tPitch: ");
    Serial.print(IMUfilter.getPitch());
    Serial.print("\tRoll: ");
    Serial.print(IMUfilter.getRoll());
    Serial.println();

    /* Print out GPS information */
    Serial.println(getGPSInfo());
    lastMillis = millis();
  }
}

void getOrientation()
{
  static const unsigned long MS_PER_READING = 1000000 / IMU_FREQUENCY;
  static unsigned long msPrevious = micros();
  
  int accel_raw[NUMBER_AXIS];
  int gyro_raw[NUMBER_AXIS];
  float accel_g[NUMBER_AXIS];
  float gyro_ds[NUMBER_AXIS];
  unsigned long msNow;

  /* Ensures we stick to the sample rate */
  msNow = micros();
  if (msNow - msPrevious >= MS_PER_READING)
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
    msPrevious += msNow;
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

