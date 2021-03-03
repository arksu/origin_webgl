from collections import defaultdict
from random import randrange
import numpy as np
import threading
import datetime
import noise
import json
import math
import time
import cv2

import warnings
warnings.simplefilter(action='ignore', category=FutureWarning)
warnings.simplefilter(action='ignore', category=RuntimeWarning)

print("Spots generation...")

map_size = {"width":5000, "height":5000}
params = {"max_riv_width": 20, 
                "min_riv_width": 12, 
                "min_sand_width":-60, 
                "max_sand_width":60,
                "lake_deep_width": 0.018,
                "lake_width": 0.029,
                "max_quality": 100,
                "min_quality": 10,
                "min_spot_range": 20,
                "max_spot_range": 80,
                "spots_count": 100
             }

spots_list = [
    "clay_spot",
    "iron_ore_spot",
    "copper_ore_spot",
    "gold_spot",
    "copper_ore_spot",
    "rock_spot",
    "tree_spot",
    "water_well_spot"
]

def colorCheck(c, eq):
    for t in eq:
        equal = np.array_equal(np.array(c), t)
        if equal:
            return True
    return False

def hexColToArr(c):
    return np.array([ int(c[2]),  int(c[1]), int(c[0])])

HEATH =       hexColToArr(b'\x00\x00\x00')
MEADOW_LOW =  hexColToArr(b'\xd9\x7c\xc0')# луг (низкие травы) ok
MEADOW_HIGH = hexColToArr(b'\x8e\xbf\x8e')# луг (высокие травы) ok
FOREST_LEAF = hexColToArr(b'\x17\xd4\x21')# лес лиственный ok
FOREST_PINE = hexColToArr(b'\x31\x61\x17')# лес хвойный ok
CLAY =        hexColToArr(b'\x70\x39\x0f')# глина ok
SAND =        hexColToArr(b'\xe0\xe0\x34')# песок ok
PRAIRIE =     hexColToArr(b'\xf0\xa0\x1f')# степь ok 
SWAMP =       hexColToArr(b'\x1c\x38\x19')# болото ok
TUNDRA =      hexColToArr(b'\x3c\x7a\x6f')# тундра
WATER =       hexColToArr(b'\x00\x55\xff')# мелководье ok 
WATER_DEEP =  hexColToArr(b'\x00\x00\xff')# глубокая вода ok


start_time = time.time()

nmap = np.array(cv2.imread("../backend/map.png"))

spots = defaultdict(list)

for i in range(int(params["spots_count"]/len(spots_list))):
    for j in spots_list:
        
        try_count = 1000
        skip = False
        
        while True:
            x = randrange(0, map_size["width"])
            y = randrange(0, map_size["height"])
            pic_color = nmap[x][y]
            if not colorCheck(pic_color, [WATER_DEEP, WATER, SAND]):
                if j == "clay_spot" and colorCheck(pic_color, [CLAY]):
                    break
                if j == "tree_spot" and colorCheck(pic_color, [FOREST_PINE, FOREST_LEAF]):
                    break
                try_count -= 1
                if try_count == 0:
                    skip = True
                    break
        
        if skip:
            continue
        
        r = randrange(params["min_spot_range"], params["max_spot_range"])
        max_q = randrange(params["min_quality"], params["max_quality"])
        
        spots[j].append({"x":x, "y":y, "r":r, "max_quality":max_q})
        
now = datetime.datetime.now()
now_dat = str(now.strftime("%Y-%m-%d_%H-%M-%S"))

with open(f"../backend/spots.json", 'w') as outfile:
    json.dump(spots, outfile)
    
print(f"Done! {(time.time() - start_time)} seconds")
