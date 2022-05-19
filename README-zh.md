# MojoConsolePlus

[EN](./README.md)|中文

MojoConsolePlus(MCP)是一个[Grasscutter](https://github.com/Grasscutters/Grasscutter)插件，旨在提供一个游戏内可用的带用户界面的控制台后端。

## 当前支持功能: 
- [x] 在游戏内发送控制台链接
- [x] 支持游戏内控制台的全部功能
    - [x] 继承了Grasscutter的权限系统，用户本来没有权限的指令，在MojoConsole中也不能使用
    - [x] 将指令执行结果通过MojoConsolePlus返回（而不是由server角色在游戏内发送给用户）
- [x] 自定义配置
- [x] 使用外部CDN

## 重要提醒:

该插件基于Grasscutter的[Development](https://github.com/Grasscutters/Grasscutter/tree/development)分支进行开发。 \

**如果你遇到了问题，请到[Discord](https://discord.gg/T5vZU6UyeG)寻求支持。但是我们不保证我们能提供相应的支持。**

**MojoConsolePlus设计时与前端解藕，你可以发起PR来介绍你实现的前端。**

自行为MojoConsolePlus开发前端时，不需要重启grasscutter服务器，刷新页面即可。使用/mojo o指令可以在外部打开浏览器来帮助您开发。

## 配置步骤 
### 下载Jar文件

在Github中找到Releases并进行下载。

### 自行编译（可选）
1. 使用`git`下载代码 ``git clone https://github.com/gc-mojoconsole/gc-mojoconsole-backend``。
2. 找到grasscutter的安装位置，复制 ``grasscutter`` 的jar文件到刚刚新创建的 ``gc-mojoconsole-backend/gc-plugin/lib`` 文件夹。
3. 进入 ``gc-mojoconsole-plus`` 文件夹，执行``gradlew build`` (cmd) **或者** ``./gradlew build`` (Powershell, Linux & Mac)来进行编译。
4. 如果编译成功， 你可以找到``gc-plugin`` 文件夹下的 ``mojoconsole.jar``文件.
5. 讲编译好的 ``mojoconsole.jar`` 文件复制到``Grasscutter`` 安装位置下的 ``plugins`` 文件夹。 
6. 启动grasscutter。

...之后请看使用说明...

### 使用说明

你有两种选择来获得前端界面

#### 使用CDN服务的前端界面(推荐使用)

7. 从仓库中下载`mojoconfig.json`，并将其放置在 `plugins` 文件夹中, 将 `useCDN` 设置为 `true`。或者，您可以先运行grasscutter，mojoconsoleplus将会自动为您生成`mojoconfig.json`。
8. 修改Grasscutter的配置文件 `config.json`，确保您激活了 `cors`选项。在CDN模式下需要CORS设置为True。具体路径为 `config.json`->`server`->`policies`->`cors`.

CDN前端界面由我们在Github Pages上提供。沟通Grasscutter的相关密钥信息是以Hash锚点的形式提供的，所以您的密钥信息在任何情况下都不会通过网络传输，请您放心。我们会经常更新CDN提供的前端，所以您可以在不需要自行管理前端的情况下永远获得最新的功能！激活 `CORS` 不会给您的Grasscutter带来任何形式上的危险，请您放心。

#### 自行提供前端界面
7. 将所有前端文件放入 `GRASSCUTTER_ROOT/plugins/mojoconsole/` 文件夹中。注意你必须要有一个名为 `console.html` 的入口，你也可以放入其他js，css等辅助文件至相同目录下。

#### MojoConsole的游戏指令

8. 游戏内发送 `/mojoconsole` or `/mojo` 给server虚拟角色后，你会在邮箱中收到链接。此外，你可以使用 `o` 参数来调用外部浏览器打开链接，例如发送`/mojo o`给server虚拟角色。默认情况下，mojoconsole是使用游戏内的浏览器进行打开的。

你的目录结构看起来会是这样：
```
GRASSCUTTER根目录
|   grasscutter.jar
|   resources
|   data
|   ...
└───plugins
    │   mojoconsole.jar
    │   mojoconfig.json
    │   ...
    └───mojoconsole
        │   console.html
        |   ...
        └───any other file that you want to include in your frontend
            │   ...
```


## 自行开发API的使用说明

URL: `/mojoplus/api`

Request: `Content-Type: application/json`
```json
    {
        "k": "SESSION_KEY", // **DEPRECATED** sesssion key is embedded in the mail, can be retreved via the GET params.
        "k2": "AUTH_KEY",   // auth key, this is the second version auth key, choose either `k` or `k2`
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


URL: `/mojoplus/auth` Request a auth key for player

Request: `Content-Type: application/json`
```json
    {
        "uid": "UID", // player uid to be requested
        "otp": "OTP", // **OPTIONAL**, use the OTP returned from previous `auth` request to check the status of the ticket.
    }
```

Response: `Content-Type: application/json`
```json
    {
        "message": "success", // message saying the execution status,
        "code": 200, // could be 200 - success, check content in `key` field,
                     // 404 - Player not found or offline
                     // 201 - Not ready yet, player has not confirmed yet
                     // 400 - request not supported
        "key": "OTP or AUTH_KEY", // with `otp` field: AUTH_KEY for that player
                                  // without `otp` field: `OTP` for further request
    }
```

## 其他资源

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

### 前端

By SpikeHD: https://github.com/SpikeHD/MojoFrontend (under development)
CDN前端：https://github.com/gc-mojoconsole/gc-mojoconsole.github.io

...你可以字行开发前端，然后发起PR来让你的前端显示在这里...