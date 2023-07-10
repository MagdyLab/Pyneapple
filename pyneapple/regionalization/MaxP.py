import jpype
import array
import numpy as np
import geopandas
import pandas
import math
from jpype import java
from jpype import javax
import emp
import smp

def MaxP(df, w, disName, sumName, sumLow, sumHigh = math.inf, minName = None, minLow = -math.inf, minHigh = math.inf, maxName = None, maxLow = -math.inf, maxHigh = math.inf, avgName = None, avgLow = -math.inf, avgHigh = math.inf, countLow = -math.inf, countHigh = math.inf):
#def emp(df, w, disName, minName, minLow, minHigh, maxName, maxLow, maxHigh, avgName, avgLow, avgHigh, sumName, sumLow, sumHigh, countLow, countHigh):
    EMP_flag = False
    if(minLow != -float('inf') or minHigh != float('inf')):
        if(minName == None):
            print("Invalid MIN constraint!")
        else:
            EMP_flag = True
    if(maxLow != -float('inf') or maxHigh != float('inf')):
        if(maxName == None):
            print("Invalid MAX constraint!")
        else:
            EMP_flag = True
    if(avgLow != -float('inf') or maxHigh != float('inf')):
        if(avgName == None):
            print("Invalid AVG constraint!")
        else:
            EMP_flag = True
    if(EMP_flag):
        return emp.mp(df, w, disName, minName, minLow, minHigh, maxName, maxLow, maxHigh, avgName, avgLow, avgHigh, sumName, sumLow, sumHigh, countLow, countHigh)
    else:
        return smp(df, w, disName, sumName, sumLow)


