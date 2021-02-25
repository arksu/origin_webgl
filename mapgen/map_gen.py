from skimage.draw import line, polygon, circle, ellipse
from scipy.spatial import Voronoi, voronoi_plot_2d
from collections import defaultdict
import matplotlib.pyplot as plt
from random import randrange
import numpy as np
import threading
import datetime
import noise
import json
import math
import time
import cv2

map_size = {"width":5000, "height":5000}
params = {"max_riv_width": 20, 
                "min_riv_width": 12, 
                "min_sand_width":-60, 
                "max_sand_width":60,
                "lake_deep_width":0.08,
                "lake_width":0.085,
                "forest_leaf": 0.45,
                "meadow_low": 0.44,
                "meadow_high": 0.41,
                "prairie": 0.56,
                "clay": 0.12,
                "swamp": 0.38,
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

start_time = time.time()

spots = defaultdict(list)
points = []

for x in range(-1, 7):
    for y in range(-1, 7):
        points.append([x*500 + randrange(0, 10)/0.005, y*500 + randrange(0, 10)/0.005])

points = np.array(points)
vor = Voronoi(points)
# fig = voronoi_plot_2d(vor)
# plt.show()

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
 
def vecs_len(v1, v2):
    return math.sqrt((v1[0] - v2[0])**2 + (v1[1] - v2[1])**2)

def vec_len(vec):
    return math.sqrt(vec[0]**2 + vec[1]**2)

def norm(vec):
    l = vec_len(vec)
    return [vec[0]/l, vec[1]/l]

def ifInRect(vec, map_size):
    if vec[0] <= 0 or vec[1] <= 0 or vec[0] >= map_size["width"] or vec[1] >= map_size["height"]:
        return False
    else:
        return True
    
def rotate(vec, ang):
    ang_rad = math.radians(ang)
    return [ vec[0] * math.cos(ang_rad) - vec[1] * math.sin(ang_rad), vec[0] * math.sin(ang_rad) + vec[1] * math.cos(ang_rad)]

def lerp(valFrom, valTo, f):
    return (valFrom * (1.0 - f)) + (valTo * f)

def getAngRange():
    return randrange(-38, 38)

def getWidthRange(params):
    return randrange(params["min_riv_width"], params["max_riv_width"])

def getSandRange(params):
    return randrange(params["min_sand_width"], params["max_sand_width"])/4.0

def drawPointToMap(nmap, point, c):
    nmap[int(point[0])][int(point[1])] = [c[0], c[1], c[2]]
    
def colorCheck(c, eq):
    for t in eq:
        equal = np.array_equal(np.array(c), t)
        if equal:
            return True
    return False
    
def draw(nmap, point, w, map_size, col):

    rr, cc = circle(point[0], point[1], int(w), nmap.shape)
    nmap[rr,cc,:] = col
                        
                        
nmap = np.zeros(shape=(map_size["width"],map_size["height"], 3))

draw_base = []

for x in range(map_size["width"]):
    for y in range(map_size["height"]):
        nmap[x][y] = FOREST_PINE
        

local_points = []
isRandDrop = True

for vpair in vor.ridge_vertices:
    
    if vpair[0] < 0.0 or vpair[1] < 0.0:
        continue
    
    v0 = vor.vertices[vpair[0]]
    v1 = vor.vertices[vpair[1]]
    
    if ifInRect(v0, map_size) or ifInRect(v1, map_size):
        if not isRandDrop:
            local_points.append((v0, v1))
        elif randrange(0,10) > 2:
            local_points.append((v0, v1))
            
# print(len(local_points))
            
for point in local_points:
    
    p1 = point[0]
    p2 = point[1]
    
    nvec = norm([p2[0] - p1[0], p2[1] - p1[1]])
    
    old_point = p1 
    
    widthFrom = getWidthRange(params)
    widthTo = getWidthRange(params)

    angFrom = getAngRange()
    angTo = getAngRange()
    
    sandFrom = getSandRange(params)
    sandTo = getSandRange(params)
    
    lerpInt = 70
    itr = 0.0

    min_len = vec_len([p2[0] - p1[0], p2[1] - p1[1]])

    for l in range(map_size["width"]):
        if l % lerpInt == 0 and l != 0:
            
            widthFrom = widthTo
            widthTo = getWidthRange(params)
            
            angFrom = angTo
            angTo = getAngRange()
            
            sandFrom = sandTo
            sandTo = getSandRange(params)

            itr = 0.0

        itr += 1.0
        
        if l > min_len/1.5:
            mrvec = rotate(nvec, lerp(angFrom, angTo, itr / float(lerpInt) ))
            rvec = norm([ lerp( p2[0] - old_point[0], mrvec[0], itr / float(lerpInt)) , lerp( p2[1] - old_point[1], mrvec[1], itr / float(lerpInt))])
        else:
            rvec = rotate(nvec, lerp(angFrom, angTo, itr / float(lerpInt) ))

        old_point = [old_point[0] + rvec[0], old_point[1] + rvec[1]]
        
        draw_base.append((old_point, lerp(widthFrom, widthTo, itr / float(lerpInt) ), map_size, lerp(sandFrom, sandTo, itr / float(lerpInt) )))
        
        if vecs_len(old_point, p2) <= 1:
            break

            
for db in draw_base:
    draw(nmap, db[0], db[3], db[2], SAND)
    
for db in draw_base:
    draw(nmap, db[0], db[1]/2, db[2], WATER)
    
for db in draw_base:
    draw(nmap, db[0], db[1]/3, db[2], WATER_DEEP)
    
scale = randrange(700, 1450)
octaves = randrange(10, 30)
persistence = randrange(15, 25) / 100.0
lacunarity = randrange(15, 20) / 10.0               

world = np.zeros((map_size["width"], map_size["height"]))
for i in range(map_size["width"]):
    for j in range(map_size["height"]):
        world[i][j] = noise.pnoise2(i/scale, 
                                    j/scale, 
                                    octaves=octaves, 
                                    persistence=persistence, 
                                    lacunarity=lacunarity, 
                                    repeatx=1000, 
                                    repeaty=1000, 
                                    base=0)
        
        
for i in range(map_size["width"]):
    for j in range(map_size["height"]):
        if world[i][j] > (0.390 - 0.390 * params["prairie"]):
            pic_color = np.array(nmap[i][j])
            
            if not colorCheck(pic_color, [WATER_DEEP, WATER, SAND]):
                nmap[i][j] = PRAIRIE

        if world[i][j] < -(0.390 - 0.390 * params["meadow_high"]):
            pic_color = np.array(nmap[i][j])

            if not colorCheck(pic_color, [WATER_DEEP, WATER, SAND]):
                nmap[i][j] = MEADOW_HIGH

scale = randrange(190, 350)
octaves = randrange(10, 30)
persistence = randrange(15, 25) / 100.0
lacunarity = randrange(20, 32) / 10.0 
                
                
world = np.zeros((map_size["width"], map_size["height"]))
for i in range(map_size["width"]):
    for j in range(map_size["height"]):
        world[i][j] = noise.pnoise2(i/scale, 
                                    j/scale, 
                                    octaves=octaves, 
                                    persistence=persistence, 
                                    lacunarity=lacunarity, 
                                    repeatx=1000, 
                                    repeaty=1000, 
                                    base=0)
        
for i in range(map_size["width"]):
    for j in range(map_size["height"]):
        if world[i][j] > (0.390 - 0.390 * params["clay"]):
            pic_color = np.array(nmap[i][j])
            
            if not colorCheck(pic_color, [WATER_DEEP, WATER]):
                nmap[i][j] = CLAY

        if world[i][j] < -(0.390 - 0.390 * params["swamp"]):
            pic_color = np.array(nmap[i][j])

            if not colorCheck(pic_color, [WATER_DEEP, WATER]):
                nmap[i][j] = SWAMP
                

                
scale = randrange(390, 550)
octaves = randrange(10, 30)
persistence = randrange(15, 25) / 100.0
lacunarity = randrange(20, 32) / 10.0 
                
                
world = np.zeros((map_size["width"], map_size["height"]))
for i in range(map_size["width"]):
    for j in range(map_size["height"]):
        world[i][j] = noise.pnoise2(i/scale, 
                                    j/scale, 
                                    octaves=octaves, 
                                    persistence=persistence, 
                                    lacunarity=lacunarity, 
                                    repeatx=1000, 
                                    repeaty=1000, 
                                    base=0)
        
for i in range(map_size["width"]):
    for j in range(map_size["height"]):
        if world[i][j] > (0.390 - 0.390 * params["meadow_low"]):
            pic_color = np.array(nmap[i][j])
            
            if not colorCheck(pic_color, [WATER_DEEP, WATER, SAND, PRAIRIE]):
                nmap[i][j] = MEADOW_LOW

        if world[i][j] < -(0.390 - 0.390 * params["forest_leaf"]):
            pic_color = np.array(nmap[i][j])

            if not colorCheck(pic_color, [WATER_DEEP, WATER, SAND, MEADOW_HIGH]):
                nmap[i][j] = FOREST_LEAF
                                

                
scale = randrange(650, 1450)   
octaves = randrange(10, 30)
persistence = randrange(15, 25) / 100.0
lacunarity = randrange(20, 25) / 10.0 

world = np.zeros((map_size["width"], map_size["height"]))
for i in range(map_size["width"]):
    for j in range(map_size["height"]):
        world[i][j] = noise.pnoise2(i/scale, 
                                    j/scale, 
                                    octaves=octaves, 
                                    persistence=persistence, 
                                    lacunarity=lacunarity, 
                                    repeatx=1000, 
                                    repeaty=1000, 
                                    base=0)
                
for i in range(map_size["width"]):
    for j in range(map_size["height"]):
        if world[i][j] < -(0.39 - 0.39 * params["lake_deep_width"]):
            nmap[i][j] = WATER_DEEP
        elif world[i][j] < -(0.39 - 0.39 * params["lake_width"]):
            pic_color = nmap[i][j]
            if not colorCheck(pic_color, [WATER_DEEP]):
                nmap[i][j] = WATER
                
                
for i in range(int(params["spots_count"]/len(spots_list))):
    for j in spots_list:
        
        try_count = 1000
        
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
                    break
        
        r = randrange(params["min_spot_range"], params["max_spot_range"]) if j == "water_well_spot" else 1
        max_q = randrange(params["min_quality"], params["max_quality"])
        
        spots[j].append({"x":x, "y":y, "r":r, "max_quality":max_q})
        
now = datetime.datetime.now()
now_dat = str(now.strftime("%Y-%m-%d_%H-%M-%S"))

with open(f"spots_{now_dat}.json", 'w') as outfile:
    json.dump(spots, outfile)

filename = f"map_{now_dat}.png"
cv2.imwrite(filename, nmap)
print (filename)
print ("--- %s seconds left ---" % (time.time() - start_time))