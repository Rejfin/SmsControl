# SmsControl
Application to control the phone through text messages

With the help of this application you are able to control your phone via SMS.</br>
You have 18 commands at your disposal, thanks to which you can change the wifi status, change the music volume,
enable synchronization or run selected application. This is only part of the capabilities of this application.

Each command can be disabled or changed by the user.
Additionally, each text message must be preceded by a pin code (which user sets himself)


## Command pattern
```@security_code command argument```
<br>example:
<br>```@1234 wifiOn```
<br>```@1234 setSoundLevel 2```

*if user does not supply an argument for the commands that accept it, the default value for that command will be used


## Command List
* wifiOn -> turn on wifi
* wifiOff -> turn off wifi
* bluetoothOn -> turn on bluetooth
* bluetoothOff -> turn off bluetooth
* soundOn -> set sound volume to 100%
* soundOff -> set sound valume to 0%
* setSoundLevel 5 -> set sound level to 5, the user sets the sound level himself (the scale depends on the device model)
* playSound -> play selected sound
* syncOn -> turn on synchronization
* syncOff -> turn off synchronization
* commandList -> sends a list of active commands to the number from which the command was sent
* getInfo -> sends the most essential information about your phone
* location -> get current location
* runApp -> run selected app
* mobileDataOn -> turn on mobile data (root required)
* mobileDataOff -> turn off mobile data (root required)
* restart -> restart phone (root required)
* shutdown - > shutdown phone (root required)
<br><br>___*root commands may not work on all devices*___ 

## ScreenShots
<br><img src="https://user-images.githubusercontent.com/64009728/90672383-59cea080-e256-11ea-8823-dcd67bd64323.jpg" width="150" height="270">
<img src="https://user-images.githubusercontent.com/64009728/90672392-5d622780-e256-11ea-83c7-c2ecc2dca358.jpg" width="150" height="270">
<img src="https://user-images.githubusercontent.com/64009728/90672397-5f2beb00-e256-11ea-9ec2-fc23cf47a788.jpg" width="150" height="270">
<img src="https://user-images.githubusercontent.com/64009728/90672405-60f5ae80-e256-11ea-9c49-8f6b658bc212.jpg" width="150" height="270">
<img src="https://user-images.githubusercontent.com/64009728/90672413-63580880-e256-11ea-8e97-f1136b372e67.jpg" width="150" height="270">
<img src="https://user-images.githubusercontent.com/64009728/90672433-67842600-e256-11ea-95e2-635f87ac9a7d.jpg" width="150" height="270">
