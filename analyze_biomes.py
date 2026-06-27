import json

# Load the file
with open(r'src\main\resources\data\minecraft\datapacks\Sengoku Biome Sorter\data\minecraft\dimension\overworld.json', 'r') as f:
    data = json.load(f)

# Define seasonal categories
verdant = ['minecraft:forest', 'minecraft:plains', 'minecraft:sunflower_plains', 'minecraft:flower_forest', 'minecraft:cherry_grove', 'minecraft:windswept_savanna', 'minecraft:jungle', 'minecraft:dark_forest', 'minecraft:stony_shore', 'minecraft:bamboo_jungle', 'minecraft:swamp', 'minecraft:meadow']

autumnal = ['minecraft:badlands', 'minecraft:savanna', 'minecraft:mangrove_swamp', 'minecraft:wooded_badlands', 'minecraft:taiga', 'minecraft:old_growth_birch_forest', 'minecraft:savanna_plateau', 'minecraft:sparse_jungle', 'minecraft:warm_ocean', 'minecraft:eroded_badlands', 'minecraft:old_growth_pine_taiga', 'minecraft:old_growth_spruce_taiga']

wintry = ['minecraft:snowy_plains', 'minecraft:snowy_taiga', 'minecraft:windswept_hills', 'minecraft:windswept_forest', 'minecraft:ice_spikes', 'minecraft:grove', 'minecraft:frozen_ocean', 'minecraft:deep_frozen_ocean']

# Analyze biome placements
print("=== BIOME ANALYSIS ===\n")
print("Temperature Ranges: Low (-1 to -0.45) = WINTRY, Mid (-0.45 to 0.2) = VERDANT, High (0.2+) = AUTUMNAL\n")
print("Humidity Ranges: Low (-1 to -0.35) = AUTUMNAL, Mid (-0.35 to 0.1) = VERDANT/AUTUMNAL, High (0.1+) = VERDANT\n")

issues = []

for biome_entry in data['generator']['biome_source']['biomes']:
    biome = biome_entry['biome']
    temp = biome_entry['parameters']['temperature']
    humidity = biome_entry['parameters']['humidity']
    
    # Only check biomes in our lists
    if biome not in verdant + autumnal + wintry:
        continue
    
    category = 'VERDANT' if biome in verdant else ('AUTUMNAL' if biome in autumnal else 'WINTRY')
    
    # Check if placement matches category
    temp_mid = (temp[0] + temp[1]) / 2
    humidity_mid = (humidity[0] + humidity[1]) / 2
    
    issue = False
    reason = ""
    
    if category == 'WINTRY':
        if temp_mid > -0.45:
            issue = True
            reason = f"Temperature {temp_mid:.2f} is not WINTRY (should be < -0.45)"
    elif category == 'VERDANT':
        # Verdant should be mid-range temperature and mid-to-high humidity
        if temp_mid < -0.45 or temp_mid > 0.2:
            issue = True
            reason = f"Temperature {temp_mid:.2f} not in VERDANT range (-0.45 to 0.2)"
        if humidity_mid < -0.35:
            issue = True
            reason = f"Humidity {humidity_mid:.2f} is too low for VERDANT (should be > -0.35)"
    elif category == 'AUTUMNAL':
        # Autumnal should be low-to-mid temperature and low humidity
        if temp_mid < -0.45 or humidity_mid > 0.3:
            issue = True
            reason = f"Temp/Humidity out of range. Temp: {temp_mid:.2f}, Humidity: {humidity_mid:.2f}"
    
    if issue:
        issues.append((biome, category, reason, temp, humidity))

if issues:
    print("POTENTIAL ISSUES FOUND:\n")
    for biome, cat, reason, temp, hum in sorted(set(issues)):
        print(f"{cat:10} {biome}")
        print(f"  Temp: [{temp[0]}, {temp[1]}], Humidity: [{hum[0]}, {hum[1]}]")
        print(f"  Issue: {reason}\n")
else:
    print("No issues found!\n")
