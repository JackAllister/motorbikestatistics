/**
 * @file logging-device.ino
 * @author Jack Allister - 23042098
 * @date 2016-2017
 * @brief Arduino sketch for the logging device.
 *
 *  - Arduino 101
 *  - Sparkfun GPS Logger shield
 *  - Onboard gyroscope + accelerometer
 *  - HC-06 Serial Bluetooth Module
 */

/* ------ Module Includes ------ */
#include <SoftwareSerial.h>
#include <TinyGPS++.h>
#include <ArduinoJson.h>

#include "Orientation.h"
#include "Storage.h"

/* ------ Module Constants ------ */

/** @brief Command to set system to idle mode */
#define IDLE_CHAR '0'
/** @brief Command to set system to realtime logging mode */
#define REALTIME_CHAR '1'
/** @brief Command to list all saved trip names from uSD */
#define LIST_SAVED_CHAR '2'
/** @brief Command to load a trip stored on uSD */
#define LOAD_TRIP_CHAR '3'

/** @brief Mapping for which HW-Serial port BT module is on */
#define BT_SERIAL Serial1
/** @brief BAUD rate of BT device */
#define BT_BAUD 115200

/* Settings for sparkfun GPS logging shield (uSD version) */
/** @brief GPS serial transmit pin */
#define GPS_TX_PIN 9
/** @brief GPS serial receive pin */
#define GPS_RX_PIN 8
/** @brief GPS serial baud rate */
#define GPS_BAUD 9600

/** @brief LED pin to indicate read */
#define LED_PIN 13

/* ------ Module Typedefs ------ */

/** @brief Typedef holding two possible states for device. */
typedef enum
{
  IDLE,
  REALTIME,
} OPERATING_MODE;

/* ------ Module Variables ------ */

/** @brief State machine for system state of device. */
OPERATING_MODE systemMode = IDLE;

/** @brief Orientation object, used for receiving device orientation. */
Orientation orientation;

/** @brief Storage object, responsible for saving & loading from uSD */
Storage storage;

/** @brief Serial object for communicating with GPS module */
SoftwareSerial serGPS(GPS_RX_PIN, GPS_TX_PIN);

/** @brief Our GPS object, responsible for parsing NMEA codes */
TinyGPSPlus gps;

/** @brief Allocated space for holding all JSON objects within */
StaticJsonBuffer<500> jsonBuffer;

/** @brief Parent JSON object, holds orientation, time & gps children */
JsonObject& mainJSON = jsonBuffer.createObject();

/** @brief Holds all orientation related information */
JsonObject& orientJSON = mainJSON.createNestedObject("orientation");

/** @brief Holds all location related information */
JsonObject& gpsJSON = mainJSON.createNestedObject("gps");

/** @brief Holds all time related inforamtion */
JsonObject& timeJSON = mainJSON.createNestedObject("time");

/* ------ Module Code ------ */

/**
 * @brief Runs once at boot of arduino.
 *
 * Responsible for setting up the peripherals. \n
 * Initialises modules such as storage, bluetooth & gps.
 */
void setup()
{
  pinMode(LED_PIN, OUTPUT);

  /* Initialise our created modules */
  storage.init();
  orientation.init();

  /* Set up serial for wireless data transmission */
  BT_SERIAL.begin(BT_BAUD);

  /* Set up serial for GPS module */
  serGPS.begin(GPS_BAUD);
}

/**
 * @brief Main system loop for arduino.
 *
 * Checks serial to see if any commands are available. \n
 * If available reads the byte and changes system mode relating to it. \n\n
 * System state machine is also iterated through each loop. \n
 * Relevant procedure depending on system state is then called.
 */
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

/**
 * @brief Returns whether system should change operating mode.
 *
 * @param modeChar - The received command byte
 * @param &newMode - Reference to new operating mode calculated via command.
 * @return bool - Whether a valid command was found.
 */
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

/**
 * @brief Responsible for completing work needed in relatime mode.
 *
 * Every time called this procedure will poll the IMU to update
 * our orientation class with newest information. \n
 * If available NMEA sentences received from GPS serial are sent
 * to our GPS parsing object. \n
 *
 * Every 1000ms all current information is transmitted via bluetooth,
 * this information is also stored to the uSD so it can be retrieved at
 * a later point.
 */
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

/**
 * @brief Responsible for updating orientation JSON object
 * with newest information.
 *
 * Interacts with devices Orientation object to get
 * Yaw, Pitch & Roll.
 */
void addOrientationToJSON()
{
  orientJSON["yaw"] = orientation.getYaw();
  orientJSON["pitch"] = orientation.getPitch();
  orientJSON["roll"] = orientation.getRoll();
}

/**
 * @brief Responsible for updating GPS JSON object
 * with newest information.
 *
 * Interacts with devices TinyGPSPlus object to get
 * all locational/gps related information.
 * Floats are cat'd to 6 digits max.
 */
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

/**
 * @brief Responsible for updating time JSON object
 * with newest information.
 *
 * Interacts with devices TinyGPSPlus object to get
 * time related information.
 * This is because GPS module has a RTC (Realtime-Clock) kept
 * via NMEA sentences.
 */
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
