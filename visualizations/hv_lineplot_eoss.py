import pandas as pd
import numpy as np
import os
import seaborn as sns
import matplotlib.pyplot as plt




def get_dataframe(csv_dir, col_name):
    df = pd.DataFrame()
    for filename in os.listdir(csv_dir):
        full_path = csv_dir + '/' + filename
        df = df.append(pd.read_csv(full_path), ignore_index=True)
    df.columns = ['NFE', col_name]
    return df




# Takes 2-D array of directories and labels
def plot_list(lines=[[]]):
    first_run = lines[0]
    first_dir = first_run[0]
    first_label = first_run[1]
    df = get_dataframe(first_dir, first_label)

    counter = 0
    for run in lines:
        if counter == 0:
            counter += 1
            continue
        dir_name = run[0]
        label = run[1]
        dft = get_dataframe(dir_name, label)
        df[label] = dft[label]
        counter += 1

    ax = sns.lineplot(x='NFE', y='value', hue='variable', data=pd.melt(df, ['NFE']))
    ax.plot()
    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()



if __name__ == '__main__':


    random_moea = '/home/gabe/repos/seakers/eoss-search/results/random/crossover'

    expert_moea1 = '/home/gabe/repos/seakers/eoss-search/results/moea1/crossover'


    add_moea1 = '/home/gabe/repos/seakers/decisions/eos_formulation/rrmoea1/crossover'
    add_moea2 = '/home/gabe/repos/seakers/decisions/eos_formulation/rrmoea2/crossover'


    items = [
        [expert_moea1, 'EXPERT_FORMULATION'],
        [add_moea1, 'ADD_FORMULATION_1']
    ]
    plot_list(items)
