
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
#include <SoftwareSerial.h>
#include <TinyGPS++.h>
#include <ArduinoJson.h>

#include "Orientation.h"
#include "Storage.h"

/* ------ Module Constants ------ */

/* Constants for mode changing characters */
#define IDLE_CHAR '0'
#define REALTIME_CHAR '1'
#define LIST_SAVED_CHAR '2'
#define LOAD_TRIP_CHAR '3'

/* Settings for bluetooth serial */
#define BT_SERIAL Serial1
#define BT_BAUD 115200

/* Settings for sparkfun GPS logging shield (uSD version) */
#define GPS_TX_PIN 9
#define GPS_RX_PIN 8
#define GPS_BAUD 9600

#define LED_PIN 13

/* ------ Module Typedefs ------ */
typedef enum
{
  IDLE,
  REALTIME,
} OPERATING_MODE;

/* ------ Module Variables ------ */

/* State machine variable */
OPERATING_MODE systemMode = IDLE;

/* Objects required for system functionality */
Orientation orientation;
Storage storage;

SoftwareSerial serGPS(GPS_RX_PIN, GPS_TX_PIN);
TinyGPSPlus gps;

/* JSON Objects used for parsing */
StaticJsonBuffer<500> jsonBuffer;
JsonObject& mainJSON = jsonBuffer.createObject();
JsonObject& orientJSON = mainJSON.createNestedObject("orientation");
JsonObject& gpsJSON = mainJSON.createNestedObject("gps");
JsonObject& timeJSON = mainJSON.createNestedObject("time");

/* ------ Module Code ------ */

/* System setup code */
void setup()
{
  pinMode(LED_PIN, OUTPUT);

  /* Initialise our storage module */
  storage.init();

  /* Set up serial for data transmission */
  BT_SERIAL.begin(BT_BAUD);

  /* Set up GPS */
  serGPS.begin(GPS_BAUD);
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
        storage.generateFileName();
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
      storage.loadTripNames();
      newMode = IDLE;
      break;
    }

    case LOAD_TRIP_CHAR:
    {
      /* Load a specific trip by file name */
      storage.loadSavedTrip();
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
  static const unsigned int MAX_STRING_SIZE = 512;
  static const unsigned long PRINT_DELAY = 1000;
  static unsigned long lastMillis = 0;
  char jsonString[MAX_STRING_SIZE];

  /* Poll our IMU to update XYZ */
  orientation.pollIMU();

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

    /* Print our json object into a string */
    mainJSON.printTo(jsonString, MAX_STRING_SIZE);

    /* Log JSON to the microSD */
    storage.saveToFile(jsonString, true);

    /* Print to our bluetooth module */
    BT_SERIAL.println(jsonString);

    lastMillis = millis();
    digitalWrite(LED_PIN, LOW);
  }
}

void addOrientationToJSON()
{
  orientJSON["yaw"] = orientation.getYaw();
  orientJSON["pitch"] = orientation.getPitch();
  orientJSON["roll"] = orientation.getRoll();
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
