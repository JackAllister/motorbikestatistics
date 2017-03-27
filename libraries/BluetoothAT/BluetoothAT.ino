/* 
 *  Basic configuration sketch to adjust the, HC-06 module to suit our needs.
 */
#define BTserial Serial1

 
void setup() 
{
    Serial.begin(9600);
    while (!Serial) ;
    
    Serial.println("Enter AT commands:");
 
    BTserial.begin(460800);  

    // Configures the bluetooth module the way we want it.
    BTserial.println("AT");
    BTserial.println("AT+NAMELOGGING-DEVICE");
    BTserial.println("AT+BAUDA");
}
 
void loop()
{
 
    // Keep reading from HC-06 and send to Arduino Serial Monitor
    if (BTserial.available())
    {  
        Serial.write(BTserial.read());
    }
 
    // Keep reading from Arduino Serial Monitor and send to HC-06
    if (Serial.available())
    {
        BTserial.write(Serial.read());
    }
 
}
