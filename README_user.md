# TorsoT-1 Bitwig Extension

A Bitwig Studio controller extension for the Torso T-1. It maps T-1 CCs to Bitwig track remote controls and forwards transport MMC commands via Sysex.

## Features
- CC 70-83 map to 8 remote controls per track
- MIDI channels 1-16 map to tracks 1-16
- MMC transport: rewind, fast-forward, stop, play, record
- Optional debug logging and relative CC mode in code

## Setup
1. Connect the Torso T-1 and make sure Bitwig sees the MIDI ports.
2. Install the provided `.bwextension` into your Bitwig Extensions folder.
3. In Bitwig, add the controller: Settings -> Controllers -> Add Controller -> Per-Sonal -> TorsoT1.

## Preferences: Selected Device + track remotes + Project Remotes
- In Bitwig Preferences of the extension, select what kind of remotes you want to add per channel
- 'Selected device' / track remotes / project remotes

## OSC / BitX
- In the BitX extension settings, set the OSC IP and port to whatever you want to use.
- Make sure the IP/port you choose match the OSC sender/receiver on the other side.

