/*
 * Prototype 1 for device side of the project.
 * 
 * Currently using:
 *  Arduino UNO
 *  Sparkfun GPS Logger Shield
 *  HC-06 BT receiver/transmitter
 *  MPU-6050 gyroscope
 *  
 *  MPU-6050 Pin Config:
 *    SDA = A4
 *    SCL = A5
 *    GND = GND
 *    VCC = 5V
 *  
 *  In future will need to port to Arduino 101
 *  this will remove the need for MPU-6050 and BT receiver
 */

/* Module Includes */
#include <SoftwareSerial.h>
#include <TinyGPS++.h>
#include <I2Cdev.h>
#include <MPU6050_6Axis_MotionApps20.h>

/* Module Constants */
#define I2CDEV_IMPLEMENTATION I2CDEV_BUILTIN_FASTWIRE

#define SERIAL_BAUD 115200

/* Settings for EM-506 GPS shield (borrowed from Peter) */
//#define GPS_TX_PIN 3
//#define GPS_RX_PIN 2
//#define GPS_BAUD 4800

/* Settings for sparkfun GPS logging shield (uSD version) */
#define GPS_TX_PIN 9
#define GPS_RX_PIN 8
#define GPS_BAUD 9600

/* MPU6050 constants */
#define MPU_INT_PIN 2
#define MPU_ADDR 0x68
#define NUMBER_AXIS 3

#define GYRO_X_OFFSET 206
#define GYRO_Y_OFFSET -31
#define GYRO_Z_OFFSET -23
#define ACCL_X_OFFSET -2495
#define ACCL_Y_OFFSET -1075
#define ACCL_Z_OFFSET 1515


/* Module Variables */
SoftwareSerial serGPS(GPS_RX_PIN, GPS_TX_PIN);
TinyGPSPlus gps;

MPU6050 mpu(MPU_ADDR);
volatile bool mpuInterrupt = false;
float yprAngle[NUMBER_AXIS] = {0, 0, 0};

/* Module Prototypes */
void updateYawPitchRoll();
String getGPSInfo();
String parseDateTime();

/* Module Code */

/* Interrupt Code */
void dmpDataReady()
{
  mpuInterrupt = true;
}

/* System setup code */
void setup() 
{
  /* Set up serial for debugging */
  Serial.begin(SERIAL_BAUD);
  
  /* Set up GPS */
  serGPS.begin(GPS_BAUD);

  /* Set up MPU6050 */
  Fastwire::setup(400, true);
  pinMode(MPU_INT_PIN, INPUT);

  /* Load and configure the DMP within MPU6050 chip */
  mpu.initialize();
  mpu.dmpInitialize();

  /* Calibration offsets for gyro/accel */
  mpu.setXGyroOffset(GYRO_X_OFFSET);
  mpu.setYGyroOffset(GYRO_Y_OFFSET);
  mpu.setZGyroOffset(GYRO_Z_OFFSET);
  mpu.setXAccelOffset(ACCL_X_OFFSET);
  mpu.setYAccelOffset(ACCL_Y_OFFSET);
  mpu.setZAccelOffset(ACCL_Z_OFFSET);

  /* Enable the DMP and set interrupt handler */
  mpu.setDMPEnabled(true);
  attachInterrupt(digitalPinToInterrupt(MPU_INT_PIN), dmpDataReady, RISING);
}

/* Main Code */
void loop() 
{
  static unsigned long lastMillis = 0;

  /* Parse NMEA codes into GPS object */
  while (serGPS.available() > 0)
    gps.encode(serGPS.read());
 
  /* Update current Yaw, Pitch & Roll */
  updateYawPitchRoll();

  /* Print orientation and location information */
  if ((millis() - lastMillis) > 500)
  {

    Serial.print("Yaw: ");
    Serial.print(yprAngle[0]);
    Serial.print("\tPitch: ");
    Serial.print(yprAngle[1]);
    Serial.print("\tRoll: ");
    Serial.print(yprAngle[2]);
    Serial.println();

    Serial.println(getGPSInfo());
    lastMillis = millis();
  }
}

void updateYawPitchRoll()
{
  static uint16_t packetSize = mpu.dmpGetFIFOPacketSize();  /* Get expected size of FIFO packet from DMP */
  static uint16_t fifoCount;                                /* Counts all bytes in FIFO */
  static uint8_t fifoBuffer[64];                            /* FIFO storage buffer */
  static float ypr[NUMBER_AXIS] = {0, 0, 0};
  
  uint8_t mpuIntStatus;   /* Holds interrupt status, can be used to find overflows */
  Quaternion qn;          /* Quaternion container */
  VectorFloat gravity;    /* Gravity vector */
  
  /* Wait for MPU interrupt or packets available */
  while ((mpuInterrupt == false) && (fifoCount < packetSize))
  {

  }

  mpuInterrupt = false;
  mpuIntStatus = mpu.getIntStatus();
  fifoCount = mpu.getFIFOCount();

  if ((mpuIntStatus & 0x10) || fifoCount >= 1024)
  {
    /* FIFO buffer on MPU overflow so we just reset it */
    mpu.resetFIFO();
  }
  else if (mpuIntStatus & 0x02)
  {   
    /* Interrupt triggered correctly */
    while (fifoCount < packetSize)
    {
      fifoCount = mpu.getFIFOCount();
    }

    /* Read a FIFO packet */
    mpu.getFIFOBytes(fifoBuffer, packetSize);

    /* Subtract read packets from count */
    fifoCount -= packetSize;

    mpu.dmpGetQuaternion(&qn, fifoBuffer);
    mpu.dmpGetGravity(&gravity, &qn);
    mpu.dmpGetYawPitchRoll(ypr, &qn, &gravity);

    yprAngle[0] = ypr[0] * 180/M_PI;
    yprAngle[1] = ypr[1] * 180/M_PI;
    yprAngle[2] = ypr[2] * 180/M_PI;
  }
}

String getGPSInfo()
{
  static const char* INVALID_STR = "INVALID";

  /* Get GPS available */
  String result = "GPS Available: ";
  result += gps.satellites.value();
  result += ' ';

  /* Get location */
  result += "Location: ";
  if (gps.location.isValid())
  {
    result += String(gps.location.lat(), 6);
    result += ',';
    result += String(gps.location.lng(), 6);
  }
  else
    result += INVALID_STR;
  result += ' ';

  /* Get speed */
  result += "Velocity: ";
  result += gps.speed.mph();
  result += "mph";
  result += ' ';

  /* Get altitude */
  result += "Altitude: ";
  result += gps.altitude.feet();
  result += "ft";
  result += ' ';

  /* Get Date/Time */
  result += parseDateTime();

  return result;
}

String parseDateTime()
{
  String result = "Date: INVALID";

  if (gps.date.isValid() && gps.time.isValid())
  {
    /* Add date to string */
    result = "Date: ";
    result += gps.date.day();
    result += "/";
    result += gps.date.month();
    result += "/";
    result += gps.date.year();

    /* Add time to string */
    result += " ";
    if (gps.time.hour() < 10)
      result += '0';
    result += gps.time.hour();
    result += ":";
    if (gps.time.minute() < 10)
      result += '0';
    result += gps.time.minute();
    result += ":";
    if (gps.time.second() < 10)
      result += '0';
    result += gps.time.second();
  }

  return result;
}

