from scipy.spatial import Voronoi, voronoi_plot_2d
import matplotlib.pyplot as plt
from random import randrange
import numpy as np
import datetime
import noise
import math
import time
import cv2


map_size = {"width":5000, "height":5000}
riv_params = {"num_pair_points": 2 ,"max_riv_width": 60, "min_riv_width": 50, "min_sand_width":-50, "max_sand_width":5}

start_time = time.time()

points = []

for x in range(-1, 5):
    for y in range(-1, 5):
        points.append([x*700 + randrange(0, 10)/0.005, y*700 + randrange(0, 10)/0.005])

points = np.array(points)
vor = Voronoi(points)
# fig = voronoi_plot_2d(vor)
# plt.show()

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
    return (valFrom * (1.0 - f)) + (valTo * f);

def getAngRange():
    return randrange(-38, 38)

def getWidthRange(riv_params):
    return randrange(riv_params["min_riv_width"], riv_params["max_riv_width"])

def getSandRange(riv_params):
    return randrange(riv_params["min_sand_width"], riv_params["max_sand_width"])

def drawPointToMap(nmap, point, c):
    nmap[int(point[0])][int(point[1])] = [c[0], c[1], c[2], 255]

def hexColToArr(c):
    return np.array([ int(c[2]),  int(c[1]), int(c[0]), 255])[0:3]
    
def drawWater(nmap, point, w, map_size, sand_w):
    
    nvec = [0.0, 1.0]
    
    for a in range(180):
        avec = norm(rotate(nvec, a*2))
        for l in range(int(w)):
            lrvec = [point[0] + avec[0]*l, point[1] + avec[1]*l]
            
            if ifInRect(lrvec, map_size):
                curr_color = nmap[int(lrvec[0])][int(lrvec[1])]

                range_ = w / 4

                if l >= 0 and l < range_:
                    nmap[int(lrvec[0])][int(lrvec[1])] = WATER_DEEP
                    continue
                     
                if l >= range_ and l < range_ * 2:
                    if not np.array_equal(curr_color, WATER_DEEP):
                        nmap[int(lrvec[0])][int(lrvec[1])] = WATER
                        continue
                       
                if l < (2 + sand_w) * range_:
                    if (not np.array_equal(curr_color, WATER_DEEP) ) and ( not np.array_equal(curr_color,  WATER)):
                        nmap[int(lrvec[0])][int(lrvec[1])] = SAND

HEATH =       hexColToArr(b'\x00\x00\x00')
MEADOW_LOW =  hexColToArr(b'\xd9\x7c\xc0')# луг (низкие травы)
MEADOW_HIGH = hexColToArr(b'\x8e\xbf\x8e')# луг (высокие травы)
FOREST_LEAF = hexColToArr(b'\x17\xd4\x21')# лес лиственный
FOREST_PINE = hexColToArr(b'\x31\x61\x17')# лес хвойный
CLAY =        hexColToArr(b'\x70\x39\x0f')# глина
SAND =        hexColToArr(b'\xe0\xe0\x34')# песок
PRAIRIE =     hexColToArr(b'\xf0\xa0\x1f')# степь
SWAMP =       hexColToArr(b'\x1c\x38\x19')# болото
TUNDRA =      hexColToArr(b'\x3c\x7a\x6f')# тундра
WATER =       hexColToArr(b'\x00\x55\xff')# мелководье
WATER_DEEP =  hexColToArr(b'\x00\x00\xff')# глубокая вода

nmap = np.zeros(shape=(map_size["width"],map_size["height"], 3))

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
    
    widthFrom = getWidthRange(riv_params)
    widthTo = getWidthRange(riv_params)

    angFrom = getAngRange()
    angTo = getAngRange()
    
    sandFrom = getSandRange(riv_params)
    sandTo = getSandRange(riv_params)
    
    lerpInt = 70
    itr = 0.0

    min_len = vec_len([p2[0] - p1[0], p2[1] - p1[1]])

    for l in range(map_size["width"]):
        if l % lerpInt == 0 and l != 0:
            
            widthFrom = widthTo
            widthTo = getWidthRange(riv_params)
            
            angFrom = angTo
            angTo = getAngRange()
            
            sandFrom = sandTo
            sandTo = getSandRange(riv_params)

            itr = 0.0

        itr += 1.0
        
        if l > min_len/1.5:
            mrvec = rotate(nvec, lerp(angFrom, angTo, itr / float(lerpInt) ))
            rvec = norm([ lerp( p2[0] - old_point[0], mrvec[0], itr / float(lerpInt)) , lerp( p2[1] - old_point[1], mrvec[1], itr / float(lerpInt))])
        else:
            rvec = rotate(nvec, lerp(angFrom, angTo, itr / float(lerpInt) ))

        old_point = [old_point[0] + rvec[0], old_point[1] + rvec[1]]
        
        drawWater(nmap, old_point, lerp(widthFrom, widthTo, itr / float(lerpInt) ), map_size, lerp(sandFrom, sandTo, itr / float(lerpInt) ))
        
        if vecs_len(old_point, p2) <= 1:
            break

scale = randrange(200, 450)
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
        if world[i][j] > 0.380:
            pic_color = nmap[i][j]
            if (not np.array_equal(np.array(pic_color), WATER_DEEP)) and (not np.array_equal(np.array(pic_color), WATER)):
                nmap[i][j] = CLAY
                
for i in range(map_size["width"]):
    for j in range(map_size["height"]):
        if world[i][j] < -0.380:
            pic_color = nmap[i][j]
            if (not np.array_equal(np.array(pic_color), WATER_DEEP)) and (not np.array_equal(np.array(pic_color), WATER)):
                nmap[i][j] = SWAMP

scale = randrange(650, 1450)                

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
        if world[i][j] < -0.275:
            nmap[i][j] = WATER_DEEP
        elif world[i][j] < -0.271:
            pic_color = nmap[i][j]
            if not np.array_equal(np.array(pic_color), WATER_DEEP):
                nmap[i][j] = WATER
                
now = datetime.datetime.now()
now_dat = str(now.strftime("%Y-%m-%d_%H-%M-%S"))
cv2.imwrite(f"map_{now_dat}.png", nmap)

print("--- %s seconds left ---" % (time.time() - start_time))