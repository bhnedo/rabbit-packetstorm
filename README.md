# rabbit-packetstorm

A small experiment on aggregating raw network packet streams into ```Storm``` data processing platform. ```Trident``` is used for continuous computation and stateful processing of packet's analytics. Finally, there is a simple dashboard to observe the analytics in realtime.

![](https://github.com/bhnedo/rabbit-packetstorm/blob/master/rabbit-packetstorm.png)

## Requirements
* Single node or multi node Storm cluster. It is also possible to test the application in local cluster mode.
* Zookeeper
* Kafka
* PostgreSQL
* JDK 8
* Node.js
* Go

