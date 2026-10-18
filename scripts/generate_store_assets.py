#!/usr/bin/env python3
"""
Faces — Play Store Asset Generator
Generates:
1. Play Store App Icon (512x512 PNG)
2. Feature Graphic (1024x500 PNG)
3. Wear OS Watch Face Screenshots (454x454 circular PNGs)
"""

import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import numpy as np

os.makedirs('store_assets/graphics', exist_ok=True)
os.makedirs('store_assets/wear', exist_ok=True)
os.makedirs('store_assets/phone', exist_ok=True)

print("--- 1. Generating Play Store App Icon (512x512) ---")
# 1. Load source icon to extract the high-contrast Faces emblem
source_path = 'mobile/src/main/res/drawable-xxhdpi/ic_launcher.png'
im = Image.open(source_path)
arr = np.array(im).astype(float)

# Extract pure logo
crop = arr[41:96, 28:117, :3]
bg = np.array([8.0, 10.0, 13.0])
diff = crop - bg
diff_max = np.array([247.0, 245.0, 242.0])
alpha = np.clip(np.mean(diff / diff_max, axis=2), 0.0, 1.0)

base_logo = Image.new('RGBA', (crop.shape[1], crop.shape[0]), (0, 0, 0, 0))
base_arr = np.array(base_logo)
base_arr[:, :, :3] = 255
base_arr[:, :, 3] = (alpha * 255).astype(np.uint8)
base_logo = Image.fromarray(base_arr)

# Super-sample logo to high resolution
large_logo = base_logo.resize((crop.shape[1] * 12, crop.shape[0] * 12), Image.Resampling.LANCZOS)
logo_aspect = large_logo.size[0] / large_logo.size[1]

# Play Store icon canvas (512 x 512)
icon_512 = Image.new('RGBA', (512, 512), (8, 10, 13, 255))
draw = ImageDraw.Draw(icon_512)

# Subtle luxury radial vignette in background
for r in range(256, 0, -2):
    intensity = int(14 * (1.0 - (r / 256.0)))
    draw.ellipse([256 - r, 256 - r, 256 + r, 256 + r], fill=(8 + intensity, 10 + intensity, 13 + intensity, 255))

# Scale logo to ~290px wide (matching luxury icon proportions)
target_logo_w = 290
target_logo_h = int(target_logo_w / logo_aspect)
scaled_logo = large_logo.resize((target_logo_w, target_logo_h), Image.Resampling.LANCZOS)

paste_x = (512 - target_logo_w) // 2
paste_y = (512 - target_logo_h) // 2

# Subtle glow/shadow under logo
shadow = scaled_logo.filter(ImageFilter.GaussianBlur(8))
shadow_tint = Image.new('RGBA', shadow.size, (0, 0, 0, 120))
icon_512.paste(shadow_tint, (paste_x, paste_y + 6), shadow)

# Paste pure white logo
icon_512.paste(scaled_logo, (paste_x, paste_y), scaled_logo)

icon_path = 'store_assets/graphics/play_store_icon_512.png'
icon_512.save(icon_path, 'PNG')
print(f"✓ Saved {icon_path} (512x512)")


print("\n--- 2. Generating Wear OS Screenshots (454x454) ---")
# Crop circular dial from live captures
wear_shots = [
    ('store_assets/phone/screenshot_1_home.png', 'store_assets/wear/screenshot_wear_1_filling_time.png', 'Filling Time (Active)'),
    ('store_assets/phone/screenshot_2_ambient.png', 'store_assets/wear/screenshot_wear_2_ambient.png', 'Filling Time (Ambient)'),
    ('store_assets/phone/screenshot_3_carousel.png', 'store_assets/wear/screenshot_wear_3_ouat.png', 'Once Upon A Time (Active)')
]

for src_path, out_path, label in wear_shots:
    if os.path.exists(src_path):
        src_im = Image.open(src_path).convert('RGBA')
        # Center of dial is x=540, y=1099, diameter ~604px
        center_x, center_y = 540, 1099
        radius = 300 # 600px diameter
        box = (center_x - radius, center_y - radius, center_x + radius, center_y + radius)
        cropped = src_im.crop(box)
        
        # Resize to standard Wear OS 454x454
        resized = cropped.resize((454, 454), Image.Resampling.LANCZOS)
        
        # Apply circular bezel mask with smooth antialiased outer edge
        mask = Image.new('L', (454 * 2, 454 * 2), 0)
        mask_draw = ImageDraw.Draw(mask)
        mask_draw.ellipse([8, 8, 454 * 2 - 8, 454 * 2 - 8], fill=255)
        mask_smooth = mask.resize((454, 454), Image.Resampling.LANCZOS)
        
        wear_canvas = Image.new('RGBA', (454, 454), (0, 0, 0, 255))
        wear_canvas.paste(resized, (0, 0), mask_smooth)
        wear_canvas.save(out_path, 'PNG')
        print(f"✓ Generated {out_path} ({label}, 454x454)")


