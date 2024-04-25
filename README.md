# Minecraft Coordinates Manager

Lost in the vast world of Minecraft? Can’t keep track of all those important places? Say goodbye to confusion and hello to clarity with the **Minecraft Coordinates Manager**! Your ultimate companion for a seamless Minecraft adventure!

## Features 🎮

The **Minecraft Coordinates Manager** is a client-server application, not a mod, designed to enhance your Minecraft gaming experience. It consists of a client and a server that work together to provide the following features:

- **Automatic Coordinate Capture (Windows only)** 📍: The program automatically captures your current in-game coordinates and takes a screenshot when you press the Insert key. For Linux users, you'll need to manually copy the coordinates, and the program will retrieve them from your clipboard when you open the window to save new coordinates.

- **Teleportation Command (Windows only)** 🚀: The program automatically switches to the game window and enters the teleportation command to the selected coordinates. For Linux users, the program will write the teleportation command to your clipboard.

- **Coordinate Storage** 🗃️: The program allows you to store information about coordinates, including the name and an in-game screenshot.

- **Synchronization** 🔄: The program synchronizes coordinate information between all clients connected to the server.

- **Cross-Platform Compatibility** 💻🐧: Both the client and server work on Linux and Windows.

## Tested Platforms 🧪

The program has been tested on the following platforms:

- Windows 11 🪟
- Windows 10 🪟
- Ubuntu 22 🐧

## Images of GUI
![Photo of main menu](/images/mainmenu.png "Main menu")
![Photo of cords add menu](/images/savecordform.png "Adding cords menu")

## Video
[![](https://markdown-videos-api.jorgenkh.no/youtube/ztlR-YaVwKA)](https://youtu.be/ztlR-YaVwKA)


# How to run Minecraft Coordinates Manager (Client)

The **Minecraft Coordinates Manager** requires Java 17 (OpenJDK 17) or higher to run. Here's how you can check if you have the correct version of Java installed and how to install it if you don't:

## Checking Your Java Version

Open a terminal or command prompt and type the following command:

```bash
java -version
```

If Java is installed on your system, you should see an output similar to this:

```bash
openjdk version "17.0.2" 2022-01-18
OpenJDK Runtime Environment (build 17.0.2+8-86)
OpenJDK 64-Bit Server VM (build 17.0.2+8-86, mixed mode, sharing)
```

The first line of the output indicates the version of the Java installed on your system. If it says "17" or higher, you're good to go!

## Installing Java 17 or Higher

If you don't have Java installed or if you have a version lower than 17, here's how you can install OpenJDK 17 or higher:

### On Windows

1. Download the OpenJDK 17 (or higher) Windows installer from the [official Oracle website](https://www.oracle.com/java/technologies/downloads/).
2. Run the installer and follow the instructions to install Java.

### On Linux

The process to install OpenJDK 17 or higher on Linux depends on the distribution you're using. Here's how you can do it on Ubuntu:

1. Update the package index:

```bash
sudo apt update
```

2. Install the OpenJDK 17 (or higher) package:

```bash
sudo apt install openjdk-17-jdk
```

3. Verify the installation:

```bash
java -version
```

You should now see "17" or higher as your Java version. If you're using a different Linux distribution, please refer to its documentation for instructions on how to install OpenJDK 17 or higher.

4. Install the `shutter` screenshot tool:

```bash
sudo apt-get install shutter
```

## Running Minecraft Coordinates Manager (Client)

Once you have Java 17 or higher and `shutter` (Linux only) installed, you can proceed with the installation of the Minecraft Coordinates Manager:

1. Go to the "Release" section of the project repository.
2. Download the `mcmClient.jar` file.
3. Open a terminal or command prompt in the directory where you downloaded the file.
4. Run the program using the following command:

```bash
java -jar mcmClient.jar
```



# How to run Minecraft Coordinates Manager (Client)

The server component of the **Minecraft Coordinates Manager** also requires Java 17 (OpenJDK 17) or higher to run. Here's how you can set up the server on your Ubuntu VPS:

## Installing Java 17 or Higher

If you haven't already installed Java on your VPS, here's how you can install OpenJDK 17 or higher:

1. Update the package index:

```bash
sudo apt update
```

2. Install the OpenJDK 17 (or higher) package:

```bash
sudo apt install openjdk-17-jdk
```

3. Verify the installation:

```bash
java -version
```

You should now see "17" or higher as your Java version.

## Running the Server as a Service

To ensure that the server runs continuously, you can set it up as a service using `systemctl`. Here's how you can do it:

1. Create a new service file in the `/etc/systemd/system` directory. You can name it `mcmServer.service`:

```bash
sudo nano /etc/systemd/system/mcmServer.service
```

2. In the service file, add the following content:

```bash
[Unit]
Description=Minecraft Coordinates Manager Server
After=network.target

[Service]
User=root
WorkingDirectory=/path/to/your/server/directory
ExecStart=/usr/bin/java -jar mcmServer.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

Replace `/path/to/your/server/directory` with the actual path to your server directory and `mcmServer.jar` with the actual name of your server JAR file.

3. Save and close the file.

4. Enable the service to start on boot:

```bash
sudo systemctl enable mcmServer
```

5. Start the service:

```bash
sudo systemctl start mcmServer
```

6. Check the status of the service:

```bash
sudo systemctl status mcmServer
```

The server should now be running on 8080 port as a service and will automatically start whenever your VPS reboots.

Enjoy managing your Minecraft coordinates! 🎮
