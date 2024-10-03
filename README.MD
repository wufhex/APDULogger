# APDULogger
A JavaCard applet that logs (almost) all APDU commands sent from a device or reader to a card. Useful for debugging or analyzing how a device interacts with the card.

## Build Process
This project uses [ant-javacard](https://github.com/martinpaljak/ant-javacard) to build CAP files and depends on the JavaCard SDK.

To build it, follow these steps:
1. Clone the repository:
   ```
   git clone https://github.com/wufhex/APDULogger/ --recursive
   ```
2. Install Apache Ant and JDK 17.
3. Run `ant` to compile the CAP file.

## Installation
Install the applet on your card using [GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro):

```
gp --install ApduLogger.cap --default
```

## Usage
### Activating the Applet
After installation, the logger won't start working until you allocate a buffer. To do this, send the following APDU command:

```
EEA0000002{SIZE_IN_BYTES_HEX}04
```

For example:
```
EEA0000002080004
```
In this example, the logger initializes a 256-byte (0800 in hex) array in the card’s persistent memory (EEPROM). After that, it will start logging all incoming APDU commands (except for some reserved ones), responding with `90 00` (Success).

### Reading the Logs
You can read the logged commands from the buffer using:
```
EEF0{OFFSET}
```

For example:
```
EEF00000
```

The offset is a 2-byte hex number that tells the applet where to split the buffer into 256-byte chunks. Offset `0` returns the first 256 bytes, offset `1` returns the next 256 bytes, and so on.

Each APDU command is separated by a 2-byte delimiter (`0xDE 0xAD`).

Example output:
```
> EEF00000
< 80 0E 00 00 DE AD 00 A4 00 00 0C 02 3F 00 DE AD 00 00 ...
```

If a received command is larger than ¾ of the buffer size, the logger will store `0xCL 0xAC` instead.

### Clearing the Buffer
To wipe the data in the buffer, use:
```
EEF10000
```

This command clears the buffer’s contents but does not free the memory. To free up space, you would need to remove the applet.

### Deactivating the Applet
Currently, there's no built-in command to stop logging. However, you can lock the applet using GlobalPlatformPro:

```
gp --lock-applet AC76A2CCBCFB229DAAF3CC
```

An update adding a feature to stop the logger will be available soon.

## Donating
I work on all my project for fun, but i'd be grateful if you could buy me a coffee!\
\
<a href='https://ko-fi.com/D1D3NTABI' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi2.png?v=3' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>