import jpype
import array
from jpype import java
from jpype import javax

def from_dataframe(df):
    gcolumn = df['geometry'].to_list()
    gArrayList = jpype.java.util.ArrayList()
    for gEntry in gcolumn:
        gArrayList.add(str(gEntry))
    Rook = jpype.JClass("edu.ucr.cs.pineapple.utils.SpatialGrid")
    SMPI = jpype.JClass("edu.ucr.cs.pineapple.regionalization.SMPPPythonInterface")
    r = Rook()
    geometry = SMPI.stringListToGeometryList(gArrayList);
    neighborList = r.RookWithGeometryNoGrid(geometry)
    nDic = {}
    wDic = {}
    for i in range(neighborList.size()):
        nList = list(neighborList.get(i));
        wList = []
        nDic[i] = nList
        for n in nList:
            wList.append(1.0)
        wDic[i] = wList
    from libpysal.weights import W
    return W(nDic, wDic)