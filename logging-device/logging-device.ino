
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

/* ------ Module Includes ------ */
#include <SPI.h>
#include <SD.h>
#include <CurieIMU.h>
#include <MadgwickAHRS.h>
#include <SoftwareSerial.h>
#include <TinyGPS++.h>
#include <ArduinoJson.h>

/* ------ Module Constants ------ */

/* Constants for mode changing characters */
#define IDLE_CHAR '0'
#define REALTIME_CHAR '1'
#define LIST_SAVED_CHAR '2'
#define LOAD_TRIP_CHAR '3'

/* Settings for bluetooth serial */
#define BT_SERIAL Serial1
#define BT_BAUD 460800

/* Settings for sparkfun GPS logging shield (uSD version) */
#define GPS_TX_PIN 9
#define GPS_RX_PIN 8
#define GPS_BAUD 9600

/* Settings for uSD logging */
#define USD_CS 10
#define MAX_LOG_FILES 5000
#define MAX_STRING_SIZE 512
#define LOG_NAME "TRIP_"
#define LOG_EXTENSION "TXT"

/* Settings for inbuilt gyroscope and accelerometer */
#define IMU_FREQUENCY 25
#define ACCEL_RANGE 2
#define GYRO_RANGE 250

#define NUMBER_AXIS 3
#define AXIS_X 0
#define AXIS_Y 1
#define AXIS_Z 2

#define LED_PIN 13

/* ------ Module Typedefs ------ */
typedef enum
{
  IDLE,
  REALTIME,
} OPERATING_MODE;

/* ------ Module Variables ------ */
OPERATING_MODE systemMode = IDLE;

/* Objects required for system functionality */
SoftwareSerial serGPS(GPS_RX_PIN, GPS_TX_PIN);
TinyGPSPlus gps;

Madgwick IMUfilter;

/* JSON Objects used for parsing */
StaticJsonBuffer<500> jsonBuffer;
JsonObject& mainJSON = jsonBuffer.createObject();
JsonObject& orientJSON = mainJSON.createNestedObject("orientation");
JsonObject& gpsJSON = mainJSON.createNestedObject("gps");
JsonObject& timeJSON = mainJSON.createNestedObject("time");

char logFileName[30]; /* String to store file name in */

/* ------ Module Code ------ */

/* System setup code */
void setup()
{
  pinMode(LED_PIN, OUTPUT);

  /* Set up serial for data transmission */
  BT_SERIAL.begin(BT_BAUD);

  /* Set up uSD card, create log folder if doesn't exist */
  SD.begin(USD_CS);

  /* Set up GPS */
  serGPS.begin(GPS_BAUD);

  /* Set up Gyroscope + Accelerometer */
  CurieIMU.begin();
  CurieIMU.setGyroRate(IMU_FREQUENCY);
  CurieIMU.setAccelerometerRate(IMU_FREQUENCY);
  CurieIMU.setAccelerometerRange(ACCEL_RANGE);
  CurieIMU.setGyroRange(GYRO_RANGE);

  IMUfilter.begin(IMU_FREQUENCY);
}

/* Main Code */
void loop()
{

  /* Check if mode change character received from front-end */
  if (BT_SERIAL.available() > 0)
  {
    char modeChar = BT_SERIAL.read();

    OPERATING_MODE newMode;

    /* If valid new mode character found change system state */
    if (parseNewMode(modeChar, newMode) == true)
    {
      systemMode = newMode;
    }
  }

  /* State machine for choosing what option takes place */
  switch (systemMode)
  {
    case IDLE:
    {
      /*
       * In IDLE mode MCU does nothing.
       * System waits and still parses incoming commands.
       */
      break;
    }

    case REALTIME:
    {
      realTimeMode();
      break;
    }
  }
}

bool parseNewMode(char modeChar, OPERATING_MODE &newMode)
{
  bool result = true;

  switch (modeChar)
  {
    case IDLE_CHAR:
    {
      newMode = IDLE;
      break;
    }

    case REALTIME_CHAR:
    {
      /* Change mode and then generate new file name for new log */
      if (systemMode != REALTIME)
      {
        /* Generate new name if not already in this mode */
        generateFileName();
      }

      newMode = REALTIME;
      break;
    }

    case LIST_SAVED_CHAR:
    {
      /*
       * Load all trips and send to application.
       * Once we have finished sending trips we can go back to idle mode.
       */
      loadTripNames();
      newMode = IDLE;
      break;
    }

    case LOAD_TRIP_CHAR:
    {
      /* Load a specific trip by file name */
      loadSavedTrip();
      newMode = IDLE;
      break;
    }

    default:
    {
      /*
       * If not a valid operating mode character
       * then return that parsing failed.
       */
      result = false;
    }
  }

  return result;
}

void realTimeMode()
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

    /* Log JSON to the microSD */
    logToFile();

    /* Print to our bluetooth module */
    mainJSON.printTo(BT_SERIAL);
    BT_SERIAL.println();

    lastMillis = millis();
    digitalWrite(LED_PIN, LOW);
  }
}

void loadSavedTrip()
{
  bool nameComplete = false;
  String fileName = "";

  while (nameComplete == false)
  {
    /* Keep reading input in serial until file name is found */
    if (BT_SERIAL.available() > 0)
    {
      char recvByte = BT_SERIAL.read();
      fileName += recvByte;

      /* Wait until extension is found, then we know full file name */
      if (fileName.endsWith(LOG_EXTENSION))
      {
        nameComplete = true;
      }
    }
  }

  /* Check if file exists */
  if (SD.exists(fileName))
  {
    /* Open file, then read out data byte by byte */
    File fileHandle = SD.open(fileName);
    if (fileHandle)
    {

      while (fileHandle.available())
      {
        char readByte = fileHandle.read();

        BT_SERIAL.write(readByte);
      }
    }
  }
}

void loadTripNames()
{
  bool filesRemaining = true;

  File root = SD.open("/");

  /* Try to open directory for logs */
  if (root)
  {
    /* Ensure starting from start of directory */
    root.rewindDirectory();

    /* Create our JSON object that we will use for sending */
    JsonObject& fileJSON = jsonBuffer.createObject();

    while (filesRemaining == true)
    {
      /* Try open handle for next file */
      File entry = root.openNextFile();
      if (entry)
      {
        if (entry.isDirectory() == false)
        {
          /* Print out file name & size */
          fileJSON["name"] = entry.name();
          fileJSON["size"] = entry.size();

          fileJSON.printTo(BT_SERIAL);
          BT_SERIAL.println();
        }

        entry.close();
      }
      else
      {
        /* No more files remaining in directory */
        filesRemaining = false;
      }
    }

    root.close();
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

void logToFile()
{
  char jsonString[MAX_STRING_SIZE];

  /* Create handle to log file */
  File logHandle = SD.open(logFileName, FILE_WRITE);

  if (logHandle)
  {
    mainJSON.printTo(jsonString, MAX_STRING_SIZE);

    logHandle.println(jsonString);
    logHandle.close();
  }
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
