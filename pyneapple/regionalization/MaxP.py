import jpype
import array
import numpy as np
import geopandas
import pandas
import math
from jpype import java
from jpype import javax
from .emp import emp
from .smp import smp
import spopt
from spopt.region import maxp as MaxP

def maxp(df, w, disName, sumName = None, sumLow = -math.inf, sumHigh = math.inf, minName = None, minLow = -math.inf, minHigh = math.inf, maxName = None, maxLow = -math.inf, maxHigh = math.inf, avgName = None, avgLow = -math.inf, avgHigh = math.inf, countLow = -math.inf, countHigh = math.inf):
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
    if((not EMP_flag) and sumName == None):
        print("Invalid constraint")
        return
    if(EMP_flag):
        p, regions = expressive_maxp(df, w, disName, minName, minLow, minHigh, maxName, maxLow, maxHigh, avgName, avgLow, avgHigh, sumName, sumLow, sumHigh, countLow, countHigh)
        return p, regions
    else:
        p, regions = MaxP.maxp(df, w, disName, sumName, sumLow, 2)
        return p, regions


