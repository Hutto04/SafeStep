
# SafeStep App Prototype 🚀

This is a basic prototype for our project **SafeStep**. This mobile application will allow users to register, login, and see the current pressure/temperature reading of their smart socks. The app will be connected to the socks via Bluetooth (currently planning to use Pi Pico W, may change).

## Documentation
For our comprehensive documention: https://safestepcsu.github.io/SafeStep/


## Installation/SetUp 🛠️

### Server
First of all, we need to start our Flask server.
1. `cd` into `/Server`
2. `run pip install -r requirements` to install all needed requirements
3. Set up the needed environment variables; see #Environment Variables section
4. Start the server by executing `python app.py`

You should see the following:
 ```txt
 * Serving Flask app 'server.main'
 * Debug mode: on
WARNING: This is a development server. Do not use it in a production deployment. Use a production WSGI server instead.
 * Running on all addresses (0.0.0.0)
 * Running on http://127.0.0.1:5000
 * Running on http://190.0.0.0:5000
```
This means your server is successfully up and running.

### App
Now that our Flask Server is up and running, let's move on to setting up our Android App in Android Studio. Ensure your flask server is ready and Android Studio is set up correctly.
1. Navigate to `SafeStep/App_Prototype` and open `App` in Android Studios.
2. Go to the Gradle scripts and open `build.gradle` (Module:app), look for `buildPython` under `defaultConfig` and change the path to **your path to your Python executable.
3. Now build using Gradle, if successful, you should now be able to run.

For a more detailed guide or troubleshooting help refer to our [documentation](https://safestepcsu.github.io/SafeStep/).


### Database (MongoDB)
I plan to write this out later, but setting up MongoDB is not hard at all.
1. Go to [MongoDB's website](https://www.mongodb.com/) and click *"Try Free"*
2. Make an account and then follow the instructions to create your first FREE database (please ensure you are choosing the free options).
3. Once you have a database, create 2 collections within that DB, one named `users` and another named `data`.
4. You'll need to grant your IP access, which can be done under the "Database Access" Tab.
5. After you've done all that, connect to your DB, choose "Drivers" and follow the instructions to get a **Mongo URI**, which is what you'll need to connect to the DB from the Flask App.

This is just a quick write-up on this, the documentations are really great though, so refer to that if you get stuck.

## Environment Variables

To run this project, you will need to add the following environment variables to your .env file

Don't know what a `.env` file is? Refer to [here](https://blog.devgenius.io/why-a-env-7b4a79ba689).

1. Within the `/Server` directory, create a `.env` file.
2. Add the following environment variables to your `.env`:

`MONGO_URI`=YOUR MONGODB URI

`SECRET_KEY`=LEAVE EMPTY OR JUST PUT WHATEVER KEY YOU WANT


## Bluetooth Setup

- Refer to our [documentation](https://safestepcsu.github.io/SafeStep/docs/tutorial-basics/bluetooth) for a comprehensive guide on setting up Bluetooth on your microcontroller.

## API Reference

#### Get all data for a specific user

```http
  GET /data
```

#### Add specific user data to the database

```http
  POST /data
```

#### Login user

```http
  POST /login
```

#### Register user

```http
  POST /register
```

To see more or get more info please refer to our [documentation](https://safestepcsu.github.io/SafeStep/docs/tutorial-basics/flask-backend).


## Current Features

- User login functionality
- Registration capability
- Secure password hashing
- Route protection with PyJWT
- DB communication from App to Flask Web Server and back
- Preliminary "abnormality detection" feature on the Server
- Profile Page
- Profile Info Editting
- Pairing screen
- Bluetooth communication
- Debugging screen
- Heatmap & Bar graph
- Bottom navgiation

## Tech Stack

**Client:** Java with Android Studios

**Server:** Flask (Python) for backend operations, MongoDB as the database

**Hardware:** Pi Pico W w/ [Round Force-Sensitive Resistor (FSR)](https://www.adafruit.com/product/166), [Ultra Thin 10K Thermistor](https://www.adafruit.com/product/4890) & [Analog/Digital MUX Breakout for Arduino](https://www.amazon.com/Analog-Digital-MUX-Breakout-Arduino/dp/B0068QD86S)


## Acknowledgements
Here is a list of some the references I used while building out this inital prototype:

- https://flask.palletsprojects.com/en/3.0.x/installation/
- https://hackernoon.com/building-an-android-app-on-a-flask-server
- https://www.youtube.com/watch?v=M3gYcPF51QY
- https://ashleyalexjacob.medium.com/flask-api-folder-guide-2023-6fd56fe38c00
- https://www.freecodecamp.org/news/structuring-a-flask-restplus-web-service-for-production-builds-c2ec676de563/#route-protection-and-authorization
- https://flask-bcrypt.readthedocs.io/en/1.0.1/
- https://pyjwt.readthedocs.io/en/stable/
- https://flask-pymongo.readthedocs.io/en/latest/
- https://stackoverflow.com/questions/3505930/make-an-http-request-with-android
- https://square.github.io/okhttp/
- https://www.youtube.com/watch?v=uSY2RqdBL04
- https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
- https://stackoverflow.com/questions/34191731/where-to-store-a-jwt-token#:~:text=If%20you%20are%20using%20REST,store%20in%20PrivateMode%20for%20security.&text=If%20you're%20writing%20an,need%20to%20make%20it%20work).
- https://stackoverflow.com/questions/12906402/type-object-datetime-datetime-has-no-attribute-datetime
- https://stackoverflow.com/questions/66449161/how-to-upgrade-an-android-project-to-java-11
  
Many more please refer to our [documentation's apppendices](https://safestepcsu.github.io/SafeStep/docs/tutorial-extras/appendices)

## TODO 📝

- Explore automating the startup process of both the server and app, possibly through a shell script or batch file, to streamline development further?
- Password salting?
- Add a password confirmation feature to ensure accuracy during registration.
- Incorporate a visibility toggle for passwords
- Data Validation (validate data before inserting to db, anything else?)
- Probably change font
- Real-time data
- Data conversion
- Notifications
- Probably more