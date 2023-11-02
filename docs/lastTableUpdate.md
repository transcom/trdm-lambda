[Back to README](README.md)


# Last Table Update

`POST {ApiGatewayURL}/api/v1/lastTableUpdate`

## Controller
[src/main/java/com/milmove/trdmlambda/milmove/controller/LastTableUpdateController.java](../src/main/java/com/milmove/trdmlambda/milmove/controller/LastTableUpdateController.java)

## Models

[Request Model](src/main/java/com/milmove/trdmlambda/milmove/model/lasttableupdate/LastTableUpdateRequest.java)

[Response Model](src/main/java/com/milmove/trdmlambda/milmove/model/lasttableupdate/LastTableUpdateResponse.java)

Example
```
{
    "physicalName": "{{TableName}}"
}
```

## HTTP Response Codes
`[200, 502]`