print("\n--- 3. Generating Feature Graphic (1024x500) ---")
# Feature Graphic canvas: 1024 x 500, no alpha
feat_img = Image.new('RGB', (1024, 500), (8, 10, 13))
f_draw = ImageDraw.Draw(feat_img)

# Dynamic gradient background: dark navy/slate to deep luxury black
for x in range(1024):
    factor = x / 1024.0
    r = int(8 + 12 * (1.0 - factor) + 10 * factor)
    g = int(10 + 16 * (1.0 - factor) + 14 * factor)
    b = int(13 + 28 * (1.0 - factor) + 22 * factor)
    f_draw.line([(x, 0), (x, 500)], fill=(r, g, b))

# Ambient glow on the right side behind the watch mockup
glow = Image.new('RGBA', (1024, 500), (0, 0, 0, 0))
g_draw = ImageDraw.Draw(glow)
g_draw.ellipse([600, 50, 950, 450], fill=(30, 60, 90, 80))
glow_blurred = glow.filter(ImageFilter.GaussianBlur(60))
feat_img.paste(glow_blurred, (0, 0), glow_blurred)

# Paste watch dial render on the right side (centered at x=780, y=250)
if os.path.exists('store_assets/wear/screenshot_wear_1_filling_time.png'):
    watch_img = Image.open('store_assets/wear/screenshot_wear_1_filling_time.png').convert('RGBA')
    watch_scaled = watch_img.resize((360, 360), Image.Resampling.LANCZOS)
    
    # Outer luxury bezel ring
    bezel = Image.new('RGBA', (380, 380), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(bezel)
    b_draw.ellipse([0, 0, 380, 380], fill=(20, 25, 32, 255), outline=(255, 255, 255, 180), width=3)
    
    watch_pos = (780 - 180, 250 - 180)
    feat_img.paste(bezel, (watch_pos[0] - 10, watch_pos[1] - 10), bezel)
    feat_img.paste(watch_scaled, watch_pos, watch_scaled)

# Add typography on left side
# Title: FACES
title_logo_w = 340
title_logo_h = int(title_logo_w / logo_aspect)
title_logo = large_logo.resize((title_logo_w, title_logo_h), Image.Resampling.LANCZOS)
feat_img.paste(title_logo, (90, 140), title_logo)

# Load fonts if available, or draw crisp subtitle badges
try:
    font_bold = ImageFont.truetype('core/src/main/res/font/quicksand_bold.otf', 22)
    font_sub = ImageFont.truetype('core/src/main/res/font/proximanova_semibold.otf', 17)
    font_badge = ImageFont.truetype('core/src/main/res/font/quicksand_bold.otf', 13)
except Exception:
    font_bold = ImageFont.load_default()
    font_sub = ImageFont.load_default()
    font_badge = ImageFont.load_default()

f_draw.text((94, 250), "Artisanal Watch Face Studio", fill=(255, 255, 255), font=font_bold)
f_draw.text((94, 288), "Photo Palette Extraction • Wear OS Standalone", fill=(160, 170, 185), font=font_sub)

# Badges (Wear OS, Palette API, AndroidX)
badges = ["WEAR OS", "16-COLOR PALETTES", "AMBIENT DISPLAY"]
bx = 94
by = 345
for b_text in badges:
    badge_w = len(b_text) * 9 + 24
    # Pill background
    f_draw.rounded_rectangle([bx, by, bx + badge_w, by + 30], radius=15, fill=(35, 42, 54), outline=(255, 255, 255, 120), width=1)
    f_draw.text((bx + 12, by + 7), b_text, fill=(240, 245, 255), font=font_badge)
    bx += badge_w + 12

feat_path = 'store_assets/graphics/feature_graphic_1024x500.png'
feat_img.save(feat_path, 'PNG')
print(f"✓ Saved {feat_path} (1024x500)")
print("\n--- All store graphics successfully generated! ---")
