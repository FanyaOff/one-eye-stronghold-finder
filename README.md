# One eye stronghold finder
A client-side fabric mod which allows you to find stronghold with only one Eye of Ender.

Fork of [Hopper4et Mod](https://github.com/Hopper4et/one-eye-stronghold-finder)

# Fixes
- 1.21.x support
- Displaying coordinats in the nether world


## Overview

This tool leverages the behavior of the Eye of Ender in Minecraft Java Edition to locate strongholds efficiently with only one throw of the Eye of Ender. Strongholds in Minecraft generate in rings around the world origin (X=0, Z=0). The locations follow a specific pattern, with a fixed number of strongholds per ring and distributed roughly evenly around the ring.

### Stronghold Generation Rings

There are 8 rings containing a total of 128 strongholds. Below is a breakdown of the stronghold distribution:

| Ring | Number of Strongholds | Distance from Origin (blocks) |
| ---- | --------------------- | ----------------------------- |
| 1    | 3                     | 1,280–2,816                   |
| 2    | 6                     | 4,352–5,888                   |
| 3    | 10                    | 7,424–8,960                   |
| 4    | 15                    | 10,496–12,032                 |
| 5    | 21                    | 13,568–15,104                 |
| 6    | 28                    | 16,640–18,176                 |
| 7    | 36                    | 19,712–21,248                 |
| 8    | 9                     | 22,784–24,320                 |

### Key Stronghold Properties

1. Strongholds are placed at equal angles from the origin within each ring.
2. Strongholds do not generate partially above ground. If any part would extend above sea level, it is replaced with air.
3. Occasionally, strongholds generate in oceans, usually covered by terrain.

## How It Works

This implementation calculates the intersection of the Eye of Ender’s trajectory with the stronghold rings using simple geometric principles. Here is how the process works:

1. **Recording Eye Movement:**

   - The program captures two positions (`pos1` and `pos2`) of the Eye of Ender during its flight. These positions are recorded with a delay to ensure that the trajectory is properly traced.
   - If the Eye travels strictly along one axis (indicating a degenerate case), the calculation is aborted.

2. **Adjusting Coordinates:**

   - To simplify calculations, the coordinate system may be swapped (X becomes Z, and vice versa) depending on which axis the Eye traveled farther along. This ensures the slope of the line remains manageable.

3. **Calculating the Trajectory:**

   - Using the two recorded points, the slope of the trajectory is determined:



   - This slope is used to predict where the trajectory intersects with the Minecraft world grid.

4. **Finding the Ring:**

   - The tool calculates the radial distance of each potential intersection point from the origin
     
   - It checks whether the distance falls within one of the predefined stronghold rings. The distance ranges for each ring are carefully defined.

5. **Aligning to Minecraft Grid:**

   - Since strongholds are aligned to a 16x16 grid, the calculated coordinates are snapped to the nearest grid point

6. **Evaluating Accuracy:**

   - For each potential stronghold location, an accuracy score is calculated based on how closely the Eye’s trajectory aligns with the grid point. Higher accuracy indicates a higher likelihood of a stronghold being present.

7. **Sorting and Displaying Results:**

   - The program ranks the identified stronghold locations by accuracy. Up to 5 of the best candidates are displayed in the in-game chat, showing:
     - Overworld coordinates (X, Z)
     - Nether coordinates (X/8, Z/8)
     - Accuracy score

## Example Output

Upon running the program, the following information is displayed in the in-game chat:

```
=== One Eye Stronghold Finder ===
X: 1234   Z: 5678   (Nether: X: 154   Z: 710)   accuracy: 95
X: 1240   Z: 5680   (Nether: X: 155   Z: 710)   accuracy: 90
```

- **Overworld Coordinates:** X: 1234, Z: 5678
- **Nether Coordinates:** X: 154, Z: 710
- **Accuracy:** A numerical value indicating the likelihood of a stronghold being at this location.

## Usage

1. Equip an Eye of Ender.
2. Throw the Eye and wait for the results to appear in the chat.
3. Travel to the provided coordinates to locate the stronghold.
4. 
![demo](https://github.com/user-attachments/assets/1e0b2afb-9917-4131-83bc-c7aefae849a4)

