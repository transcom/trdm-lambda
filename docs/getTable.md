[Back to README](README.md)

# Get Table

`POST {ApiGatewayURL}/api/v1/getTable`

## Controller
[src/main/java/com/milmove/trdmlambda/milmove/controller/GetTableController.java](../src/main/java/com/milmove/trdmlambda/milmove/controller/GetTableController.java)

## Models
[Request Model](../src/main/java/com/milmove/trdmlambda/milmove/model/gettable/GetTableRequest.java)

[Response Model](../src/main/java/com/milmove/trdmlambda/milmove/model/gettable/GetTableResponse.java)

## Example Requests

**README**: All date time requests are to be provided as strings in XMLGregorian format (Go's time.Time defaults to this). Booleans are to be provided as booleans.

### Request all data since a certain date time 

Example
```
{
    "physicalName": "LN_OF_ACCT",
    "contentUpdatedSinceDateTime":"2023-11-03T12:29:57.040Z",
    "returnContent": true
}
```

### Request all data for a specific date time range

Example
```
{
    "physicalName": "LN_OF_ACCT",
    "contentUpdatedSinceDateTime":"2023-11-03T12:29:57.040Z",
    "returnContent": true,
    "contentUpdatedOnOrBeforeDateTime": "2023-11-04T12:29:57.040Z"
}
```

### Optional Fields (Including not found in example)

`contentUpdatedOnOrBeforeDateTime` is an XMLGregorianCalendar time. When provided along with `contentUpdatedSinceDateTime`, our API will convert that to an `IS_ON_OR_AFTER` and  

`IS_ON_OR_BEFORE` filter to retrieve data for the specified date range.

`returnRowStatus` is a boolean to return the status of the row from TRDM (Such as active or inactive).

`returnLastUpdate` is a boolean to return the last update with the request from TRDM.
#### HTTP Response Codes
`[200, 400, 500, 502]`
