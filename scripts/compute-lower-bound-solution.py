import pandas as pd
import sys


if len(sys.argv) < 2:
    raise Exception('Input filename needs to be provided.')

data = []

for i in range(1, len(sys.argv)):
    data.append(pd.read_csv(sys.argv[i], delimiter=';'))


instances = []

for index, row in data[0].iterrows():
    solved_in_all_files = True
    for dataframe in data:
        other_row = dataframe[dataframe['file'] == row['file']]
        if other_row.empty or (other_row['verified'].item() != 'correct' and other_row['verified'].item() != '>>BAD COST<< '):
            solved_in_all_files = False
            break
    if solved_in_all_files:
        exact_solution = row['solsize'] - (0 if row['verified'] == 'correct' else int(row['costdiff']))
        solution_ratios = []
        for dataframe in data:
            other_row = dataframe[dataframe['file'] == row['file']]
            solution_ratios.append(other_row['recsteps'].item() / exact_solution if exact_solution > 0 else 1)
        instances.append(solution_ratios)

result = pd.DataFrame(instances)

result.to_csv('lb_solutions.csv', sep=';', index=True)
