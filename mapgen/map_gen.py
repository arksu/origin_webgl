from skimage.draw import line, polygon, circle, ellipse
from scipy.spatial import Voronoi, voronoi_plot_2d
from collections import defaultdict
from opensimplex import OpenSimplex
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
                "lake_deep_width": 0.018,
                "lake_width": 0.029,
                "max_quality": 100,
                "min_quality": 10,
                "min_spot_range": 20,
                "max_spot_range": 80,
                "spots_count": 100
             }


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
 
    
tiles_list=[
    (SWAMP,       {"scale":250.0, "threshold": 0.74}),
    (SWAMP,       {"scale":300.0, "threshold": 0.8}),
    (CLAY,        {"scale":300.0, "threshold": 0.55}),
    (CLAY,        {"scale":400.0, "threshold": 0.55}),
    (MEADOW_LOW,  {"scale":170.0, "threshold": 0.6}),
    (MEADOW_HIGH, {"scale":170.0, "threshold": 0.6}),
    (FOREST_LEAF, {"scale":170.0, "threshold": 0.6}),
    (PRAIRIE,     {"scale":220.0, "threshold": 0.72}),
    (TUNDRA,      {"scale":170.0, "threshold": 0.8}),
    (FOREST_PINE, {"scale":200.0, "threshold": 0.8}),
    (FOREST_PINE, {"scale":260.0, "threshold": 0.8}),
]

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

for x in range(-1, 25):
    for y in range(-1, 25):
        points.append([x*200 + randrange(0, 10)/0.005, y*200 + randrange(0, 10)/0.005])

points = np.array(points)
vor = Voronoi(points)

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
                        
def printProgressBar (iteration, total, prefix = '', suffix = '', decimals = 1, length = 100, fill = '█', printEnd = "\r"):
    percent = ("{0:." + str(decimals) + "f}").format(100 * (iteration / float(total)))
    filledLength = int(length * iteration // total)
    bar = fill * filledLength + '-' * (length - filledLength)
    print(f'\r{prefix} |{bar}| {percent}% {suffix}', end = printEnd)
    if iteration == total: 
        print()

        
pb_idx = 0
        
printProgressBar(pb_idx, 8 + len(tiles_list), prefix = 'Progress:', suffix = 'Complete', length = 50)
pb_idx+=1

nmap = np.zeros(shape=(map_size["width"],map_size["height"], 3))

for i in range(map_size["width"]):
    for j in range(map_size["height"]):
        nmap[i][j] = FOREST_PINE
        
printProgressBar(pb_idx, 8 + len(tiles_list), prefix = 'Progress:', suffix = 'Complete', length = 50)
pb_idx+=1

draw_base = []

printProgressBar(pb_idx, 8 + len(tiles_list), prefix = 'Progress:', suffix = 'Complete', length = 50)
pb_idx+=1

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

printProgressBar(pb_idx, 8 + len(tiles_list), prefix = 'Progress:', suffix = 'Complete', length = 50)
pb_idx+=1

noises = []

for i in tiles_list:

    tmp = OpenSimplex(seed=randrange(0, 9999))
    noises.append(tmp)
    
    printProgressBar(pb_idx, 8 + len(tiles_list), prefix = 'Progress:', suffix = 'Complete', length = 50)
    pb_idx+=1
    
printProgressBar(pb_idx, 8 + len(tiles_list), prefix = 'Progress:', suffix = 'Complete', length = 50)
pb_idx+=1

idx=0
for k in tiles_list:
    for i in range(map_size["width"]):
        for j in range(map_size["height"]):
            if noises[idx].noise2d(x=float(i)/k[1]["scale"], y=float(j)/k[1]["scale"]) > k[1]["threshold"]:
                nmap[i][j] = k[0]
    idx+=1
          
printProgressBar(pb_idx, 8 + len(tiles_list), prefix = 'Progress:', suffix = 'Complete', length = 50)
pb_idx+=1

for db in draw_base:
    draw(nmap, db[0], db[3], db[2], SAND)
    
for db in draw_base:
    draw(nmap, db[0], db[1]/2, db[2], WATER)
    
for db in draw_base:
    draw(nmap, db[0], db[1]/3, db[2], WATER_DEEP)             

world = OpenSimplex(seed=42)
    
for i in range(map_size["width"]):
    for j in range(map_size["height"]):
        if world.noise2d(x=i*0.005, y=j*0.005) > 0.35:
            nmap[i][j] = WATER_DEEP
        elif world.noise2d(x=i*0.005, y=j*0.005) > 0.3:
            pic_color = nmap[i][j]
            if not colorCheck(pic_color, [WATER_DEEP]):
                nmap[i][j] = WATER

printProgressBar(pb_idx, 8 + len(tiles_list), prefix = 'Progress:', suffix = 'Complete', length = 50)
pb_idx+=1

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
        
        r = randrange(params["min_spot_range"], params["max_spot_range"])
        max_q = randrange(params["min_quality"], params["max_quality"])
        
        spots[j].append({"x":x, "y":y, "r":r, "max_quality":max_q})
        
now = datetime.datetime.now()
now_dat = str(now.strftime("%Y-%m-%d_%H-%M-%S"))



with open(f"spots_{now_dat}.json", 'w') as outfile:
    json.dump(spots, outfile)

printProgressBar(pb_idx, 8 + len(tiles_list), prefix = 'Progress:', suffix = 'Complete', length = 50)
pb_idx+=1

print(8 + len(tiles_list), pb_idx)

filename = f"map_{now_dat}.png"
cv2.imwrite(filename, nmap)

printProgressBar(pb_idx, 8 + len(tiles_list), prefix = 'Progress:', suffix = 'Complete', length = 50)

print (filename)
print ("--- %s seconds left ---" % (time.time() - start_time))