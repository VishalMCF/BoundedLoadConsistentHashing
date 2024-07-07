# Partition Map Visualization

## Request A)
```json
curl --location 'localhost:8080' \
--header 'Content-Type: application/json' \
--data '{
    "partitionCount": 360,
    "replicationFactor": 20,
    "members": [
        {
            "name": "Member1"
        },
        {
            "name": "Member2"
        },
        {
            "name": "Member3"
        },
        {
            "name": "Member4"
        },
        {
            "name": "Member5"
        },
        {
            "name": "Member6"
        },
        {
            "name": "Member7"
        },
        {
            "name": "Member8"
        }
    ]
}
'
```
### Response
```json
{
  "partitionMap": {
    "1": {
      "name": "Member5"
    },
    "2": {
      "name": "Member8"
    },
    "3": {
      "name": "Member6"
    },
    "4": {
      "name": "Member8"
    },
    "5": {
      "name": "Member3"
    },
    "6": {
      "name": "Member1"
    },
    "7": {
      "name": "Member7"
    }
  }
}
```

### Request B)

```cgo
curl --location 'localhost:8080/location' \
--header 'Content-Type: application/json' \
--data '{
    "locateKeys" : [
        "key1",
        "key2",
        "key3",
        "key4"
    ]
}
'
```
### Response
```json
[
    {
        "key1": {
            "name": "Member8"
        }
    },
    {
        "key2": {
            "name": "Member6"
        }
    },
    {
        "key3": {
            "name": "Member3"
        }
    },
    {
        "key4": {
            "name": "Member2"
        }
    }
]
```
