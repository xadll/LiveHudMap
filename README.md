# Live HUD Map for Wurm Unlimited

Sponsored by [Razors Edge (Ages of Urath Saga)](http://forum.wurmonline.com/index.php?/topic/133419-razors-edge-ages-of-urath-saga/) Server

## Installation
Requires [client modloader](https://github.com/ago1024/WurmClientModLauncher/releases/latest)

* Download `livemap.zip`
* Extract `livemap.zip` into client folder (the jar should land in `mods/livemap/livemap.jar`)
* Enjoy

![Flat View](img/flat-view.png) ![3D View](img/3d-view.png) ![Topographic View](img/topographic-view.png) ![Cave View](img/cave-view.png)

## Known Issues

* The 3D view may scroll out of range when walking up a high mountain
* Your player location marker may end up behind a mountain in 3D view

## Settings

Configure your servers in `livemap.properties`.

```properties
server.0.name=Server Name A
server.0.size=4096
server.0.deeds_json_url=https://example.com/server-a/deeds.json

server.1.name=Server Name B
server.1.size=4096
server.1.deeds_json_url=https://example.com/server-b/deeds.json
server.1.default=true

server.2.name=Server Name C
server.2.size=2048
server.2.deeds_json_url=https://example.com/server-c/deeds.json
```

Options
- `server.<number>`: Server keys must be numbered sequentially starting from 0, one after another.
- `default=true`: Marks which server deeds are loaded automatically on client launch (set on only one server).
- `size`: World map dimension in tiles (e.g. `2048` or `4096`).
- `deeds_json_url`: Direct URL to fetch the deeds.json file for overlaying deed boundaries.

## In-Game Commands

`toggle livemap`
