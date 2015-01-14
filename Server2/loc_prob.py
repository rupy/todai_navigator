#! /usr/bin/python
# -*- coding: utf-8 -*-

import geopy  # sudo apt-get install python-geopy
import geopy.distance
import sys
import math
from scipy.integrate import quad


DIST_THRESHOLD = 200.0 # [m]
SIGMA          = 50.0  # [m]
MEAN           = 0.0   # [m]
DELTA          = 5.0   # [m]

place={
"akamon"                    : [35.710613, 139.760325], 
"engineer_bldg8"            : [35.714805, 139.760944], 
"main_gate"                 : [35.712944, 139.759648], 
"science_bldg47"            : [35.713152, 139.763343], 
"engineer_bldg11"           : [35.713671, 139.759464], 
"fukutake_hall"             : [35.711425, 139.760060], 
"sanshiro_pond"             : [35.712281, 139.762069], 
"yasudakoudou"              : [35.713439, 139.762275], 
"engineer_bldg14"           : [35.714289, 139.759319], 
"gotenshita_memorial_arena" : [35.712719, 139.763796], 
"science_bldg1"             : [35.713789, 139.763260]
}

distance={
"akamon"                    : 0,
"engineer_bldg8"            : 0,
"main_gate"                 : 0,
"science_bldg47"            : 0,
"engineer_bldg11"           : 0,
"fukutake_hall"             : 0,
"sanshiro_pond"             : 0,
"yasudakoudou"              : 0,
"engineer_bldg14"           : 0,
"gotenshita_memorial_arena" : 0,
"science_bldg1"             : 0
}

probability={
"akamon"                    : 0,
"engineer_bldg8"            : 0,
"main_gate"                 : 0,
"science_bldg47"            : 0,
"engineer_bldg11"           : 0,
"fukutake_hall"             : 0,
"sanshiro_pond"             : 0,
"yasudakoudou"              : 0,
"engineer_bldg14"           : 0,
"gotenshita_memorial_arena" : 0,
"science_bldg1"             : 0
}


def calcNormProbability(x, mean, sigma, delta):
    """
    確率密度関数P(x < X < x+delta)~N(mean, sigma)を計算する。
    """
    end = float(x+delta)
    answer, abserr = quad(lambda x:(1/(math.sqrt(2*math.pi)*sigma))*math.exp(-pow((x-mean),2.0)/(2*pow(sigma,2.0))),end,x)

    return answer


def getProbability(latitude, longitude):
    """
    緯度経度(L)を入力すると辞書形式で各クラス(C)の出現確率:P(C|L)を返す。
    """
    p1 = geopy.Point(latitude, longitude)
    norm_term = 0.0
    for building in place:
        p2 = geopy.Point(place[building][0], place[building][1])
        distance[building]    = geopy.distance.distance(p1, p2).m # 最後の.mはメートルという意味。.kmで単位はキロになる。
        probability[building] = calcNormProbability(distance[building], MEAN, SIGMA, DELTA)
        norm_term += probability[building]

    for building in place:
        probability[building] = probability[building]/norm_term
        print "%-30s: %-10f, %-10f" %(building, distance[building] ,probability[building])
       
    return probability


if __name__ == "__main__":
    LAT, LNG = float(sys.argv[1]), float(sys.argv[2])
    getProbability(LAT, LNG)
