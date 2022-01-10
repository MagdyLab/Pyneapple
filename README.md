# FaCT

The three-phase greedy algorithm for solving the enriched max-p-regions problem.

## Code and Data
1. The repository contains a project that can be imported by the IntelliJ IDEA. 
2. The source code for the EMP implementation is in "src/main/java/edu/ucr/emp/EMP.java". 
3. The data used in the experiments are in the "data" folder. The size of the CA dataset is too big for github, it is available at https://drive.google.com/drive/folders/16uiQEX_cKD2d_rcLKj0-0MWy0g5Ql7wk?usp=sharing.

## To run the experiments

The implementation of the FaCT algorithm has been packaged into the EMP.jar package. It is assumed that the data required for the experiments is in the directory ./data. To run the experiments, use the command:
```
java -jar EMP.java
```
The 10 experiments described in the experiment section for the EMP problem will be carried out one by one.

To reproduce the results about the impact of the threshold value and scability for the MP-regions implementation, use the command:
```
java -jar MP.java
```
