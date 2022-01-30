import pandas as pd
import sys

data = []

for i in range(1, len(sys.argv)):
    df = pd.read_csv(sys.argv[i], delimiter=';', index_col=False)
    values = []

    for index, row in df.iterrows():
        if row['verified'] == 'correct':
            values.append(row['time'])

    values.sort()

    data.append(values)

compare_df = pd.DataFrame(data).transpose()
compare_df.set_axis([chr(65 + i) + "-sorted" for i in range(len(data))], axis=1, inplace=True)

compare_df.to_csv('compare-solvers.csv', index=False, sep=';')


