/*
 * Basic prototype of the MPU-6050, prints readings out via serial.
 * MPU-6050 module uses i2c protocol for communication to arduino.
 * 
 * MPU-6050 to Arduino pin layout:
 * SDA = A4
 * SCL = A5
 * GND = GND
 * VCC = 5V
 * 
 * Registers and definitions found from 
 * reference manual: RM-MPU-6000A
 * https://store.invensense.com/Datasheets/invensense/RM-MPU-6000A.pdf
 * 
 * Reference to wire library:
 * https://www.arduino.cc/en/Reference/Wire
 * 
 * Reference to calculatingtilt from accelerometer data:
 * http://www.geekmomprojects.com/gyroscopes-and-accelerometers-on-a-chip/
 * 
 */
#include <Math.h>
#include <Wire.h>

/* I2C address for the MPU6050 */
static const int MPU_ADDR = 0x68;

static const uint8_t NUMBER_AXIS = 3;
static const uint8_t AXIS_X = 0;
static const uint8_t AXIS_Y = 1;
static const uint8_t AXIS_Z = 2;

/* defines for i2c when ending transmission session */
#define RELEASE_BUS true
#define RESTART_BUS false

void getReadings(double accl[NUMBER_AXIS], double gyro[NUMBER_AXIS]);

void setup() 
{
  static const uint8_t PWR_MGMT_1 = 0x6B;
  static const uint8_t WAKE_UP = 0;

  /* Initialise the i2c library */
  Wire.begin();

  /* Signal to the 6050 we want to send i2c to it */
  Wire.beginTransmission(MPU_ADDR);

  /* Tell the 6050 to wake up */
  Wire.write(PWR_MGMT_1);
  Wire.write(WAKE_UP);
  Wire.endTransmission(RELEASE_BUS);

  /* Start serial so we can communicate with arduino */
  Serial.begin(9600);
}

void loop() 
{
  static const char AXIS_NAMES[NUMBER_AXIS] = {'X', 'Y', 'Z'};
  
  double acceleration[NUMBER_AXIS];
  double rotation[NUMBER_AXIS];
  char printBuff[50];

  getReadings(acceleration, rotation);

  /* Print rotational data */
  for (int i = 0; i < NUMBER_AXIS; i++)
  {
    sprintf(printBuff, "rot%c: ", AXIS_NAMES[i]);
    Serial.print(printBuff);
    Serial.print(rotation[i]);
    Serial.print(" ");
  }

  /* Print accel data */
  for (int i = 0; i < NUMBER_AXIS; i++)
  {
    sprintf(printBuff, "accl%c: ", AXIS_NAMES[i], acceleration[i]);
    Serial.print(printBuff);
    Serial.print(acceleration[i]);
    Serial.print(" ");
  }
  Serial.println();

  delay(100); 
}

void getReadings(double accl[NUMBER_AXIS], double rotation[NUMBER_AXIS])
{

  static const uint8_t START_ADDR = 0x3B; 
  static const uint8_t RECV_SIZE = 14;
  static const uint8_t BYTE_SIZE = 8;

  static const double GYRO_SCALE = 131.0f;
  static const double DEGREES_PER_RADIAN = 57.2958f;

  /* 
   * These variables needs to be static as we want to be stored 
   * for each call. We also want to be able to set initial values
   */
  static boolean firstRun = true;
  static double lastGX, lastGY, lastGZ;
  
  static double lastTime = millis();

  double currTime = millis();
  double timeStep = (currTime - lastTime) / 1000.0f;
  
  /* Signal to i2c we want to send i2c again */
  Wire.beginTransmission(MPU_ADDR);

  /* 
   * We now need to read the 7 data registers, luckily they are grouped
   * so we can read from START_ADDR for 14 bytes then send a i2c stop
   */
  Wire.write(START_ADDR);
  Wire.endTransmission(RESTART_BUS);
  Wire.requestFrom(MPU_ADDR, (int)RECV_SIZE, (int)true);

  /* Read the raw acceleration values */
  int16_t rawAX = (Wire.read() << BYTE_SIZE) | Wire.read();
  int16_t rawAY = (Wire.read() << BYTE_SIZE) | Wire.read();
  int16_t rawAZ = (Wire.read() << BYTE_SIZE) | Wire.read();

  /* 
   * For some stupid reason temperature register inbetween accel and gyro. 
   * We can ignore this reading as it's not actually needed.
   */
  int16_t rawTmp = (Wire.read() << BYTE_SIZE) | Wire.read(); 

  /* GYRO_SCALE needs to be taken into account (250 deg/s) */
  int16_t rawGX = (Wire.read() << BYTE_SIZE) | Wire.read();
  int16_t rawGY = (Wire.read() << BYTE_SIZE) | Wire.read();
  int16_t rawGZ = (Wire.read() << BYTE_SIZE) | Wire.read();
  
  double scaledGX = double(rawGX) / GYRO_SCALE;
  double scaledGY = double(rawGY) / GYRO_SCALE;
  double scaledGZ = double(rawGZ) / GYRO_SCALE;

  /* Calculate the accelerometer angles */
  accl[AXIS_X] = DEGREES_PER_RADIAN * atan(rawAX / sqrt(square(rawAY) + square(rawAZ)));
  accl[AXIS_Y] = DEGREES_PER_RADIAN * atan(rawAY / sqrt(square(rawAX) + square(rawAZ)));
  accl[AXIS_Z] = DEGREES_PER_RADIAN * atan(sqrt(square(rawAY) + square(rawAX)) / rawAZ);
 
  if (firstRun == true)
  {
    /* Set initial values to equal accel values */
    lastGX = accl[AXIS_X];
    lastGY = accl[AXIS_Y];
    lastGZ = accl[AXIS_Z];
    firstRun = false;
  }
  else
  {
    /* Integrate from past gyro values to calc new ones */
    lastGX = lastGX + (timeStep * scaledGX);
    lastGY = lastGY + (timeStep * scaledGY);
    lastGZ = lastGZ + (timeStep * scaledGZ);
  }

  /* Apply filter to values */
  rotation[AXIS_X] = (0.96 * accl[AXIS_X]) + (0.04 * lastGX);
  rotation[AXIS_Y] = (0.96 * accl[AXIS_Y]) + (0.04 * lastGY);
  rotation[AXIS_Z] = (0.96 * accl[AXIS_Z]) + (0.04 * lastGZ);
 
  /* These values need to be stored for next time function called */
  lastTime = currTime;
}

