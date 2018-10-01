# DS-Proj2


#### This project has used the protocol of MQTT based on org.eclipse.paho.client.mqttv3


##### How to Run

##### start MQTT broker
+ install mosquitto first
+ run 'mosquitto_sub -h 127.0.0.1 -t mqtt/test' for Subscriber
+ run 'mosquitto_pub -h 127.0.0.1 -t mqtt/test -m "Hello world"' for Publisher



To run the server, 
enter

out/production/Server

run "rmiregistry&" 

then in IDEA, 
run GameServer & GameClient

