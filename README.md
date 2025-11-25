/**
# Altimeter – Pressure-Based Altitude Calculator

This project demonstrates how to build a **real-time altimeter app** in **Jetpack Compose (Material 3)**  
using the device’s **pressure sensor** and a **simulated pressure mode** for testing.

---

## Features

- **Live pressure sensor reading** (hPa)
- **Altitude calculation** using the barometric formula  
  `h = 44330 × (1 - (P/P0)^(1/5.255))`
- **Dynamic background color**  
  Higher altitude → darker background
- **Simulation buttons**  
  Useful for laptops/emulators without real pressure sensors.
  When in simulated state, won't be overwritten by the sensor until you stop the simulation.
---

Uses:
- Jetpack Compose Material 3
- Kotlin StateFlow
- Android SensorManager


---
*/
