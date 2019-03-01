# Fairness in Dataflow Scheduling in the Cloud

This repository contains the code used in used in the experiments of the paper "Fairness in Dataflow Scheduling in the Cloud"

## Compile
Using maven the project can be build using:

```mvn clean package```

This will produce a jar containing all required dependencies with can be executed by running:

```
java -jar java -jar target/DataflowScheduler-1.0-jar-with-dependencies.jar 
```

## Run

The scheduler uses as input dataflows described in the [Pegasus Dax format](http://pegasus.isi.edu/schema/DAX).
The folder resources contains multiple dataflows of different size.

The DataflowScheduler must be executed in the same folder as the resources folder. The DataflowScheduler can be 
executed using the following command:

```
java -jar target/DataflowScheduler-1.0-jar-with-dependencies.jar <arguments>

```

### Arguments
Each time the DataflowScheduler if executed it outputs the results for one experiment.
The arguments that can be configure each experiment are:

1. RankMethod, default dagMerge (default, perDag, dagMerge)
2. Use multiple Objectives (Boolean)
3. Ensemble size (how many dataflow the schedule will contain) 
4. Quantum Size, can be an hour or a second (perSec, perHour)
5. Pruning quota, how many candidate plans to keep at any time
6. Constraint mode (0,1,2,3)
   * 0: No constraints
   * 1: After each scheduling step keep one candidate plan, that maximizes the fairness and satisfies the money and time constraints
   * 2: Keep only one plan after scheduling is complete. After each scheduling step only the plans that satisfy the constraints are kept.
   * 3: During scheduling produce all plans regardless of the constraints and at the end return only the plan that maximizes the fairness and satisfies the money and time constraints
7. Money constraint (double)
8. Time constraint in MS
9. Name of the first dataflow of the ensemble (i.e. MONTAGE, LIGO, ...)
10. The size of the first dataflow of the ensemble (i.e. 10, 50, 100)
11. Name of the second dataflow 
12. The size of seconddataflow 
13. ...
14. ...

An example experiment is:
```
mvn clean package

java -jar java -jar target/DataflowScheduler-1.0-jar-with-dependencies.jar dagMerge true 2 perSec 10 1 13500 60000 Example 10 Example 10 
```