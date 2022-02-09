#! /usr/bin/env python3

import sys
import numpy as np
from scipy.sparse.csgraph import connected_components

if len(sys.argv) != 3:
    print('usage: wce_verify.py graph.dimacs wce.solution', file=sys.stderr)
    sys.exit(1)

# read graph size
n = None
with open(sys.argv[1]) as f:
    i=0
    line = f.readline()
    while line:
        i += 1
        if line[0] == '#':
            line = f.readline()
            continue
        # do we have n yet?
        if n is None:
            n = int(line.strip())
            g = np.zeros((n, n), dtype=int)
        else:
            e = line.strip().split()
            u = int(e[0])-1
            v = int(e[1])-1
            g[u, v] = g[v, u] = int(e[2])
        line = f.readline()

# read edge modifications
cost = 0
with open(sys.argv[2]) as f:
    line = f.readline()
    i=0
    while line:
        i += 1
        if line[0] != '#':
            e = line.strip().split()
            if len(e) < 2:
                print("bad entry in solution, line {}".format(i))
                sys.exit(1)
            u = int(e[0])-1
            v = int(e[1])-1
            if u == v:
                print("bad entry in solution, line {}".format(i))
                sys.exit(1)
            if v >= n:
                print("bad entry in solution, line {}".format(i))
                sys.exit(1)
            cost += abs(g[u, v])
            if g[u, v] == 0 and g[v, u]==0:
                g[u, v] = g[v, u] = 1
            else:
                g[u, v] = -g[u, v]
                g[v, u] = -g[v, u]
        line = f.readline()

# find connected components
posg = np.copy(g)
posg[posg < 0] = 0

nc, labels = connected_components(posg, directed=False, return_labels=True)

# determine size of each connected component
cliquesize = np.zeros(nc, dtype=int)
for i in range(n):
    cliquesize[labels[i]] += 1

# check if each vertex in the connected component has degree #vertices in cc - 1
for i in range(n):
    assert(np.count_nonzero(posg[i]) <= cliquesize[labels[i]]-1)
    if np.count_nonzero(posg[i]) < cliquesize[labels[i]]-1:
        # vertex i has too low degree
        # take arbitrary neighbor
        ni = posg[i].nonzero()[0]
        for j in ni:
            nj = posg[j].nonzero()[0]
            for k in nj:
                if i != k and j != k and i != j and posg[i,k] == 0:
                    print("Found P3 {}, {}, {}".format(i+1, j+1, k+1))
                    sys.exit(1)
        print("Error: {} has too low degree ({} instead of {}), but I couldn't find a P3".format(i, np.count_nonzero(posg[i]), cliquesize[labels[i]]-1))

# all degrees fit. return cost.
print(cost)
