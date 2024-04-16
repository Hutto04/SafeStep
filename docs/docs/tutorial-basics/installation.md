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

1. ### **Clone the repository**

   ```bash
   git clone https://github.com/SafeStepCSU/SafeStep.git
   ```

2. ### **Set up the mobile app**

   - Open the project in Android Studio (`SafeStep/App_Prototype/App`) - this is the mobile app, open the `App` folder in Android Studio.
   - Build with Gradle and run the app on an emulator or physical device.

3. ### **Set up the web server backend**

   - In that same repository under the same directory, open the `SafeStep/App_Prototype/Server` folder in a new terminal.
   - Install the required packages using `pip`:

     ```bash
     pip install -r requirements.txt
     ```

   - Setup the environment variables:

     - You can create a `.env` file in the `Server` directory and add the following:

       ```bash
       MONGO_URI=mongodb://localhost:27017/safestep
       SECRET_KEY=your_secret_key
       ```

   - Run the Flask server:

     ```bash
       python app.py
     ```

   - The server should now be running on `http://localhost:5000`.

4. ### **Set up the database**
   - Install MongoDB on your local machine.
   - Create a new database called `SafeStep`.
   - Create a collection called `users`.
   - Create a **time-series** collection called `data`.

## Docker Setup

We also provide a Docker setup for easy deployment.
This will automatically set up the web server backend and database without the need for manual configuration.

:::note

If you manually set up the backend and database, you can skip this section, this is an alternative setup method.

:::

:::info

This is currently **incomplete**, need to add the files for the Docker setup.

:::

1. ### **Install Docker**

   - Follow the instructions on the [official Docker website](https://docs.docker.com/get-docker/) to install Docker on your machine.
   - Make sure Docker is running.
   - You can verify this by running:

     ```bash
     docker --version
     ```

2. ### **Install Docker Compose**

   - Follow the instructions on the [official Docker Compose website](https://docs.docker.com/compose/install/) to install Docker Compose on your machine.
   - You can verify this by running:

   ```bash
   docker-compose --version
   ```

3. ### **Set up the environment variables**

   - Create a `.env` file in the project directory and add the following:

     ```bash
     MONGO_URI=mongodb://mongo:27017/safestep
     SECRET_KEY=your_secret_key
     ```

   - This will allow the Flask server to connect to the MongoDB database running in the Docker container.

4. ### **Build the Docker images**

   - In the project directory, run:

   ```bash
   docker-compose build
   ```

5. ### **Run the Docker containers**

   - Run the following command to start the Docker containers:

   ```bash
   docker-compose up
   ```

   - The server should now be running on `http://localhost:5000`.

## Additional Notes

- The mobile app is currently set up to connect to the Flask server running on `http://localhost:5000`. This will need to be updated if you are running the server on a different address or production server.
