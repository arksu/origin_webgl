from skimage.draw import line, polygon, circle, ellipse
from scipy.spatial import Voronoi, voronoi_plot_2d
from random import randrange
import numpy as np
import datetime
import math
import time
import cv2

import warnings
warnings.simplefilter(action='ignore', category=FutureWarning)
warnings.simplefilter(action='ignore', category=RuntimeWarning)


print("Rivers generation...")

# размеры карты, 1 супергрид 5000 на 5000 тайлов
map_size = {"width": 5000, "height": 5000}

params = {
    "max_riv_width": 20,
    "min_riv_width": 12,
    "min_sand_width": -60,
    "max_sand_width": 60,
    "rivers_cell_count": 10,
    "rivers_cell_count2": 16,
    "rivers_rand_range": 10,
}

def hexColToArr(c):
    return np.array([ int(c[2]),  int(c[1]), int(c[0])])

SAND =        hexColToArr(b'\xe0\xe0\x34')# песок ok
WATER =       hexColToArr(b'\x00\x55\xff')# мелководье ok 
WATER_DEEP =  hexColToArr(b'\x00\x00\xff')# глубокая вода ok

start_time = time.time()

points = []
points1 = []

rlen = (map_size["width"] / params["rivers_cell_count"]) / 2
rrange = params["rivers_rand_range"]
for x in range(-1, params["rivers_cell_count"]):
    for y in range(-1, params["rivers_cell_count"]):
        points.append([x*rlen + randrange(-rrange, rrange)*rlen, y*rlen + randrange(-rrange, rrange)*rlen])

points = np.array(points)
vor = Voronoi(points)

rlen = map_size["width"] / params["rivers_cell_count2"]
for x in range(-1, params["rivers_cell_count2"]):
    for y in range(-1, params["rivers_cell_count2"]):
        points1.append([x*rlen + randrange(-rrange, rrange)*rlen, y*rlen + randrange(-rrange, rrange)*rlen])

points1 = np.array(points1)
vor1 = Voronoi(points1)

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

        

printProgressBar(1, 8 , prefix = 'Progress:', suffix = 'Complete', length = 50)

# nmap = np.zeros(shape=(map_size["width"],map_size["height"], 3))

nmap = np.array(cv2.imread("../backend/map.png"))
        
printProgressBar(2, 8 , prefix = 'Progress:', suffix = 'Complete', length = 50)

draw_base = []

printProgressBar(3, 8 , prefix = 'Progress:', suffix = 'Complete', length = 50)

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
                        
for vpair in vor1.ridge_vertices:
    
    if vpair[0] < 0.0 or vpair[1] < 0.0:
        continue
    
    v0 = vor1.vertices[vpair[0]]
    v1 = vor1.vertices[vpair[1]]
    
    if ifInRect(v0, map_size) or ifInRect(v1, map_size):
        if not isRandDrop:
            local_points.append((v0, v1))
        elif randrange(0,10) > 2:
            local_points.append((v0, v1))
                                        
printProgressBar(4, 8 , prefix = 'Progress:', suffix = 'Complete', length = 50)
                
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

printProgressBar(5, 8 , prefix = 'Progress:', suffix = 'Complete', length = 50)

for db in draw_base:
    draw(nmap, db[0], db[3], db[2], SAND)
    
printProgressBar(6, 8 , prefix = 'Progress:', suffix = 'Complete', length = 50)
    
for db in draw_base:
    draw(nmap, db[0], db[1]/2, db[2], WATER)
    
printProgressBar(7, 8 , prefix = 'Progress:', suffix = 'Complete', length = 50)
    
for db in draw_base:
    draw(nmap, db[0], db[1]/3, db[2], WATER_DEEP)    
    
now = datetime.datetime.now()
now_dat = str(now.strftime("%Y-%m-%d_%H-%M-%S"))

printProgressBar(8, 8, prefix = 'Progress:', suffix = 'Complete', length = 50)

filename = "../backend/map.png"
cv2.imwrite(filename, nmap)

print(f"Done! {(time.time() - start_time)} seconds")
