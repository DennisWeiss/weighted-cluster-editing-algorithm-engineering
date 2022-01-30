import pandas as pd
import sys


TIMEOUT = 30


def type_of(filename):
    if 'random' in filename:
        return 'random'
    if 'real-world' in filename:
        return 'real-world'
    if 'actionseq' in filename:
        return 'action-seq'


if len(sys.argv) < 3:
    raise Exception('Input filenames of both files need to be provided as console arguments')

data1 = pd.read_csv(sys.argv[1], delimiter=';', index_col=False)
data2 = pd.read_csv(sys.argv[2], delimiter=';', index_col=False)

compare = []

for index, row in data1.iterrows():
    row2 = data2[data2['file'] == row['file']]
    if row['verified'] == 'correct' and (not row2.empty and row2['verified'].item() == 'correct'):
        compare.append([
            row['file'],
            type_of(row['file']),
            row['time'] if row['verified'] == 'correct' else TIMEOUT,
            row2['time'].item() if not row2.empty and row2['verified'].item() == 'correct' else TIMEOUT,
            row['recsteps'] if row['verified'] == 'correct' else None,
            row2['recsteps'].item() if not row2.empty and row2['verified'].item() == 'correct' else None
        ])

pd.DataFrame(compare, columns=['file', 'Type', 'Atime', 'Btime', 'Arecsteps', 'Brecsteps']).to_csv('compare.csv', index=False, sep=';')

