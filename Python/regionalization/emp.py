def emp(df, w, disName, minName, minLow, minHigh, maxName, maxLow, maxHigh, avgName, avgLow, avgHigh, sumName, sumLow, sumHigh, countLow, countHigh):
    neighborHashMap = java.util.HashMap()
    for key, value in w.neighbors.items():
        tempSet = java.util.TreeSet()
        for v in value:
            tempSet.add(jpype.JInt(v))
        neighborHashMap.put(jpype.JInt(key), tempSet)
    EMP = jpype.JClass("edu.ucr.cs.pineapple.regionalization.EMP")()
    idList = jpype.java.util.ArrayList()
    disAttr = jpype.java.util.ArrayList()
    minAttr = jpype.java.util.ArrayList()
    maxAttr = jpype.java.util.ArrayList()
    avgAttr = jpype.java.util.ArrayList()
    sumAttr = jpype.java.util.ArrayList()
    disSucc = disAttr.addAll(df[disName].tolist())
    minSucc = minAttr.addAll(df[disName].tolist())
    maxSucc = maxAttr.addAll(df[sumName].tolist())
    avgSucc = avgAttr.addAll(df[disName].tolist())
    sumSucc = sumAttr.addAll(df[sumName].tolist())#check for att assign
    for i in range(0, df.shape[0]):
        idList.add(jpype.JInt(i))
    EMP.execute_regionalization(neighborHashMap, disAttr, minAttr,minLow, minHigh, maxAttr, maxLow, maxHigh, avgAttr, avgLow, avgHigh, sumAttr, sumLow, sumHigh, countLow, countHigh)
   
    return EMP.getP(), np.array(EMP.getRegionList())