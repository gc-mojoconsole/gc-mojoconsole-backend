# MojoConsolePlus

MojoConsolePlus(MCP) is a [Grasscutter](https://github.com/Grasscutters/Grasscutter) plugin (Apart from 4Benj's GCGM plugin) and it's goal is to implement fully in-game webwiew based .

## Currently Features: 
- [x] Send console link in game
- [x] Do what players can do in the in-game chat based console
    - [x] Inherit the original permission system
    - [x] Capture command response to plugin instead of send chat to player
- [ ] More configurable

## Important Notes:
This plugin is made to run on the current [Development](https://github.com/Grasscutters/Grasscutter/tree/development) branch of Grasscutter. \
This plugin is in very early development and only have the backend now. Frontend is extermely hardcore.
**If you require support please ask on the [Grasscutter Discord](https://discord.gg/T5vZU6UyeG). However, support is not guarenteed.**

**MojoConsolePlus is decoupled with any frontend yet, feel free to open a PR to introduce your implented frontend.**

Development frontend for MojoConsolePlus don't have to restart the Grasscutter server. Just refresh page! There will be a URL printed to the console as log to help you access the console in your browser for development.

## Setup 
### Download Plugin Jar

See realeases.

### Compile yourself
1. Pull the latest code from github using ``git clone https://github.com/mingjun97/gc-mojoconsole-plus`` in your terminal of choice.
2. Locate your grasscutter server and copy the ``grasscutter`` server jar into the newly created ``gcgm-plugin/gc-plugin/lib`` folder
3. Navigate back into the project root folder called ``gc-mojoconsole-plus`` folder and run ``gradlew build`` (cmd) **or** ``./gradlew build`` (Powershell, Linux & Mac).
4. Assuming the build succeeded, in your file explorer navigate to the ``gc-plugin`` folder, you should have a ``mojoconsole.jar`` file, copy it.
5. Navigate to your ``Grasscutter`` server, find the ``plugins`` folder and paste the ``mojoconsole.jar`` into it. 
6. Start your server.

7. Put the all the frontend files into the folder `GRASSCUTTER_RESOURCE/gcstatic/mojo/console.html`.

8. Send command `/mojoconsole` or `/mojo` to server in game, and you will receive mail in your mailbox. Then follow the instructions there.

Your final plugins folder's directory structure should look similar to this
```
plugins
│   mojoconsole.jar
│   ...
resources
└───gcstatic
    │   ...
    └───mojo
        │   console.html
        |   ...
        └───any other file that you want to include in your frontend
            │   ...
```


## API

URL: `/mojoplus/api`

Request: `Content-Type: application/json`
```json
    {
        "k": "SESSION_KEY", // sesssion key is embedded in the mail, can be retreved via the GET params.
        "request": "invoke", // set request to ping will ignore the payload, which just check the aliveness of current sessionKey 
        "payload": "command just like what you do in your in game chat console" // example: "heal" for heal all avatars
    }
```

Response: `Content-Type: application/json`
```json
    {
        "message": "success", // message saying the execution status,
        "code": 200, // could be 200 - success, 403 - SessionKey invalid, 500 - Command execution error (should from command), 400 - request not supported
        "payload": "response for the command", // example: got "All characters have been healed." when invoking with "heal"
    }
```
## Resources

You can use the following function to send the request, just plug it after you finished the command generation job. `payload` is the command you wish to send.

```javascript
function sendCommand(payload){
                var client = new XMLHttpRequest();
                var key = new window.URLSearchParams(window.location.search).get("k");
                var url = '/mojoplus/api';
                client.open("POST", url, true);
                client.setRequestHeader("Content-Type", "application/json");
                client.onreadystatechange = function () {
                if (client.readyState === 4 && client.status === 200) {
                    var result = document.getElementById("c2");
                    // Print received data from server
                    result.innerHTML = JSON.parse(this.responseText).payload.replace(/\n/g, "<p/>");
                }
                };
 
                // Converting JSON data to string
                var data = JSON.stringify({ "k": key, "request": "invoke", "payload": payload });
                // Sending data with the request
                client.send(data);
            }
```

### Frontend

By SpikeHD: https://github.com/SpikeHD/MojoFrontend (under development)

...You can develop your own frontend and make PR to put yours here...