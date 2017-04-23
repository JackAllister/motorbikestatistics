/*
 * Bluetooth example using HC-06 bluetooth module.
 * This prototype just simply echo's what is received
 * back to the master (phone/laptop etc).
 * 
 * References:
 *  http://www.martyncurrey.com/arduino-and-hc-06-zs-040/
 */
#include <SoftwareSerial.h>

static const int BT_TX_PIN = 10;
static const int BT_RX_PIN = 11;
static const int LED_PIN = 13;

SoftwareSerial btModule(BT_TX_PIN, BT_RX_PIN);

void setup() {

  pinMode(LED_PIN, OUTPUT);

  /* Standard 9.6k baud rate */
  btModule.begin(9600);
  Serial.begin(9600);
}

void loop() {
  char recv;

  /* Check if serial has received any data */
  if (btModule.available())
  {
    recv = btModule.read();
    Serial.print(recv);

    if (recv == '1')
    {
      btModule.println("LED turned on");
      digitalWrite(LED_PIN, HIGH);
    }
    else if (recv == '0')
    {
      btModule.println("LED turned off");
      digitalWrite(LED_PIN, LOW);      
    }
  }

}
