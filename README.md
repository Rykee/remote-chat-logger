# Remote Clan Info Logger

This plugin is used to send clan related info as HTTP requests with JSON body:

- Clan chat log
- Clan member list
- Clan member joined
- Clan member left/kicked
- Clan member last login time

# Configuration
![Configuration](https://i.imgur.com/plZcu38.png)

**Authorization header**: If provided adds an Authorization header to the HTTP requests. E.g.: Bearer token, Basic auth

**Clan chat URL**:  Clan chat logs will be sent to this URL as a POST request

**Add clan members URL**:  When a player joins the clan, or clicking the "Send clan members list" button on the side panel, a POST request will be sent to this URL.

**Remove clan member URL**: When a player leaves the clan or gets kicked a DELETE request will be sent to this URL.

**Last login URL**:  Update a clan member's last login date


## Request objects

### ClanMessageRequest - Chat logging
|Name|Type|N|Description|
|---|---|---|---|
|sender|String|1|The player's name who sent the message
|message|String|1|The message
|clanName|String|1|The clan's name where the message was sent
|loggedBy|String|1|The user who recorded this message
|timestamp|Timestamp|1|The message's timestamp in ISO 8601 format

Example:
```
{
    "sender": "Nour",
    "message": "I can take share pots",
    "clanName": "Holy Empire",
    "loggedBy": "Rhykee",
    "timestamp": "2021-09-11T12:15:13Z"
}
```    
### ClanMembersRequest - Adding/Deleteing members
|Name|Type|N|Description|
|---|---|---|---|
|members|Array of String|1|The players' names

Example:
```
{
    "members": [
        "Rhykee",
        "Not Needed",
        "Conyrideyak",
        "darth keeper"
    ]
}
```
### UpdateClanMemberLoginRequest

|Name | Type | N | Description |
|---|---|---|---|
|username|String|1|The player's name
|lastLoginTime|Timestamp|1|The time of the login's timestamp in ISO 8601 format

Example:
```
{
    "username": "Rhykee",
    "lastLoginTime": "2021-09-11T12:15:13Z"
}
```
