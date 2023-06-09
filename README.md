# TouchPortalLogiGHub Plugin

## ONLY LOGITECH LITRA GLOW IS CURRENTLY SUPPORTED

### This plugin is for Logitech G Hub

### Logitech G Hub needs to be open for plug in to work
- I made the plugin wait for G Hub to be open and initialize itself before getting settings.
- Also, the plugin actions should not work when G Hub is closed and should start to work again after G Hub is opened again and initialized.


### Actions:
- Power device on/off
  - Powers device on/off, or you can toggle between the two.
  - Select device, profile and app you want to power on/off.
  - If a different profile is active for that app then it will not change the device being on/off.
- Switch Profile
  - Switches active profile for a specific app.
  - Select profile and app you want to change.
- Set Brightness
  - Change brightness of selected profile.
  - Value is from 0-100 and should match percent in G Hub.
    - Sometimes it will not be exact because to tell G Hub to update it I have to give it a number between 20-250, so it will convert that number from your percent, so if it is off by 1% sometimes, that is normal.
  - Select device, profile and app you want to change brightness of.
  - If a different profile is active for that app then it will not change the device being on/off.
- Set Temperature
  - Change temperature of selected profile.
    - Value is from 2700-6500. 
    - If you set value to below 2700 then it will automatically be 2700.
    - Rounds to nearest 100, so if you select 3049 it will round to 3000 and if you select 3051 it will round to 3100.
  - Select device, profile and app you want to change temperature of.
  - If a different profile is active for that app then it will not change the device being on/off.
- Set Preset
  - Select device, profile and app you want to enable a preset for.
  - It should get all presets in G Hub even user created one, but I have seen it not get all of them for some reason, looking into why.
    - If needed I would recommend to just create a button that will set both temp and brightness at the same time if you want a preset and it does not load correctly.

### Connectors
- Profile brightness
  - See above about brightness action, does basically the same thing, but you change it with a slider.
- Profile temperature
  - See above about temperature action, does basically the same thing, but you change it with a slider.

## FAQ
- since I do not have any other logitech products I cannot add support for them.
  - I hope to be able to add more support, but I have no timetable with that being possible.
- also unsure if there will be issues if you use two Litra glow devices, as I can only test with one
- unsure if this would work with Litra beam.
- In the future I may add plug in states to track everything, but is not in initial release.
- I advise to make sure all profiles and apps have separate names. If you have multiple profiles with the same name that go to the same app, then it will cause issues.

## Future enhancements
- I want to sort every list in the actions and connectors.
- Seems like sometimes it duplicates the "Manual Adjustment" in G Hub and i do not know why it does that, or how to get rid of it currently?