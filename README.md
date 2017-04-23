# A Motorcycle Statistics System for Monitoring Rider Performance

## Introduction

Riding motorbikes means quite a lot to many people, be it a hobby, mode of transport or a profession. Perfecting and improving certain aspects of riding can be quite trivial at times without statistical analysis.

The aim of this project is to aid in this area by developing an affordable device/system that can log important factors related to the rider performance such as location, movement and more. This device is akin to the concept of a telematics, better known as a ‘black boxes’ in cars.

## Deliverables

As mentioned briefly this system consists of two separate pieces of code, the logging device and the front-end application.
This can be seen within the folder named 'motorbikestatistics' included within the submission or via remote version control at:
https://github.com/JackAllister/motorbikestatistics/

Code relating to the logging device is within the folder named 'logging-device'
Code relating to the front-end application in within the folder named 'android-app'


### Front-end application
Installation of Android Studio is required to effectively view code relating to the front-end application. However if this is not possible the files can be viewed within your favourite development environment.

##### Locations of front-end files are as follows:
* **Main Code Java Classes** - android-app\app\src\main\java\com\jack\motorbikestatistics\
* **Unit Tests** - android-app\app\src\test\java\com\jack\motorbikestatistics\
* **Instrumentation Tests** - android-app\app\src\androidTest\java\com\jack\motorbikestatistics\

### Logging device
Installation of Arduino IDE is again required to view, edit and compile logging device code. However once again if this is not possible browsing of code can be done using your favourite development environment.

##### Locations of front-end files are as follows:
* **Logging Device Main Code** - logging-device\

##### Final logging device uses the following hardware:
* Arduino 101
* SparkFun GPS-13750
* HC-06 Bluetooth Module
