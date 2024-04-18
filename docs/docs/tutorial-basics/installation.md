---
sidebar_position: 1
---

# Installation

A quick start guide to get you up and running with SafeStep.

## Prerequisites

All requirements needed to run the project:

- `Android Studio` - for the mobile app
- `Flask` - for the web server backend
- `MongoDB` - for the database

## Step-by-Step Guide

1. ### Clone the repository

   ```bash
   git clone https://github.com/SafeStepCSU/SafeStep.git
   ```

2. ### Set up the web server backend

   - In that same repository under the same directory, open the `SafeStep/App_Prototype/Server` folder in a new terminal.
   - Install the required packages using `pip`:

     ```bash
     pip install -r requirements.txt
     ```

   - Setup the environment variables:

     - You can create a `.env` file in the `Server` directory and add the following:

       ```bash
       MONGO_URI=https://safestep.onrender.com/
       SECRET_KEY=your_secret_key
       ```

:::note

`https://safestep.onrender.com/` is the URL to the public Flask server running on Render. You can replace this with your own MongoDB URI if you want to use your own database.

:::

:::info

However, **if you are using an emulator**, you will need to replace the `MONGO_URI` with **YOUR OWN LOCAL URL**. This is because the emulator cannot access the public server running on Render.

Use `http://10.0.0.2:5000` as the URL in the `Helper` class if you are running the Flask Server locally when running on an emulator **(PLEASE BE AWARE THE EMULATOR DOES NOT SUPPORT BLUETOOTH).**

:::
      

   - Run the Flask server:

     ```bash
       python app.py
     ```

   - The server should now be running on `http://localhost:5000`.

3. ### Set up the database
   - Install MongoDB on your local machine.
   - Create a new database called `SafeStep`.
   - Create a collection called `users`.
   - Create a **time-series** collection called `data`.

4. ### Chaquopy 

:::info
Chaquopy is a plugin that allows you to run Python code in an Android app. It is used in the SafeStep project to run the Python code to generate graphs using `matplotlib` in the mobile app.
:::

- Navigate to the `SafeStep/App_Prototype/App` and under `Gradle Scripts`, look for the `build.gradle.kts` file (Module: app).
- Look for: `buildPython("C:/Users/.../AppData/Local/Programs/Python/Python310/python.exe")`
- You will need to replace the path with the path to your Python executable **on your machine**.

5. ### Set up the mobile app

    - Open the project in Android Studio (`SafeStep/App_Prototype/App`) - this is the mobile app, open the `App` folder in Android Studio.
    - Build with Gradle and run the app on an emulator or physical device.

## Docker Setup

We also provide a Docker setup for easy deployment.
This will automatically set up the web server backend and database without the need for manual configuration.

:::note

If you manually set up the backend and database, you can skip this section, this is an alternative setup method.

:::

:::info

This is currently **incomplete**, need to add the files for the Docker setup.

:::

1. ### Install Docker

   - Follow the instructions on the [official Docker website](https://docs.docker.com/get-docker/) to install Docker on your machine.
   - Make sure Docker is running.
   - You can verify this by running:

     ```bash
     docker --version
     ```

2. ### Install Docker Compose

   - Follow the instructions on the [official Docker Compose website](https://docs.docker.com/compose/install/) to install Docker Compose on your machine.
   - You can verify this by running:

   ```bash
   docker-compose --version
   ```

3. ### Set up the environment variables

   - Create a `.env` file in the project directory and add the following:

     ```bash
     MONGO_URI=mongodb://mongo:27017/safestep
     SECRET_KEY=your_secret_key
     ```

   - This will allow the Flask server to connect to the MongoDB database running in the Docker container.

4. ### Build the Docker images

   - In the project directory, run:

   ```bash
   docker-compose build
   ```

5. ### Run the Docker containers

   - Run the following command to start the Docker containers:

   ```bash
   docker-compose up
   ```

   - The server should now be running on `http://localhost:5000`.

## Additional Notes

- The mobile app is currently set up to connect to the Flask server running on `http://localhost:5000`. This will need to be updated if you are running the server on a different address or production server.
