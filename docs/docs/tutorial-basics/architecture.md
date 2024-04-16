---
sidebar_position: 2
---

# Architecture

![Architecture](../../static/img/Data-flow1.png)

## System Overview

1. The smart sock is the main hardware component of SafeStep. It contains sensors that monitor foot temperature and pressure. The data collected by the sensors is transmitted to a mobile app via Bluetooth. The app analyzes the data and alerts the user if there are any signs of potential foot ulcers.

2. The mobile app is the main software component of SafeStep. It receives and analyzes the data from the smart sock. The app is responsible for alerting the user if there are any signs of potential foot ulcers.

3. The web server backend is built with Flask. It is responsible for storing and retrieving data from the MongoDB database. The web server backend also provides an API for the mobile app to communicate with the database.

4. The MongoDB database stores the data collected by the smart sock. The database is used to store historical data and provide insights into the user's foot health over time.

5. The user interacts with the mobile app to view real-time data on their foot health. The app provides alerts and recommendations based on the data collected by the smart sock.

6. The user can also view historical data and trends in the mobile app. This allows the user to track changes in their foot health over time and make informed decisions about their care.

## Deep Dive into the components

### Smart Sock

The smart sock is the main hardware component of SafeStep. It contains sensors that monitor foot temperature and pressure. The sensors are embedded in the sock and collect data in real-time. The data is transmitted to a mobile app via Bluetooth.

### Mobile App

The mobile app is the main software component of SafeStep. It receives and analyzes the data from the smart sock. The app is responsible for alerting the user if there are any signs of potential foot ulcers. The app also provides real-time data on foot temperature and pressure.

### Web Server Backend

The web server backend is built with Flask. It is responsible for storing and retrieving data from the MongoDB database. The web server backend also provides an API for the mobile app to communicate with the database. The web server backend is hosted on a cloud server and is accessible via the internet.

### MongoDB Database

The MongoDB database stores the data collected by the smart sock. The database is used to store historical data and provide insights into the user's foot health over time. The database is hosted on a cloud server and is accessible via the internet.

### User Interface

The user interacts with the mobile app to view real-time data on their foot health. The app provides alerts and recommendations based on the data collected by the smart sock. The user can also view historical data and trends in the mobile app. This allows the user to track changes in their foot health over time and make informed decisions about their care.

### Conclusion

SafeStep is a comprehensive system that combines hardware and software to monitor the foot health of diabetic patients. The system provides real-time data on foot temperature and pressure and alerts the user if there are any signs of potential foot ulcers. The system also stores historical data and provides insights into the user's foot health over time. SafeStep is designed to help prevent foot ulcers and amputations by providing diabetic patients with the information they need to take care of their feet.

**_Maybe...insert image or video Here_**
