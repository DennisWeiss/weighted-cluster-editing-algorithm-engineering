import math
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import sys


if len(sys.argv) < 2:
    raise Exception('Input filename needs to be provided.')

df = pd.read_csv(sys.argv[1], delimiter=';')

data = np.zeros((0, 30_000))

for index, row in df.iterrows():
    if row['verified'] == 'correct' or row['verified'] == '>>BAD COST<< ':
        sa_values_str = row['recsteps']
        if isinstance(sa_values_str, str):
            sa_values = sa_values_str.split(',')
            costdiff = row['costdiff']
            exact_solution = row['solsize'] - (0 if math.isnan(costdiff) else float(costdiff))
            data = np.vstack((data, np.asarray([[exact_solution / int(cost) if exact_solution != 0 else int(cost) for cost in sa_values]])))

plt.plot(np.arange(0, 30_000, 1), data.mean(axis=0))
plt.show()

