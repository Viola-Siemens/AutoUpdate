# AutoUpdate
AutoUpdate Framework for Java

### Description
This project is coded for Minecraft client for a modded server. Users can add JVM arguments `-javaagent:AutoUpdate.jar=<url>` (for example, -javaagent:AutoUpdate.jar=https://localhost:8080/) to start self auto update before game starts.

### Client
After building this project, you'll get two jar files: `AutoUpdate.jar` as main file, and `gson-2.9.0.jar` as lib file.

If you use this project for mod self-update, you need to move these two files into .minecraft folder (same directory with mods folder).

Then add above-mentioned `-javaagent:AutoUpdate.jar=<url>` JVM arguments to the launcher. Each time before game starts, you'll see a window that gives you information about self updating and will end with "Successfully updated."

### Server
It's no need to upload these two jar files to the server, but you still need to create a json config file `setup.js` and public resource file directory (for example, "./mods/") to store files mentioned in `setup.js`.

For json file `setup.js`, there's only 1 necessary field `mod`, which is a json array.
Each element in `mod` has 4 necessary fields `name` as file name to update, `size` as file size in byte, `sha1` as the sha1 hash code of file, `previous` as the previous version files (to replace to the higher version file).

Here is an example of `setup.js`:

```javascript
{
	"name": "LSC",
	"description": "",
	"version": "9.0.0",
	"website":"https://lsc.ungine.cn/",
	"mods": [
		{
			"name": "create-mc1.18.2_v0.5.0c.jar",
			"size": 14509736,
			"sha1": "4b01df584e77401ae0b781a8ee03eede5ab31e84",
			"previous": [
				{
					"name": "create-mc1.18.2_v0.5.0b.jar",
					"size": 14509504,
					"sha1": "a29223c76c8201bb6b2a4c4e7068e651824395b8"
				}
			]
		},
		{
			"name": "dyeable_redstone_signal-1.0.3.jar",
			"size": 197083,
			"sha1": "aafd12cfb6e1edad6ddeceed7e579fa0ce42020d",
			"previous": [

			]
		},
		{
			"name": "emeraldcraft-3.2.jar",
			"size": 2204931,
			"sha1": "a759ff2df002d9b2ded6189e134f5eced2e1e517",
			"previous": [

			]
		},
		{
			"name": "flywheel-forge-1.18-0.6.4.jar",
			"size": 1286923,
			"sha1": "217950cfa3f4f5a148021c79569d2ce251b77524",
			"previous": [

			]
		},
		{
			"name": "oceanblender-1.0.1.jar",
			"size": 286004,
			"sha1": "6b4f8a3f5e29631ad001d3ad1fbd08e24e5b5080",
			"previous": [

			]
		},
		{
			"name": "oceanworld-1.0.5.jar",
			"size": 897840,
			"sha1": "3781c371d14529fba28c7bafdf1bdd2fad602450",
			"previous": [

			]
		},
		{
			"name": "Quark-3.2-353.jar",
			"size": 13703382,
			"sha1": "51f509e181ab54b06a7b8a9fc74c5c5c6511804b",
			"previous": [

			]
		},
		{
			"name": "starlight-1.0.2+forge.546ae87.jar",
			"size": 110596,
			"sha1": "c7dde83f5fbc864d85842c74a4433a8a5c0f15b7",
			"previous": [

			]
		},
		{
			"name": "TerraBlender-forge-1.18.2-1.1.0.102.jar",
			"size": 99002,
			"sha1": "da7578a7dacf19e44a3c706e173ba7012b26aec9",
			"previous": [

			]
		}
	]
}
```

And here is the server structure:

```
+ public/
|-- setup.js
|-+ mods/
|-|-- create-mc1.18.2_v0.5.0c.jar
|-|-- dyeable_redstone_signal-1.0.3.jar
|-|-- emeraldcraft-3.2.jar
|-|-- flywheel-forge-1.18-0.6.4.jar
|-|-- oceanblender-1.0.1.jar
|-|-- oceanworld-1.0.5.jar
|-|-- Quark-3.2-353.jar
|-.
.
```

The only two things to do when a new mod version is released are:
1. Upload mod file to mods/ directory.
2. Edit setup.js

For example, if oceanworld-1.0.6.jar were released, you need to upload it to mods/ and edit from:
```javascript
		{
			"name": "oceanworld-1.0.5.jar",
			"size": 897840,
			"sha1": "3781c371d14529fba28c7bafdf1bdd2fad602450",
			"previous": [

			]
		},
```
to:
```javascript
		{
			"name": "oceanworld-1.0.6.jar",
			"size": xxxxx,
			"sha1": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
			"previous": [
          "name": "oceanworld-1.0.5.jar",
			    "size": 897840,
			    "sha1": "3781c371d14529fba28c7bafdf1bdd2fad602450"
			]
		},
```
