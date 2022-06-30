# MTAXI

## Introduction
The goal of the project is to simulate a network of mTaxis delivering rides, published by an external service. They cooperate to deliver and they organize themselves to elect a Master MTaxi.

This was an university project for the **distributed and pervasive systems** course.

## Components
### MTAXI
**MQTT server** publishing orders on a given topic. The rides have starting and end coordinates. 

### REST API
The api makes possible to mtaxis to enter the system and offers a resource to put statistics computed by the master mtaxi.

### MTaxis
The mtaxis are the critical point of the project. They communicate with each other via **gRPC** and they send statistics to the REST API.

### Pollution sensors
Every mtaxi has a pollution sensor.
The code simulates a sensor that produces a stream of data, the measurements are processed by every mtaxi with an **overlapping sliding window**.

## Functionalities
### MTaxis election
The mtaxi network doesn't rely on the REST API to choose a mtaxi, they use in fact a **ring election** to choose the drone with the highest battery and ID. 

Every mtaxi periodically ping the master and when he's down, they start the election process choosing a new one. 

### Rides assignment

The Master MTaxi receives rides by subscribing to the order topic with an **MQTT client**.

After that, he decides who will deliver an order based on battery level and proximity.
The chosen mtaxi will deliver and send back statistics to the master, such as kms, residual battery, average pollution etc.

### Network statistics 

Every ten seconds the Master MTaxi sends statistics to the REST API, such as the average number of deliveries per mtaxi, the average kms, pollution etc. 
They are computed aggregating statistics sent by mtaxis after each delivery. 

### RPC communications
MTaxi communicates with each other via gRPC. Each one of them has a server that implements services and each service is a particular type of communication. For instance, when a mtaxi enters the network it sends other its details with an RPC request on a given service, attaching the a payload.

The messages are defined with **protobuf**. 
