[Back to README](README.md)

# Get Table

`POST {ApiGatewayURL}/api/v1/getTable`

## Notice
It is strongly recommended to provide the contentUpdatedSinceDateTime in your request. Please leverage the last updated time within the database.

## Controller
[src/main/java/com/milmove/trdmlambda/milmove/controller/GetTableController.java](../src/main/java/com/milmove/trdmlambda/milmove/controller/GetTableController.java)

## Models
[Request Model](../src/main/java/com/milmove/trdmlambda/milmove/model/gettable/GetTableRequest.java)

[Response Model](../src/main/java/com/milmove/trdmlambda/milmove/model/gettable/GetTableResponse.java)

Example
```
{
    "physicalName": "{{TableName}}",
    "contentUpdatedSinceDateTime":"{{now}}",
    "returnContent": true
}
```

#### HTTP Response Codes
`[200, 400, 500, 502]`