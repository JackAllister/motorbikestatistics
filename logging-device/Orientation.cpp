
/*
 * Module created to deal with all orientation related functionality.
 */

/* ------ Module Includes ------ */
#include <BMI160.h>
#include <CurieIMU.h>
#include <MadgwickAHRS.h>

#include "Orientation.h"

/* ------ Module Constants ------ */
#define IMU_FREQUENCY 25
#define ACCEL_RANGE 2
#define GYRO_RANGE 250

#define NUMBER_AXIS 3
#define AXIS_X 0
#define AXIS_Y 1
#define AXIS_Z 2

/* ------ Module Code ------ */

Orientation::Orientation()
{
  /* Set up the Gyroscope + Accelerometer */
  CurieIMU.begin();
  CurieIMU.setGyroRate(IMU_FREQUENCY);
  CurieIMU.setAccelerometerRate(IMU_FREQUENCY);
  CurieIMU.setAccelerometerRange(ACCEL_RANGE);
  CurieIMU.setGyroRange(GYRO_RANGE);

  IMUfilter.begin(IMU_FREQUENCY);
}

bool Orientation::pollIMU()
{
  static const unsigned long US_PER_READING = 1000000 / IMU_FREQUENCY;
  static unsigned long usPrevious = micros();

  bool result = false;
  int accel_raw[NUMBER_AXIS];
  int gyro_raw[NUMBER_AXIS];
  float accel_g[NUMBER_AXIS];
  float gyro_ds[NUMBER_AXIS];
  unsigned long usNow;

  /* Ensures we stick to the sample rate (by not sampling too early) */
  usNow = micros();
  if ((usNow - usPrevious) >= US_PER_READING)
  {
    /* Read raw data from the IMU */
    CurieIMU.readMotionSensor(accel_raw[AXIS_X], accel_raw[AXIS_Y], accel_raw[AXIS_Z],
                              gyro_raw[AXIS_X], gyro_raw[AXIS_Y], gyro_raw[AXIS_Z]);

    /* Convert raw readings from IMU to accel (G) and rotation vel (deg/s) */
    accel_g[AXIS_X] = convertRawAccel(accel_raw[AXIS_X]);
    accel_g[AXIS_Y] = convertRawAccel(accel_raw[AXIS_Y]);
    accel_g[AXIS_Z] = convertRawAccel(accel_raw[AXIS_Z]);
    gyro_ds[AXIS_X] = convertRawGyro(gyro_raw[AXIS_X]);
    gyro_ds[AXIS_Y] = convertRawGyro(gyro_raw[AXIS_Y]);
    gyro_ds[AXIS_Z] = convertRawGyro(gyro_raw[AXIS_Z]);

    /* Update the filter. Orientation is calculated here */
    IMUfilter.updateIMU(gyro_ds[AXIS_X], gyro_ds[AXIS_Y], gyro_ds[AXIS_Z],
                        accel_g[AXIS_X], accel_g[AXIS_Y], accel_g[AXIS_Z]);

    /* Increment previous counter */
    usPrevious += US_PER_READING;

    result = true;
  }

  return result;
}

float Orientation::convertRawAccel(int aRaw)
{
  /*
   * Since using 2G range.
   * -2G maps to raw value of -32768
   * +2G maps to raw value of +32767
   */
   float a = (aRaw * (float)ACCEL_RANGE) / 32768.0;
   return a;
}

float Orientation::convertRawGyro(int gRaw)
{
  /*
   * since we are using 250 degrees/seconds range
   * -250 maps to a raw value of -32768
   * +250 maps to a raw value of 32767
   */
  float g = (gRaw * (float)GYRO_RANGE) / 32768.0;
  return g;
}

float Orientation::getYaw()
{
  return IMUfilter.getYaw();
}

float Orientation::getPitch()
{
  return IMUfilter.getPitch();
}

float Orientation::getRoll()
{
  return IMUfilter.getRoll();
}
