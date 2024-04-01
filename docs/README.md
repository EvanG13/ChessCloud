# Backend
This documentation is subject to change. It is here to give a general overview of what the backend for will look like.

# Overview
The backend for our `React Native` [chess app](https://github.com/EvanG13/Chess) will consist of two separate APIs working together. The reason why this app 
will be split into two separate APIs is because of our usage of `Amazon API Gateway`. Not every endpoint for our backend
requires `Websockets`. For example, `login` and `logout` endpoints will be purely `RESTful`. However, it is not possible 
to create a single API in `API Gateway` that has both purely `REST endpoints` and purely `Websocket routes` together.

# Technologies
- ## Java & Maven
- ## API Gateway
  - This project uses Amazon API Gateway for managing our APIs
    1. REST API
    2. Websocket API
- ## Lambda
  - This project will use AWS Lambda for the backend. Due to the limited traffic the app will experience, it makes more sense to keep the backend serverless and pay by usage
- ## DynamoDB & MongoDB
  - This project will make use of two databases. The reason for this is ideally cut cost. We would like limit the amount of read and write usage to `DynamoDB` as well as how much `DynamoDB` is storing
    - `Prod` - `DynamoDB` 
    - `Dev`  - `MongoDB` 
- ## Amazon S3
  - TODO

## Infrastructure as Code (IaC)
- ### Terraform
  - This project will utilize Terraform as our infrastructure as code.  