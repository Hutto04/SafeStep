import numpy as np
import matplotlib.pyplot as plt
from PIL import Image, ImageFilter, ImageDraw
from os.path import dirname, join
import io

# Define constants for pressure point positions (y, x)
CALCANEUS_LEFT = (300, 95)
LATERAL_LEFT = (200, 80)
MTK_1_LEFT = (100, 140)
MTK_2_LEFT = (120, 130)
MTK_3_LEFT = (120, 100)
MTK_4_LEFT = (120, 90)
MTK_5_LEFT = (120, 60)
D1_LEFT = (60, 120)

CALCANEUS_RIGHT = (300, 90)
LATERAL_RIGHT = (200, 110)
MTK_1_RIGHT = (100, 50)
MTK_2_RIGHT = (120, 65)
MTK_3_RIGHT = (120, 90)
MTK_4_RIGHT = (120, 100)
MTK_5_RIGHT = (120, 125)
D1_RIGHT = (60, 60)

# Define constants for the sensor and blur radius
SENSOR_RADIUS = 10  # Radius around the sensor point to affect
BLUR_RADIUS = 10    # Smaller blur radius

def get_asset_image(file_name):
    # Build the full path to the image file
    filename = join(dirname(__file__), file_name)
    # Open the image using PIL
    image = Image.open(filename)
    return np.array(image)  # Convert PIL Image to NumPy array to use shape

def generate_heatmap(calcaneus_left, lateral_left, mtk_1_left, mtk_2_left, mtk_3_left, mtk_4_left, mtk_5_left, d1_left,
                     calcaneus_right, lateral_right, mtk_1_right, mtk_2_right, mtk_3_right, mtk_4_right, mtk_5_right,
                     d1_right):
    
    # Generate paths for the image files
    left_foot_outline = get_asset_image('left_foot_outline.png')
    right_foot_outline = get_asset_image('right_foot_outline.png')

    # List of sensor values for left and right foot
    left_values = [calcaneus_left, lateral_left, mtk_1_left, mtk_2_left, mtk_3_left, mtk_4_left, mtk_5_left, d1_left]
    right_values = [calcaneus_right, lateral_right, mtk_1_right, mtk_2_right, mtk_3_right, mtk_4_right, mtk_5_right, d1_right]

    # Define sensor positions on the foot using the predefined constants
    sensor_positions_left = [CALCANEUS_LEFT, LATERAL_LEFT, MTK_1_LEFT, MTK_2_LEFT, MTK_3_LEFT, MTK_4_LEFT, MTK_5_LEFT, D1_LEFT]
    sensor_positions_right = [CALCANEUS_RIGHT, LATERAL_RIGHT, MTK_1_RIGHT, MTK_2_RIGHT, MTK_3_RIGHT, MTK_4_RIGHT, MTK_5_RIGHT, D1_RIGHT]

    # Image setup
    left_img = Image.new('L', (left_foot_outline.shape[1], left_foot_outline.shape[0]), 0)
    right_img = Image.new('L', (right_foot_outline.shape[1], right_foot_outline.shape[0]), 0)
    draw_left = ImageDraw.Draw(left_img)
    draw_right = ImageDraw.Draw(right_img)

    # Draw sensors
    for value, pos in zip(left_values, sensor_positions_left):
        if value > 0:  # Only draw if there's a value
            draw_left.ellipse(
                (pos[1] - SENSOR_RADIUS, pos[0] - SENSOR_RADIUS, pos[1] + SENSOR_RADIUS, pos[0] + SENSOR_RADIUS),
                fill=int(value * 255))
    for value, pos in zip(right_values, sensor_positions_right):
        if value > 0:
            draw_right.ellipse(
                (pos[1] - SENSOR_RADIUS, pos[0] - SENSOR_RADIUS, pos[1] + SENSOR_RADIUS, pos[0] + SENSOR_RADIUS),
                fill=int(value * 255))

    # Apply Gaussian blur
    left_img = left_img.filter(ImageFilter.GaussianBlur(BLUR_RADIUS))
    right_img = right_img.filter(ImageFilter.GaussianBlur(BLUR_RADIUS))

    # Convert to array and mask
    left_array = np.array(left_img)
    right_array = np.array(right_img)
    left_mask = np.ma.masked_where(left_foot_outline[..., -1] == 0, left_array)
    right_mask = np.ma.masked_where(right_foot_outline[..., -1] == 0, right_array)

    # Plotting
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(10, 5))
    ax1.imshow(left_foot_outline, cmap='gray', alpha=0.3)
    ax2.imshow(right_foot_outline, cmap='gray', alpha=0.3)
    ax1.imshow(left_mask, cmap='hot', interpolation='nearest')
    ax2.imshow(right_mask, cmap='hot', interpolation='nearest')
    ax1.axis('off')
    ax2.axis('off')
    plt.tight_layout()
    #plt.show()
    
    plt.tight_layout()
    
    # Save the plot to a bytes object and return it
    buf = io.BytesIO()
    plt.savefig(buf, format='png')
    plt.close(fig)  # Close the figure to free memory
    buf.seek(0)
    image_bytes = buf.read()
    buf.close()
    
    return image_bytes

