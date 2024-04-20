---
sidebar_position: 4
---

# Flask Backend

This section will deep dive into the Flask backend and the API endpoints that are used in our project.

## What you will need:

We will not be going over installing any software as it is a straightforward process.

- Python
- Flask
- MongoDB - for storing data
- Postman (optional) - good for testing API endpoints

## Setting up the Flask Backend

1. Head to our [GitHub repository](https://github.com/SafeStepCSU/SafeStep) and clone or download the project to your local machine.
    - Ensure you have the `dev` branch selected.
    - Head to the directory `SafeStep/App_Prototype` and open the project in your preferred IDE.
        1. Open Android Studio and click `File` -> `Open` -> 
        2. Navigate to the `SafeStep/App_Prototype` directory and select the `Server` folder.
        3. Click `OK` and your project should now be open in your preferred IDE.

2. Set up your Environment Variables:
    - Create a `.env` file in the root of the `Server` directory.
    - Add the following environment variables to the `.env` file:
        ```bash
        MONGO_URI=<your_mongo_uri>
        SECRET_KEY=<whatever_secret_key_you_want>
        ```
    - Replace `<your_mongo_uri>` with your MongoDB URI. 
      - Go to [MongoDB](https://www.mongodb.com/) and create an account.
      - Once signed in, follow the instructions to create a new cluster.
      - Follow the instructions to connect to your cluster and get the URI.
    - Replace `<whatever_secret_key_you_want>` with a secret key of your choice.
      - Can be any string of your choice.

3. Once you connect to your MongoDB cluster...

- Create two collections in your database:
  - `users`
  - `data`
    - Make this a time series collection.

4. Install the required dependencies:
    - Open a terminal in the `Server` directory and run the following command:
        ```bash
        pip install -r requirements.txt
        ```
      
5. Run the Flask server:
    - In the terminal, run the following command:
        ```bash
        python app.py
        ```
    - You should see the following output:
        ```bash
        * Running on Running on http://127.0.0.1:5000/
        ```
      - This means the server is running successfully.

## Endpoints

- **GET** `/`
    - Returns 'Hello World! I exist!'
    - Used to check if the server is running, that's all.

### User Endpoints

- **POST** `/register`
  - Registers a new user.
  - Requires a JSON body with the following fields:
    - `username`
    - `password`
  - Sets up every user with a default profile. (all fields are empty).
  - Returns a JSON message with confirmation or error message.

- **POST** `/login`
  - Logs in a user.
  - Requires a JSON body with the following fields:
    - `username`
    - `password`
  - Provides a JWT token for the user to use for authentication.
  - Returns a JSON message with confirmation or error message.

- **GET** `/data`
  - Returns the last 10 data entries from the database for the user.
  - Requires a JWT token in the header.
  - Returns a JSON message with the data entries if successful.

- **GET** `/data/latest`
  - Returns the latest data entry from the database for the user.
  - Requires a JWT token in the header.
  - Returns a JSON message with the data entry if successful.

- **POST** `/data`
    - Adds a new data entry to the database.
    - Requires a JSON body with the following fields:
        - `data`
            - `temperature_data`
            - `pressure_data`
    - Requires a JWT token in the header.
    - Will check if the data is 'abnormal' and mark it as such.
    - Returns a JSON message with confirmation or error message.

- **GET** `/profile`
    - Returns the profile information for the user.
    - Requires a JWT token in the header.
    - Returns a JSON message with the profile information if successful.

- **PUT** `/profile`
    - Updates the profile information for the user.
    - Requires a JSON body with at least ONE of the following fields:
        - `name`
        - `email`
        - `dob`
        - `height`
        - `weight`
        - `doctor_name`
        - `doctor_email`
    - Requires a JWT token in the header.
    - Returns a JSON message with confirmation or error message.

## Good to know

- There is a `config.py` file for setting up the server with different configurations.
  - Such as in Development mode, or Production mode.
  - Default is Development mode.
  - This is useful for setting up different environments for testing and production easily.

- The `helper.py` file contains helper functions for the server.
  - `is_abnormal` - checks if the data is 'abnormal' and marks it as such. (If x value is > x mark as abnormal)
  - `convert_old` - converts the obj accordingly: 
    - object -> list
    - object -> string
    - object -> dictionary