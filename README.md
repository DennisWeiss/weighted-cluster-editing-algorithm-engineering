# Solver for the Problem "Weighted Cluster Editing"

*Weighted Cluster Editing* is an NP-hard problem. It asks for the minimum cost solution of modifying a graph (removing edges or inserting edges). The costs (weights of edges) are part of the input. Positive costs correspond to edges of the graph with the cost to remove the edge and negative costs correspond to non-edges of the graph with the absolute value being the cost of inserting this edge. In this version all edge weights (costs) are integer.

## Our algorithm

Our algorithm solves the problem by solving the underlying optimization problem directly by branching on merging two vertices of a P3 and branching on deleting an existing edge of a P3. During the recursive branching we use computation of upper and lower bounds to prune the search tree.


## Installation instructions

### What you should download
team3-heuristic.jar
team3-ilp.jar
team3-csp.jar

### To install

```bash
cd team3 # start where you downloaded our jar files into
mkdir lib
cd lib
wget https://github.com/google/or-tools/releases/download/v9.2/or-tools_amd64_ubuntu-18.04_v9.2.9972.tar.gz
tar xfz or-tools_amd64_ubuntu-18.04_v9.2.9972.tar.gz
cd or-tools_Ubuntu-18.04-64bit_v9.2.9972
unzip ortools-linux-x86-64-9.2.9972.jar -d extracted-jar
```

### To execute

```bash
cd team3 # it's probably mandatory to execute them from the folder containing our jars
java -cp "team3-heuristic.jar:lib/or-tools_Ubuntu-18.04-64bit_v9.2.9972/*" berlin.tu.algorithmengineering.heuristics.HeuristicMain
java -cp "team3-csp.jar:lib/or-tools_Ubuntu-18.04-64bit_v9.2.9972/*" berlin.tu.algorithmengineering.csp.ConstraintSatisfactionMain
java -jar "team3-ilp.jar"
```
