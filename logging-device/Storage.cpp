/*
 * Module create to deal with all storage related functionality.
 */

/* ------ Module Includes ------ */
#include <SD.h>
#include <ArduinoJson.h>

#include "Storage.h"

/* ------ Module Constants ------ */
#define BT_SERIAL Serial1
#define USD_CS 10
#define MAX_LOG_FILES 5000
#define LOG_NAME "TRIP_"
#define LOG_EXTENSION "TXT"

/* ------ Module Variables ------ */
StaticJsonBuffer<200> jsonFileBuffer;
JsonObject& fileJSON = jsonFileBuffer.createObject();

/* ------ Module Code ------ */

void Storage::init()
{
  SD.begin(USD_CS);
}

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
