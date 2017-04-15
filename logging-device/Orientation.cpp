/**
 * @file Orientation.cpp
 * @author Jack Allister - 23042098
 * @date 2016-2017
 * @brief Module created to deal with all orientation related functionality.
 *
 * Uses the built in Gyroscope & Accelerometer of the Arduino 101
 * to create an Inertial Measurement Unit (IMU).
 */

/* ------ Module Includes ------ */
#include <BMI160.h>
#include <CurieIMU.h>

#include "Orientation.h"

using namespace LoggingDevice;

/* ------ Module Constants ------ */

/** @brief Frequency of update rate for IMU (25Hz) */
#define IMU_FREQUENCY 25
/** @brief Range of acelerometer +-2G */
#define ACCEL_RANGE 2
/** @brief Range of gyroscope +-250 deg/sec */
#define GYRO_RANGE 250

/** @brief Number of axis for our IMU */
#define NUMBER_AXIS 3
/** @brief Reference to X axis in array */
#define AXIS_X 0
/** @brief Reference to Y axis in array */
#define AXIS_Y 1
/** @brief Reference to Z axis in array */
#define AXIS_Z 2

/* ------ Module Code ------ */

/**
 * @brief Initialisation function for orientation module.
 *
 * Initialises the CurieIMU module with set ranges and rates,
 * our Madgwick filter is also initialised with this information.
 */
void Orientation::init()
{
  /* Set up the Gyroscope + Accelerometer */
  CurieIMU.begin();
  CurieIMU.setGyroRate(IMU_FREQUENCY);
  CurieIMU.setAccelerometerRate(IMU_FREQUENCY);
  CurieIMU.setAccelerometerRange(ACCEL_RANGE);
  CurieIMU.setGyroRange(GYRO_RANGE);

  IMUfilter.begin(IMU_FREQUENCY);
}

/**
 * @brief Updates the IMU with newest values at 25Hz frequency.
 *
 * Function reads raw values from accelerometer and gyroscope and sends
 * them to our Madgwick filter (IMUfilter). \n
 * This function needs to be called by the system as often as possible. \n
 * To ensure correct frequency of 25Hz if kept to a micros counter is
 * in place. \n
 * Function will return true or false as of whether that call actually updated
 * the IMU (depending on micros count check).
 *
 * @return bool - Whether the IMU was actually updated.
 */
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

/**
 * @brief Converts a raw reading from accelerometer to a value in G.
 *
 * @param aRaw - Raw accelerometer axis value.
 * @return float - Processed acceleration axis in G.
 */
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

/**
 * @brief Converts a raw reading from gyro to a value in deg/sec.
 *
 * @param gRaw - Raw gyroscope axis value.
 * @return float - Processed rotation axis in deg/sec.
 */
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

/**
 * @brief Returns the Yaw orientation of the device.
 * @return float - Yaw orientation.
 */
float Orientation::getYaw()
{
  return IMUfilter.getYaw();
}

/**
 * @brief Returns the Pitch orientation of the device.
 * @return float - Pitch orientation.
 */
float Orientation::getPitch()
{
  return IMUfilter.getPitch();
}

/**
 * @brief Returns the Roll orientation of the device.
 * @return float - Roll orientation.
 */
float Orientation::getRoll()
{
  return IMUfilter.getRoll();
}
