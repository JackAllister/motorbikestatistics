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
  char fileName[30];

public:
  /* ------ Public Prototypes ------ */
  void init();
  bool saveToFile(char data[], bool newLine);
  bool generateFileName();
  void loadTripNames();
  void loadSavedTrip();

};

 #endif
