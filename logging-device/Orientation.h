/*
 * Header for orientation module.
 */
 #ifndef ORIENTATION_H
 #define ORIENTATION_H

/* ------ Module Includes ------ */
 #include <MadgwickAHRS.h>

/* ------ Module Class ------ */
class Orientation
{
private:
  /* ------ Private Variables ------ */
  Madgwick IMUfilter;

  /* ------ Private Prototypes ------ */
  float convertRawAccel(int aRaw);
  float convertRawGyro(int aRaw);

public:
  /* ------ Public Prototypes ------ */
  Orientation();
  bool pollIMU();
  float getYaw();
  float getPitch();
  float getRoll();
};

#endif
