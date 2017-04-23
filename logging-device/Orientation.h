/*
 * Header for orientation module.
 */
 #ifndef ORIENTATION_H
 #define ORIENTATION_H

/* ------ Module Includes ------ */
 #include <MadgwickAHRS.h>

namespace LoggingDevice {
 /**
  * @brief Class for dealing with Orientation functionality on logging device.
  */
class Orientation
{
private:
  /* ------ Private Variables ------ */
  /** @brief Madgwick filter object uses to steady orientation readings */
  Madgwick IMUfilter;

  /* ------ Private Prototypes ------ */
  float convertRawAccel(int aRaw);
  float convertRawGyro(int aRaw);

public:
  /* ------ Public Prototypes ------ */
  void init();
  bool pollIMU();
  float getYaw();
  float getPitch();
  float getRoll();
};

}

#endif
