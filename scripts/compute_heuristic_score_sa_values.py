import pandas as pd
import sys


if len(sys.argv) < 2:
    raise Exception('Input filename needs to be provided.')

data = []

for i in range(1, len(sys.argv)):
    data.append(pd.read_csv(sys.argv[i], delimiter=';'))


avg_score = 0
avg_running_time = 0
n = 0


def finished_in_all_files(filename, data):
    for i in range(len(data)):
        row = data[i][data[i]['file'] == filename]
        sa_values_str = row['recsteps'].item()
        if not isinstance(sa_values_str, str):
            return False
        sa_values = sa_values_str.split(',')
        if row.empty or not (row['finished'].item() == 1 and (row['verified'].item() == 'correct' or row['verified'].item() == '>>BAD COST<< ') and len(sa_values) == 30_000):
            return False
    return True


indices = []

for index, row in data[0].iterrows():
    verified = row['verified']
    if index == 149:
        print()
    if finished_in_all_files(row['file'], data):
        n += 1
        indices.append(index)
        solsize = row['solsize']
        score = 1 if verified == 'correct' else (solsize - row['costdiff']) / solsize
        avg_score = (n-1) / n * avg_score + 1/n * score
        avg_running_time = (n-1) / n * avg_running_time + 1/n * row['time']


print(n)
print(f'avg score = {avg_score}')
print(f'avg running time = {avg_running_time}')
