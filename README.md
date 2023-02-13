# Pineapple


Pineapple is an open-source Python library for scalable and enriched spatial data analysis. The regionalization module provides the algorithm for the enriched p-regions problem, the scalable max-p-regions solution, and the enriched max-p-regions problem solution. The spatial regression module provides the scalable Multi-scale Geographically Weighted Regression method. It is under development for the inclusion of newly proposed algorithms for scalable spatial analysis methods.

## Modules

### pineapple.regionalization.pruc

### SMP

### EMP
The three-phase greedy algorithm for solving the enriched max-p-regions problem.

## How to use

### EMP

To 

The implementation of the EMP algorithm has been packaged into the EMP.jar package. It is assumed that the data required for the experiments is in the directory ./data. To run the experiments, use the command:
```
java -jar EMP.java
```
The 10 experiments described in the experiment section for the EMP problem will be carried out one by one.

To reproduce the results about the impact of the threshold value and scability for the MP-regions implementation, use the command:
```
java -jar MP.java
```
