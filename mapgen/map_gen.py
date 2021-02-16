import numpy as np
from random import randrange
import matplotlib.pyplot as plt
import math
import cv2

GRASS =      np.array([30, 80, 30, 255])
SAND =       np.array([30, 80, 80, 255])
WATER =      np.array([80, 30,  0, 255])
DEEP_WATER = np.array([30,  0,  0, 255])

map_size = (1000, 1000, 0, 5)
riv_params = {"num_pair_points": 2 ,"max_riv_width": 10, "min_riv_width": 5}

nmap = np.zeros(shape=(map_size[0],map_size[1], 4))

for x in range(map_size[0]):
    for y in range(map_size[1]):
        nmap[x][y] = GRASS

points = []

for i in range(riv_params["num_pair_points"]):
    x_st = randrange(0, map_size[1])
    y_st = 0
    x_en = randrange(0, map_size[0])
    y_en = map_size[1]

    points.append( (x_st, y_st, x_en, y_en) )

#random walk

def vecs_len(v1, v2):
    return math.sqrt((v1[0] - v2[0])**2 + (v1[1] - v2[1])**2)

def vec_len(vec):
    return math.sqrt(vec[0]**2 + vec[1]**2)

def norm(vec):
    l = vec_len(vec)
    return [vec[0]/l, vec[1]/l]

def ifInRect(vec):
    if vec[0] <= 0 or vec[1] <= 0 or vec[0] >= map_size[0] or vec[1] >= map_size[1]:
        return False
    else:
        return True
    
def rotate(vec, ang):
    ang_rad = math.radians(ang)
    return [ vec[0] * math.cos(ang_rad) - vec[1] * math.sin(ang_rad), vec[0] * math.sin(ang_rad) + vec[1] * math.cos(ang_rad)]

def lerp(valFrom, valTo, f):
    return (valFrom * (1.0 - f)) + (valTo * f);

def getAngRange():
    return randrange(-70, 60)

def getWidthRange():
    return randrange(riv_params["min_riv_width"], riv_params["max_riv_width"])

nvec = [1.0, 0.0]
angFrom = 0.0
angTo = getAngRange()

widthFrom = riv_params["min_riv_width"]
widthTo = randrange(riv_params["min_riv_width"], riv_params["max_riv_width"])
itr = 0.0

for j in range(riv_params["num_pair_points"]):

    old_point = [0.0, map_size[0] * (j + 1) / (riv_params["num_pair_points"] + 1) ]

    lerp_time = 25

    for i in range(9999):

        itr+=1.0

        if i % lerp_time == 0:
            angFrom = angTo
            angTo = getAngRange()
            widthFrom = widthTo
            widthTo = getWidthRange()
            itr = 0.0

        rvec = rotate(nvec, lerp(angFrom, angTo, itr / float(lerp_time) ))

        curr_point = [old_point[0] + rvec[0], old_point[1] + rvec[1]]

        for a in range(360):
            arvec = rotate(nvec, a)
            widthCurr = lerp(widthFrom, widthTo, itr / float(lerp_time))
            for l in range(int(widthCurr)):
                lrvec = [curr_point[0] + arvec[0]*l, curr_point[1] + arvec[1]*l]
                if ifInRect(lrvec):
                    x = int(lrvec[0])
                    y = int(lrvec[1])

                    curr_color = nmap[x][y]
                    
                    range_ = widthCurr / 4
                    
                    if l >= 0 and l < range_:
                        if ifInRect(curr_point):
                            nmap[int(lrvec[0])][int(lrvec[1])] = DEEP_WATER
                        
                    if l >= range_ and l < 2 * range_:
                        if not np.array_equal(curr_color, DEEP_WATER):
                            if ifInRect(curr_point):
                                nmap[int(lrvec[0])][int(lrvec[1])] = WATER
                    
                    if l >= 2 * range_ and l < 3 * range_:
                        if (not np.array_equal(curr_color, DEEP_WATER) ) and (not np.array_equal(curr_color, WATER)):
                            if ifInRect(curr_point):
                                nmap[int(lrvec[0])][int(lrvec[1])] = SAND
        
        old_point = curr_point


cv2.imwrite('map1.png', nmap)