## Swagger Petstore
This is a sample server Petstore server.  You can find out more about Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, you can use the api key `special-key` to test the authorization filters.
### About
| Url                                                               | Version | Contact                                                         | Terms of Service                                                        | License                                                                 |
| ----------------------------------------------------------------- | ------- | --------------------------------------------------------------- | ----------------------------------------------------------------------- | ----------------------------------------------------------------------- |
| [petstore.swagger.io/v2](http://petstore.swagger.io/v2 "API url") | 1.0.0   | [apiteam@swagger.io](mailto:apiteam@swagger.io "Contact Email") | [http://swagger.io/terms/](http://swagger.io/terms/ "Terms of Service") | [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html "License") |

### Schemes
| Scheme |
| ------ |
| http   |

## Endpoints
## store/order/{orderId}
## GET
### getOrderById
Find purchase order by ID
For valid response try integer IDs with value >= 1 and <= 10. Other values will generated exceptions
### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
|          |                      |
| 200      | successful operation |
| 400      | Invalid ID supplied  |
| 404      | Order not found      |

### Parameters
| Name    | In   | Description                        | Required? | Type    |
| ------- | ---- | ---------------------------------- | --------- | ------- |
|         |      |                                    |           |         |
| orderId | path | ID of pet that needs to be fetched | true      | integer |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id   | Scopes |
| ---- | ------ |
| None | None   |
## DELETE
### deleteOrder
Delete purchase order by ID
For valid response try integer IDs with positive integer value. Negative or non-integer values will generate API errors
### Expected Response Types
| Response | Reason              |
| -------- | ------------------- |
| 400      | Invalid ID supplied |
| 404      | Order not found     |

### Parameters
| Name    | In   | Description                              | Required? | Type    |
| ------- | ---- | ---------------------------------------- | --------- | ------- |
|         |      |                                          |           |         |
| orderId | path | ID of the order that needs to be deleted | true      | integer |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id   | Scopes |
| ---- | ------ |
| None | None   |
## store/order
## POST
### placeOrder
Place an order for a pet

### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
| 200      | successful operation |
| 400      | Invalid Order        |

### Parameters
| Name | In   | Description                         | Required? | Type                       |
| ---- | ---- | ----------------------------------- | --------- | -------------------------- |
|      |      |                                     |           | [Order](#order-definition) |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id   | Scopes |
| ---- | ------ |
| None | None   |
## pet/findByStatus
## GET
### findPetsByStatus
Finds Pets by status
Multiple status values can be provided with comma separated strings
### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
| 200      | successful operation |
| 400      | Invalid status value |

### Parameters
| Name   | In    | Description                                         | Required? | Type  |
| ------ | ----- | --------------------------------------------------- | --------- | ----- |
|        |       |                                                     |           |       |
| status | query | Status values that need to be considered for filter | true      | array |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id            | Scopes     |
| ------------- | ---------- |
|               |            |
| petstore_auth | write:pets |
## user/createWithList
## POST
### createUsersWithListInput
Creates list of users with given input array

### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
|          |                      |
| default  | successful operation |

### Parameters
| Name | In   | Description         | Required? | Type  |
| ---- | ---- | ------------------- | --------- | ----- |
|      |      |                     |           |       |
| body | body | List of user object | true      | array |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id   | Scopes |
| ---- | ------ |
| None | None   |
## user/createWithArray
## POST
### createUsersWithArrayInput
Creates list of users with given input array

### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
|          |                      |
| default  | successful operation |

### Parameters
| Name | In   | Description         | Required? | Type  |
| ---- | ---- | ------------------- | --------- | ----- |
|      |      |                     |           |       |
| body | body | List of user object | true      | array |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id   | Scopes |
| ---- | ------ |
| None | None   |
## store/inventory
## GET
### getInventory
Returns pet inventories by status
Returns a map of status codes to quantities
### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
|          |                      |
| 200      | successful operation |

### Parameters
| Name | In | Description | Required? | Type |
| ---- | -- | ----------- | --------- | ---- |

### Content Types Produced
| Produces         |
| ---------------- |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id      | Scopes |
| ------- | ------ |
|         |        |
## pet/{petId}
## GET
### getPetById
Find pet by ID
Returns a single pet
### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
|          |                      |
| 200      | successful operation |
| 400      | Invalid ID supplied  |
| 404      | Pet not found        |

### Parameters
| Name  | In   | Description         | Required? | Type    |
| ----- | ---- | ------------------- | --------- | ------- |
|       |      |                     |           |         |
| petId | path | ID of pet to return | true      | integer |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id      | Scopes |
| ------- | ------ |
|         |        |
## POST
### updatePetWithForm
Updates a pet in the store with form data

### Expected Response Types
| Response | Reason        |
| -------- | ------------- |
|          |               |
| 405      | Invalid input |

### Parameters
| Name   | In       | Description                        | Required? | Type    |
| ------ | -------- | ---------------------------------- | --------- | ------- |
|        |          |                                    |           |         |
| petId  | path     | ID of pet that needs to be updated | true      | integer |
| name   | formData | Updated name of the pet            | false     | string  |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes                          |
| --------------------------------- |
| application/x-www-form-urlencoded |

### Security
| Id            | Scopes     |
| ------------- | ---------- |
|               |            |
| petstore_auth | write:pets |
## DELETE
### deletePet
Deletes a pet

### Expected Response Types
| Response | Reason              |
| -------- | ------------------- |
| 400      | Invalid ID supplied |
| 404      | Pet not found       |

### Parameters
| Name    | In     | Description      | Required? | Type    |
| ------- | ------ | ---------------- | --------- | ------- |
| api_key | header |                  | false     | string  |
| petId   | path   | Pet id to delete | true      | integer |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id            | Scopes     |
| ------------- | ---------- |
|               |            |
| petstore_auth | write:pets |
## pet/findByTags
## GET
### findPetsByTags
Finds Pets by tags
Muliple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing.
### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
| 200      | successful operation |
| 400      | Invalid tag value    |

### Parameters
| Name | In    | Description       | Required? | Type  |
| ---- | ----- | ----------------- | --------- | ----- |
|      |       |                   |           |       |
| tags | query | Tags to filter by | true      | array |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id            | Scopes     |
| ------------- | ---------- |
|               |            |
| petstore_auth | write:pets |
## pet
## POST
### addPet
Add a new pet to the store

### Expected Response Types
| Response | Reason        |
| -------- | ------------- |
|          |               |
| 405      | Invalid input |

### Parameters
| Name | In   | Description                                    | Required? | Type                   |
| ---- | ---- | ---------------------------------------------- | --------- | ---------------------- |
|      |      |                                                |           | [Pet](#pet-definition) |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes         |
| ---------------- |
| application/json |
| application/xml  |

### Security
| Id            | Scopes     |
| ------------- | ---------- |
|               |            |
| petstore_auth | write:pets |
## PUT
### updatePet
Update an existing pet

### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
|          |                      |
| 400      | Invalid ID supplied  |
| 404      | Pet not found        |
| 405      | Validation exception |

### Parameters
| Name | In   | Description                                    | Required? | Type                   |
| ---- | ---- | ---------------------------------------------- | --------- | ---------------------- |
|      |      |                                                |           | [Pet](#pet-definition) |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes         |
| ---------------- |
| application/json |
| application/xml  |

### Security
| Id            | Scopes     |
| ------------- | ---------- |
|               |            |
| petstore_auth | write:pets |
## pet/{petId}/uploadImage
## POST
### uploadFile
uploads an image

### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
|          |                      |
| 200      | successful operation |

### Parameters
| Name               | In       | Description                       | Required? | Type    |
| ------------------ | -------- | --------------------------------- | --------- | ------- |
|                    |          |                                   |           |         |
| petId              | path     | ID of pet to update               | true      | integer |
| additionalMetadata | formData | Additional data to pass to server | false     | string  |

### Content Types Produced
| Produces         |
| ---------------- |
| application/json |

### Content Types Consumed
| Consumes            |
| ------------------- |
| multipart/form-data |

### Security
| Id            | Scopes     |
| ------------- | ---------- |
|               |            |
| petstore_auth | write:pets |
## user/logout
## GET
### logoutUser
Logs out current logged in user session

### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
|          |                      |
| default  | successful operation |

### Parameters
| Name | In | Description | Required? | Type |
| ---- | -- | ----------- | --------- | ---- |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id   | Scopes |
| ---- | ------ |
| None | None   |
## user/login
## GET
### loginUser
Logs user into the system

### Expected Response Types
| Response | Reason                             |
| -------- | ---------------------------------- |
| 200      | successful operation               |
| 400      | Invalid username/password supplied |

### Parameters
| Name     | In    | Description                          | Required? | Type   |
| -------- | ----- | ------------------------------------ | --------- | ------ |
| username | query | The user name for login              | true      | string |
| password | query | The password for login in clear text | true      | string |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id   | Scopes |
| ---- | ------ |
| None | None   |
## user
## POST
### createUser
Create user
This can only be done by the logged in user.
### Expected Response Types
| Response | Reason               |
| -------- | -------------------- |
|          |                      |
| default  | successful operation |

### Parameters
| Name | In   | Description         | Required? | Type                     |
| ---- | ---- | ------------------- | --------- | ------------------------ |
|      |      |                     |           | [User](#user-definition) |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id   | Scopes |
| ---- | ------ |
| None | None   |
## user/{username}
## GET
### getUserByName
Get user by user name

### Expected Response Types
| Response | Reason                    |
| -------- | ------------------------- |
|          |                           |
| 200      | successful operation      |
| 400      | Invalid username supplied |
| 404      | User not found            |

### Parameters
| Name     | In   | Description                                                | Required? | Type   |
| -------- | ---- | ---------------------------------------------------------- | --------- | ------ |
|          |      |                                                            |           |        |
| username | path | The name that needs to be fetched. Use user1 for testing.  | true      | string |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id   | Scopes |
| ---- | ------ |
| None | None   |
## PUT
### updateUser
Updated user
This can only be done by the logged in user.
### Expected Response Types
| Response | Reason                |
| -------- | --------------------- |
| 400      | Invalid user supplied |
| 404      | User not found        |

### Parameters
| Name     | In   | Description                  | Required? | Type                     |
| -------- | ---- | ---------------------------- | --------- | ------------------------ |
| username | path | name that need to be updated | true      |                          |
| body     | body | Updated user object          | true      | string                   |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id   | Scopes |
| ---- | ------ |
| None | None   |
## DELETE
### deleteUser
Delete user
This can only be done by the logged in user.
### Expected Response Types
| Response | Reason                    |
| -------- | ------------------------- |
| 400      | Invalid username supplied |
| 404      | User not found            |

### Parameters
| Name     | In   | Description                       | Required? | Type   |
| -------- | ---- | --------------------------------- | --------- | ------ |
|          |      |                                   |           |        |
| username | path | The name that needs to be deleted | true      | string |

### Content Types Produced
| Produces         |
| ---------------- |
| application/xml  |
| application/json |

### Content Types Consumed
| Consumes |
| -------- |
| None     |

### Security
| Id   | Scopes |
| ---- | ------ |
| None | None   |
## Security Definitions
| Id            | Type   | Flow     | Authorization Url                       | Name    | In     | Scopes                  |
| ------------- | ------ | -------- | --------------------------------------- | ------- | ------ | ----------------------- |
| petstore_auth | oauth2 | implicit | http://petstore.swagger.io/oauth/dialog |         |        | :write:pets, :read:pets |
| api_key       | apiKey |          |                                         | api_key | header |                         |

| Scope      | Description                 |
| ---------- | --------------------------- |
|            | modify pets in your account |
| write:pets | read your pets              |
## Definitions
### Order Definition
| Property | Type    | Format    |
| -------- | ------- | --------- |
| id       | integer | int64     |
| petId    | integer | int64     |
| quantity | integer | int32     |
| shipDate | string  | date-time |
| status   | string  |           |
| complete | boolean |           |
### Category Definition
| Property | Type    | Format |
| -------- | ------- | ------ |
| id       | integer | int64  |
| name     | string  |        |
### User Definition
| Property   | Type    | Format |
| ---------- | ------- | ------ |
| id         | integer | int64  |
| username   | string  |        |
| firstName  | string  |        |
| lastName   | string  |        |
| email      | string  |        |
| password   | string  |        |
| phone      | string  |        |
| userStatus | integer | int32  |
### Tag Definition
| Property | Type    | Format |
| -------- | ------- | ------ |
| id       | integer | int64  |
| name     | string  |        |
### Pet Definition
| Property  | Type                             | Format |
| --------- | -------------------------------- | ------ |
| id        |                                  | int64  |
| category  | integer                          |        |
| name      | [Category](#category-definition) |        |
| photoUrls | string                           |        |
| tags      | array                            |        |
| status    | array                            |        |
### ApiResponse Definition
| Property | Type    | Format |
| -------- | ------- | ------ |
|          |         |        |
| code     | integer | int32  |
| type     | string  |        |
| message  | string  |        |
## Additional Resources
[Find out more about Swagger](http://swagger.io "External Documentation")
