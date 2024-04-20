from bson import ObjectId

# Abnormal detector
def is_abnormal(data):
    # Checking pressure data
    pressure_data = data.get('pressure_data')
    if pressure_data:
        for value in pressure_data.values():
        # If the pressure is above x, then it's abnormal
            if value > 5.90:
                return True

    # Now check temperature data for abnormality
    temperature_data = data.get('temperature_data')
    if temperature_data:
        for value in temperature_data.values():
        # If the temperature is above x, then it's abnormal
            if value > 27.20:
                return True
    return False


# Convert ObjectId to string for JSON serialization (MongoDB)
# have to do cases because of the nested nature of the data that we currently have (can change)
def convert_oid(obj):
    # If the object is a list, convert each item in the list
    if isinstance(obj, list):
        return [convert_oid(item) for item in obj]
    # If the object is a dictionary, convert each value in the dictionary
    elif isinstance(obj, dict):
        return {key: convert_oid(value) for key, value in obj.items()}
    # If the object is an ObjectId, convert it to a string
    elif isinstance(obj, ObjectId):
        return str(obj)
    else:
        return obj
