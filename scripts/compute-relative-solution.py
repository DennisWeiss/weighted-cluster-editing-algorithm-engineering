import pandas as pd
import sys


if len(sys.argv) < 3:
    raise Exception('Input filename needs to be provided.')

upper_bound_data = pd.read_csv(sys.argv[1], delimiter=';')
lower_bound_data = pd.read_csv(sys.argv[2], delimiter=';')

instances = []

for index, upper_bound_row in upper_bound_data.iterrows():
    if upper_bound_row['finished'] == 1 and (upper_bound_row['verified'] == 'correct' or upper_bound_row['verified'] == '>>BAD COST<< '):
        file = upper_bound_row['file']
        lower_bound_row = lower_bound_data[lower_bound_data['file'] == file]
        if not lower_bound_row.empty and (lower_bound_row['verified'].item() == 'correct' or lower_bound_row['verified'].item() == '>>BAD COST<< '):
            heuristic_solution = upper_bound_row['solsize']
            exact_solution = heuristic_solution - (0 if upper_bound_row['verified'] == 'correct' else upper_bound_row['costdiff'])
            lower_bound_solution = lower_bound_row['recsteps'].item()
            if exact_solution > 0:
                instances.append([file, heuristic_solution / exact_solution, lower_bound_solution / exact_solution])

result = pd.DataFrame(instances, columns=['file', 'upperbound', 'lowerbound'])

result.to_csv('lb_ub.csv', sep=';', index=True)
