/*
 * Header for storage module.
 */
 #ifndef STORAGE_H
 #define STORAGE_H

/* ------ Module Class ------ */
class Storage
{
private:
  /* ------ Private Variables ------ */

  /** @brief File name to use when saving data */
  char fileName[30];
  /** @brief Allocated space for holding JSON objects within */
  StaticJsonBuffer<200> jsonFileBuffer;
  /** @brief JSON object that holds file information (size + name) */
  JsonObject& fileJSON = jsonFileBuffer.createObject();

public:
  /* ------ Public Prototypes ------ */
  void init();
  bool saveToFile(char data[], bool newLine);
  bool generateFileName();
  void loadTripNames();
  void loadSavedTrip();

};

 #endif
