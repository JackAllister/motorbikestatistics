/*
 * Basic prototype of the MPU-6050, prints readings out via serial.
 * MPU-6050 module uses i2c protocol for communication to arduino.
 * 
 * MPU-6050 to Arduino pin layout:
 * SDA = A4
 * SCL = A5
 * GND = GND
 * VCC = 5V
 * INT = PIN2
 * 
 * Registers and definitions found from 
 * reference manual: RM-MPU-6000A
 * https://store.invensense.com/Datasheets/invensense/RM-MPU-6000A.pdf
 * 
 * Reference to wire library:
 * https://www.arduino.cc/en/Reference/Wire
 * 
 */
#include <Wire.h>

/* I2C address for the MPU6050 */
static const uint8_t MPU_ADDR = 0x68;

/* defines for i2c when ending transmission session */
#define RELEASE_BUS true
#define RESTART_BUS false

void setup() {
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

void loop() {
  static const uint8_t START_ADDR = 0x3B; 
  static const uint8_t RECV_SIZE = 14;
  static const uint8_t NUMBER_REGISTERS = 7;
  static const uint8_t BYTE_SIZE = 8;

  static const char* data_name[] = {"acX", "acY", "acZ", "temp", "gX", "gY", "gZ"};
  int16_t data_reg[NUMBER_REGISTERS];
  char print_buff[50];
  
  /* Signal to i2c we want to send i2c again */
  Wire.beginTransmission(MPU_ADDR);

  /* 
   * We now need to read the 7 data registers, luckily they are grouped
   * so we can read from START_ADDR for 14 bytes then send a i2c stop
   */
  Wire.write(START_ADDR);
  Wire.endTransmission(RESTART_BUS);
  Wire.requestFrom((int)MPU_ADDR, (int)RECV_SIZE, (int)true);

  for (int i = 0; i < NUMBER_REGISTERS; i++)
  {
    /* We read in chunks of 2 int8's and store in an array */
    data_reg[i] = (Wire.read() << BYTE_SIZE) | Wire.read();

    /* Print register data */
    sprintf(print_buff, "%s: %d | ", data_name[i], data_reg[i]);
    Serial.print(print_buff);
  }

  Serial.println();
  delay(5000); 
}

