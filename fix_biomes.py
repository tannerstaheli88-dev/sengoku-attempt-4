import json

# Load the file
with open(r'src\main\resources\data\minecraft\datapacks\Sengoku Biome Sorter\data\minecraft\dimension\overworld.json', 'r') as f:
    data = json.load(f)

# Define seasonal categories
verdant = ['minecraft:forest', 'minecraft:plains', 'minecraft:sunflower_plains', 'minecraft:flower_forest', 'minecraft:cherry_grove', 'minecraft:windswept_savanna', 'minecraft:jungle', 'minecraft:dark_forest', 'minecraft:stony_shore', 'minecraft:bamboo_jungle', 'minecraft:swamp', 'minecraft:meadow']

autumnal = ['minecraft:badlands', 'minecraft:savanna', 'minecraft:mangrove_swamp', 'minecraft:wooded_badlands', 'minecraft:taiga', 'minecraft:old_growth_birch_forest', 'minecraft:savanna_plateau', 'minecraft:sparse_jungle', 'minecraft:warm_ocean', 'minecraft:eroded_badlands', 'minecraft:old_growth_pine_taiga', 'minecraft:old_growth_spruce_taiga']

wintry = ['minecraft:snowy_plains', 'minecraft:snowy_taiga', 'minecraft:windswept_hills', 'minecraft:windswept_forest', 'minecraft:ice_spikes', 'minecraft:grove', 'minecraft:frozen_ocean', 'minecraft:deep_frozen_ocean']

# Modify biomes to match seasonal categories
for biome_entry in data['generator']['biome_source']['biomes']:
    biome = biome_entry['biome']
    temp = biome_entry['parameters']['temperature']
    humidity = biome_entry['parameters']['humidity']
    
    # Only process biomes in our lists
    if biome not in verdant + autumnal + wintry:
        continue
    
    category = 'VERDANT' if biome in verdant else ('AUTUMNAL' if biome in autumnal else 'WINTRY')
    temp_mid = (temp[0] + temp[1]) / 2
    humidity_mid = (humidity[0] + humidity[1]) / 2
    
    # Check and fix misplacements
    replacements_made = []
    
    if category == 'WINTRY':
        # Wintry should be in low temperature zone
        if temp_mid > -0.45:
            biome_entry['biome'] = 'minecraft:grove'  # Replace with wintry placeholder
            replacements_made.append(f"Temp too high: {temp_mid:.2f}")
    
    elif category == 'VERDANT':
        # Verdant needs mid temp and higher humidity
        if temp_mid < -0.45 or temp_mid > 0.2:
            biome_entry['biome'] = 'minecraft:forest'  # Replace with verdant placeholder
            replacements_made.append(f"Temp out of range: {temp_mid:.2f}")
        elif humidity_mid < -0.35:
            biome_entry['biome'] = 'minecraft:forest'  # Replace with verdant placeholder
            replacements_made.append(f"Humidity too low: {humidity_mid:.2f}")
    
    elif category == 'AUTUMNAL':
        # Autumnal needs mid-to-high temp and low humidity
        if temp_mid < -0.45:
            biome_entry['biome'] = 'minecraft:savanna'  # Replace with autumnal placeholder
            replacements_made.append(f"Temp too low: {temp_mid:.2f}")
        elif humidity_mid > 0.3:
            biome_entry['biome'] = 'minecraft:savanna'  # Replace with autumnal placeholder
            replacements_made.append(f"Humidity too high: {humidity_mid:.2f}")

# Save the modified file
with open(r'src\main\resources\data\minecraft\datapacks\Sengoku Biome Sorter\data\minecraft\dimension\overworld.json', 'w') as f:
    json.dump(data, f, indent=2)

print("File updated successfully!")
