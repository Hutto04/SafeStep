from flask import jsonify, request
from server.main import mongo
from datetime import datetime, timedelta
from server.util.helper import is_abnormal, convert_oid
from flask_bcrypt import Bcrypt
from server.util.decorator import token_required
import random
import jwt

# TODO: Maybe move routes to a separate file for better organization?
def init_app_routes(app):
    """
    @desc: Just a test route to check if the server is running
    """
    @app.route('/')
    def hello_world():
        return 'Hello World! I exist!'


    """
    @desc: Register a new user
    @route: /register
    @method: POST
    @access: Public
    @return: JSON object containing a message indicating the success or failure of the registration
    """
    @app.route('/register', methods=['POST'])
    def register():
        # Directly use mongo.db to access the database
        user_collection = mongo.db.users

        # Should get back a JSON of username and password
        user_data = request.json
        username = user_data.get('username')
        password = user_data.get('password').encode('utf-8')  # Ensure password is in bytes

        if 'username' not in user_data or 'password' not in user_data:
            return jsonify({"message": "Missing username or password."}), 400

        # Check if the username already exists
        if user_collection.find_one({"username": user_data["username"]}) is not None:
            return jsonify({"message": "Username already exists."}), 400

        # Hash the password
        # TODO Salting?
        hashed_password = Bcrypt().generate_password_hash(password).decode('utf-8')
        user_data["password"] = hashed_password

        # Create user document including the createdDate
        user_document = {
            'username': username,
            'password': hashed_password,  # Store the hashed password
            'createdDate': datetime.utcnow() - timedelta(hours=4),  # adjusted for timezone, mongo stores in UTC by default
            'profile': {
                'name': '',
                'email': '',
                'phone': '',
                'age': '',
                'height': '',
                'weight': '',
                'doctor': '',
                'doctor_email': '',
                'emergency_contact': '',
            }

        }

        # Insert the new user into the database
        try:
            user_collection.insert_one(user_document)
            return jsonify({"message": "User registered successfully."}), 201
        except Exception as e:
            print(e)
            return jsonify({"message": "An error occurred while registering the user."})


    """
    @desc: This route is for logging in a user
    @route: /login
    @method: POST
    @access: Public
    @return: JSON object containing a message indicating the success or failure of the login
    """
    @app.route('/login', methods=['POST'])
    def login():
        # Directly use mongo.db to access the database - this could probably be abstracted out (DRY)
        user_collection = mongo.db.users

        # Should get back a JSON of username and password
        user_data = request.json
        username = user_data.get('username')
        password = user_data.get('password').encode('utf-8')

        # Check if the username and password are present
        if 'username' not in user_data or 'password' not in user_data:
            return jsonify({"message": "Missing username or password."}), 400

        # Check if the username already exists
        user = user_collection.find_one({"username": username})
        if user is None:
            return jsonify({"message": "Invalid username or password."}), 400

        # Check if the password is correct
        if Bcrypt().check_password_hash(user["password"], password):
            # Create a token that expires in 1 hour
            token = jwt.encode({
                "username": username,
                "exp": datetime.utcnow() + timedelta(hours=1)},
                app.config['SECRET_KEY'])

            print("Token: ", token)  # debug

            return jsonify({
                "token": token,
                "message": "Login successful."
            }), 200

        return jsonify({"message": "Invalid username or password."}), 400


    """
    @desc: This route is used to retrieve the most recent 10 data entries for a specific user
    @route: /data
    @method: GET
    @access: Private - should only retrieve data for the user after authentication
    @return: JSON object containing the most recent 10 data entries for the user
    """
    @app.route('/data', methods=['GET'])
    @token_required  # this is a decorator that checks for a valid token before allowing access to the route
    def get_data(current_user):
        # Directly use mongo.db to access the database
        data_collection = mongo.db.data

        # get the user's ID from the token
        user_id = current_user.get('_id')
        try:
            # Find the most recent 10 data entries for the user (found by user_id) and sort by timestamp (most recent first)
            data = list(data_collection.find({"user_id": user_id}).sort([("timestamp", -1)]).limit(10))
            # Convert the ObjectId to string
            data = convert_oid(data)
        except Exception as e:
            print(e)
            return jsonify({"message": "An error occurred while retrieving data from the database."})

        return jsonify(data), 200


    """
    @desc: This route is used to retrieve the most recent data entry for a specific user
    @route: /data/latest
    @method: GET
    @access: Private - should only retrieve data for the user after authentication
    @return: JSON object containing the most recent data entry for the user
    """
    @app.route('/data/latest', methods=['GET'])
    @token_required  # this is a decorator that checks for a valid token before allowing access to the route
    def get_latest_data(current_user):
        # Directly use mongo.db to access the database
        data_collection = mongo.db.data

        # get the user's ID from the token
        user_id = current_user.get('_id')
        try:
            # Find the most recent data entry for the user (found by user_id) and sort by timestamp (most recent first)
            data = list(data_collection.find({"user_id": user_id}).sort([("timestamp", -1)]).limit(1))
            # Convert the ObjectId to string
            data = convert_oid(data)
        except Exception as e:
            print(e)
            return jsonify({"message": "An error occurred while retrieving data from the database."})

        return jsonify(data), 200


    """
    @desc: This route is used to insert data into the data collection for a specific user
    @route: /data
    @method: POST
    @access: Private - should only insert data for the user after authentication
    @return: JSON object containing a message indicating the success or failure of the insertion
    """
    @app.route('/data', methods=['POST'])
    @token_required  # this is a decorator that checks for a valid token before allowing access to the route
    def insert_data(current_user):
        data_collection = mongo.db.data

        user_id = current_user.get('_id')

        # print body for debugging
        print(request.json)

        # Generate random pressure data - placeholder for now.
        # TODO: Replace this with actual pressure data from the sensors - so needs to be sent from the phone as json
        # Body currently has a unused JSON object for this
        data = {
            "pressure_data": {
                "MTK-1": random.randint(0, 100),
                "MTK-2": random.randint(0, 100),
                "MTK-3": random.randint(0, 100),
                "MTK-4": random.randint(0, 100),
                "MTK-5": random.randint(0, 100),
                "D1": random.randint(0, 100),
                "Lateral": random.randint(0, 100),
                "Calcaneus": random.randint(0, 100),
            },
            "temperature_data": {
                "Temperature": random.randint(0, 100),
            },
            "timestamp": datetime.utcnow() - timedelta(hours=4),  # adjusted for timezone, mongo stores in UTC by default
            # Need to include the user's ID
            "user_id": user_id
        }

        # Check if any data is abnormal
        # TODO: Modify this to check for abnormal pressure and temperature data and sets the 'abnormal' flag accordingly
        if is_abnormal(data):
            # Take action for abnormal data
            # TODO: Notifications?
            print("Abnormal pressure detected!")
            data['abnormal'] = True  # Mark the data as abnormal

        # Insert into the data collection
        try:
            data_collection.insert_one(data)
            return jsonify({"message": "Data inserted successfully."}), 201
        except Exception as e:
            print(e)
            return jsonify({"message": "An error occurred while inserting data into the database."})


    """
    @desc: This route is used to update the user's profile information
    @route: /profile
    @method: PUT
    @access: Private - should only update the user's profile after authentication
    @return: JSON object containing a message indicating the success or failure of the update
    """
    @app.route('/profile', methods=['PUT'])
    @token_required
    def update_profile(current_user):
        user_collection = mongo.db.users

        user_id = current_user.get('_id')

        # Get the new user data
        new_data = request.json

        # Check if the new data is empty
        if not new_data:
            return jsonify({"message": "No data provided."}), 400

        # Update the user's profile
        try:
            user_collection.update_one({"_id": user_id}, {"$set": {"profile": new_data}})
            return jsonify({"message": "Profile updated successfully."}), 200
        except Exception as e:
            print(e)
            return jsonify({"message": "An error occurred while updating the profile."})


    """
    @desc: This route is used to get the user's profile information
    @route: /profile
    @method: GET
    @access: Private - should only retrieve the user's profile after authentication
    @return: JSON object containing the user's profile information
    """
    app.route('/profile', methods=['GET'])
    @token_required
    def get_profile(current_user):
        user_collection = mongo.db.users

        user_id = current_user.get('_id')

        # Get the user's profile
        try:
            user = user_collection.find_one({"_id": user_id})
            # Convert the ObjectId to string
            user = convert_oid(user)
            profile = user.get('profile')
            return jsonify(profile), 200
        except Exception as e:
            print(e)
            return jsonify({"message": "An error occurred while retrieving the profile."})





