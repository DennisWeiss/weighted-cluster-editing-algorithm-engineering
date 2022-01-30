import pandas as pd
import sys


INSTANCE_TYPE = 'action-seq'


def type_of(filename):
    if 'random' in filename:
        return 'random'
    if 'real-world' in filename:
        return 'real-world'
    if 'actionseq' in filename:
        return 'action-seq'


if len(sys.argv) < 2:
    raise Exception('Number of files needs to be provided')

if len(sys.argv) < int(sys.argv[1]) + 1:
    raise Exception(f"Input filename{'' if int(sys.argv[1]) == 1 else 's'} of {int(sys.argv[1])} file{'' if int(sys.argv[1]) == 1 else 's'} need to be provided as console arguments")

data = []

for i in range(int(sys.argv[1])):
    data.append(pd.read_csv(sys.argv[i+2], delimiter=';', index_col=False))

total_time = 0
total_recsteps = 0
instances = 0


def correct_in_all_files(filename, data):
    for i in range(len(data)):
        row = data[i][data[i]['file'] == filename]
        if row.empty or row['verified'].item() != 'correct':
            return False
    return True


for index, row in data[0].iterrows():
    if INSTANCE_TYPE is None or type_of(row['file']) == INSTANCE_TYPE:
        if correct_in_all_files(row['file'], data):
            total_time += row['time']
            total_recsteps += row['recsteps']
            instances += 1


time_per_instance = total_time / instances
recsteps_per_instance = total_recsteps / instances

print(f"time per instance: {time_per_instance}")
print(f"recursive steps per instance: {recsteps_per_instance}")
print(f"time per recursive step: {1000 * time_per_instance / recsteps_per_instance}")
