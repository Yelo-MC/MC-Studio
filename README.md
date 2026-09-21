# BlockBeats

BlockBeats is a Fabric mod for Minecraft that adds a compact digital audio workstation (DAW). Compose note-block music in a piano roll, arrange several instruments on the same grid, and burn the finished song onto a playable music disc.

## Features

- A 128-step piano roll with 25 pitches
- All 16 vanilla note-block instruments
- Multiple instruments on the same grid
- Adjustable tempo from 40 to 240 BPM
- A configurable loop point
- Click-and-drag note painting and right-click erasing
- Song naming and recording onto a Blank Disc
- Burned Discs that retain their notes, tempo, creator, song name, and a randomly colored center
- Burned Disc playback in vanilla jukeboxes
- A dedicated BlockBeats creative tab

## Using the DAW

1. Place a DAW Block and right-click it.
2. Choose an instrument from the panel on the left.
3. Left-click or hold and drag to place notes. Right-click or hold and drag to erase them.
4. Use the piano keys to preview pitches and the BPM slider to set the tempo.
5. Enable loop mode, then click a column to set or remove the loop point.
6. Enter a song name, place a Blank Disc in the recording slot, and wait for the progress arrow to fill.
7. Take the resulting Burned Disc and play it in any jukebox.

While composing, previews and DAW playback are local to the player using the interface. Burned Discs played in a jukebox can be heard by nearby players.

## Controls

| Action | Default control |
| --- | --- |
| Place notes | Left mouse button |
| Erase notes | Right mouse button |
| Play / pause | Space |
| Stop | Backspace |
| Toggle loop mode | L |

The keyboard controls are available under the **BlockBeats** category in Minecraft's keybind settings.

## Recipes

### DAW Block

Eight planks surrounding one quartz:

```text
PPP
PQP
PPP
```

### Blank Disc

Four coal surrounding one iron ingot:

```text
 C 
CIC
 C 
```

## Requirements

- Minecraft 1.21.11
- Java 21 or newer
- Fabric Loader 0.18.4 or newer
- Fabric API
- owo-lib 0.13.0 for Minecraft 1.21.11

BlockBeats, Fabric API, and owo-lib are required on both clients and dedicated servers.

## Building

Clone the repository and run:

```shell
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

The built mod JAR will be placed in `build/libs`.

## License

BlockBeats is available under the [MIT License](LICENSE).
