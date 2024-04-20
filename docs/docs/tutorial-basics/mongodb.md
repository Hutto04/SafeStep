---
sidebar_position: 5
---

# MongoDB

This covers how we used MongoDB in our project.

## Set up

We will keep this real brief as setting up MongoDB is a straightforward process.

Just go to their [official website](https://www.mongodb.com/) make an account and follow their instruction to make a cluster and then an URI connection string.

## Collections

We have two collections in our database:
  - `users`
    - This collection stores all the user information.
  - `data`
    - This collection stores all the data from the Pico.
      - Pressure readings
      - Temperature readings
    - Be sure to make this **time-series** data as we will be querying this data a lot.

## Additional Information

If you need any additional information or assistance, please submit an issue on our [GitHub repository](https://github.com/SafeStepCSU/SafeStep/issues).
