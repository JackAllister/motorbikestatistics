/**
 * @file Storage.cpp
 * @author Jack Allister - 23042098
 * @date 2016-2017
 * @brief Module created to handle all storage related functionality.
 *
 * Handles saving, listing & loading of trips.
 * Uses MicroSD available on the Sparkfun GPS logging shield.
 */

/* ------ Module Includes ------ */
#include <SD.h>
#include <ArduinoJson.h>

#include "Storage.h"

/* ------ Module Constants ------ */

/** @brief Mapping for which HW-Serial port BT module is on */
#define BT_SERIAL Serial1
/** @brief Chip select pin for MicroSD card (SPI) */
#define USD_CS 10
/** @brief Maximum amount of log files that can be stored on the device */
#define MAX_LOG_FILES 5000
/** @brief The prefix of the name for logs */
#define LOG_NAME "TRIP_"
/** @brief The suffix of the name for logs (file extension) */
#define LOG_EXTENSION "TXT"

/* ------ Module Code ------ */

/**
 * @brief Initialisation function for storage module.
 *
 * Responsible for starting the uSD library.
 */
void Storage::init()
{
  SD.begin(USD_CS);
}

/**
 * @brief Saves a single line of data to a file.
 *
 * Opens a handle to the current fileName. If the file exists data is
 * appended, if not the file is created first.
 *
 * @param data - Character array of data to save.
 * @param newLine - Whether to add new line character at end of line.
 * @return bool - Whether saving was a success.
 */
bool Storage::saveToFile(char data[], bool newLine)
{
  bool result = false;

  /* Create handle to log file */
  File logHandle = SD.open(fileName, FILE_WRITE);

  /* If handle exists print line to file */
  if (logHandle)
  {

    /* Print line, option to add newline characters */
    logHandle.print(data);
    if (newLine)
    {
      logHandle.println();
    }

    logHandle.close();
    result = true;
  }
  return result;
}

/**
 * @brief Generates a new filename to use for saving.
 *
 * Searches through existing files using pattern PREFIX_ID.SUFFIX \n
 * Existing files are skipped, once non-existant is found that is used.
 *
 * @return bool - Whether a valid file name was able to be found.
 */
bool Storage::generateFileName()
{
  bool result = false;
  int i = 0;

  for (i = 0; i < MAX_LOG_FILES; i++)
  {
    /* Clear name of log file */
    memset(fileName, 0, strlen(fileName));

    /* Set the new log file name to: trip_XXXXX.json */
    sprintf(fileName, "%s%d.%s", LOG_NAME, i, LOG_EXTENSION);

    if (!SD.exists(fileName))
    {
      /* If a file doesn't exist */
      result = true;
      break;
    }
  }

  return result;
}

/**
 * @brief Loads the information of all trips and sends them over bluetooth.
 *
 * Searches directory for trips, then sends trip's name & size over serial.
 */
void Storage::loadTripNames()
{
  bool filesRemaining = true;

  File root = SD.open("/");

  /* Try to open directory for logs */
  if (root)
  {
    /* Ensure starting from start of directory */
    root.rewindDirectory();

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

/**
 * @brief Loads a saved trip and sends data to client via Serial.
 *
 * Waits for the filename to be received via serial.
 * Once file name is received, procedure attempts to open the file.
 * If the file exists it then sends all bytes in the file via Serial.
 */
void Storage::loadSavedTrip()
{
  bool nameComplete = false;
  String fileToOpen = "";

  while (nameComplete == false)
  {
    /* Keep reading input in serial until file name is found */
    if (BT_SERIAL.available() > 0)
    {
      char recvByte = BT_SERIAL.read();
      fileToOpen += recvByte;

      /* Wait until extension is found, then we know full file name */
      if (fileToOpen.endsWith(LOG_EXTENSION))
      {
        nameComplete = true;
      }
    }
  }

  /* Check if file exists */
  if (SD.exists(fileToOpen))
  {
    /* Open file, then read out data byte by byte */
    File handle = SD.open(fileToOpen);
    if (handle)
    {

      while (handle.available())
      {
        char readByte = handle.read();

        BT_SERIAL.write(readByte);
      }

      handle.close();
    }
  }
}
