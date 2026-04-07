#!/usr/bin/env python3
from PIL import Image, ImageDraw
import os

# Create a simple blue square icon
sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

for density, size in sizes.items():
    img = Image.new('RGB', (size, size), color='#2196F3')
    draw = ImageDraw.Draw(img)
    
    # Draw a simple 'F' shape
    draw.rectangle([size*0.2, size*0.1, size*0.4, size*0.9], fill='#FFFFFF')
    draw.rectangle([size*0.2, size*0.1, size*0.8, size*0.3], fill='#FFFFFF')
    draw.rectangle([size*0.2, size*0.5, size*0.6, size*0.7], fill='#FFFFFF')
    
    img.save(f'app/src/main/res/{density}/ic_launcher.png')
    img.save(f'app/src/main/res/{density}/ic_launcher_round.png')
    print(f'Created {density} icons')

print('All icons created!')
