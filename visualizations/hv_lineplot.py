import pandas as pd
import numpy as np
import os
import seaborn as sns
import matplotlib.pyplot as plt



add_dir = '/home/gabe/repos/seakers/decisions/gnc_formulation/crossover'
add_dir2 = '/home/gabe/repos/seakers/decisions/gnc_formulation/crossover2'
add_dir3 = '/home/gabe/repos/seakers/decisions/gnc_formulation/crossover3'
add_dir_500 = '/home/gabe/repos/seakers/decisions/gnc_formulation/crossover4'
add_dir_1000 = '/home/gabe/repos/seakers/decisions/gnc_formulation/crossover5'
add_dir_1000_hv = '/home/gabe/repos/seakers/decisions/gnc_formulation/crossover5_hv' ######
add_dir_2000 = '/home/gabe/repos/seakers/decisions/gnc_formulation/crossover6'




hand_crafted_dir = '/home/gabe/repos/seakers/reliability/results/random'



hand_crafted_random = '/home/gabe/repos/seakers/reliability/results/random'
hand_crafted_crossover = '/home/gabe/repos/seakers/reliability/results/crossover'
hand_crafted_crossover2 = '/home/gabe/repos/seakers/reliability/results/crossover2'
expert_formulation_500 = '/home/gabe/repos/seakers/reliability/results/crossover3'
expert_formulation_1000 = '/home/gabe/repos/seakers/reliability/results/crossover4'
expert_formulation_1000_strong = '/home/gabe/repos/seakers/reliability/results/crossover5'
expert_formulation_1000_strong_hv = '/home/gabe/repos/seakers/reliability/results/crossover5_hv' ######
expert_formulation_2000_strong = '/home/gabe/repos/seakers/reliability/results/crossover6'



data_dir = '/home/gabe/repos/seakers/decisions/hypervolumes'
save_dir = '/home/gabe/repos/seakers/decisions/visualizations/store/gnc/'



def get_dataframe(csv_dir, col_name):
    df = pd.DataFrame()
    for filename in os.listdir(csv_dir):
        full_path = csv_dir + '/' + filename
        df = df.append(pd.read_csv(full_path), ignore_index=True)
    df.columns = ['NFE', col_name]
    return df



def computer_nfe_1000_hv():
    df1 = get_dataframe(expert_formulation_1000_strong_hv, 'EXPERT_FORMULATION')
    df2 = get_dataframe(add_dir_1000_hv, 'ADD_FORMULATION')
    df1['ADD_FORMULATION'] = df2['ADD_FORMULATION']
    ax = sns.lineplot(x='NFE', y='value', hue='variable', data=pd.melt(df1, ['NFE']))
    ax.plot()
    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()


def compare_nfe_2000():
    df1 = get_dataframe(expert_formulation_2000_strong, 'EXPERT_FORMULATION')
    df2 = get_dataframe(add_dir_2000, 'ADD_FORMULATION')
    df1['ADD_FORMULATION'] = df2['ADD_FORMULATION']
    ax = sns.lineplot(x='NFE', y='value', hue='variable', data=pd.melt(df1, ['NFE']))
    ax.plot()
    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()


def compare_nfe_1000_strong():
    df1 = get_dataframe(expert_formulation_1000, 'EXPERT_FORMULATION')
    df2 = get_dataframe(add_dir_1000, 'ADD_FORMULATION')
    df3 = get_dataframe(expert_formulation_1000_strong, 'EXPERT_FORMULATION_2')
    df1['ADD_FORMULATION'] = df2['ADD_FORMULATION']
    df1['EXPERT_FORMULATION_2'] = df3['EXPERT_FORMULATION_2']
    ax = sns.lineplot(x='NFE', y='value', hue='variable', data=pd.melt(df1, ['NFE']))
    ax.plot()
    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()


def compare_nfe_1000():
    df1 = get_dataframe(expert_formulation_1000_strong, 'EXPERT_FORMULATION')
    df2 = get_dataframe(add_dir_1000, 'ADD_FORMULATION')
    df1['ADD_FORMULATION'] = df2['ADD_FORMULATION']
    ax = sns.lineplot(x='NFE', y='value', hue='variable', data=pd.melt(df1, ['NFE']))
    ax.plot()
    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()


def compare_nfe_500():
    df1 = get_dataframe(expert_formulation_500, 'EXPERT_FORMULATION')
    df2 = get_dataframe(add_dir_500, 'ADD_FORMULATION')
    df1['ADD_FORMULATION'] = df2['ADD_FORMULATION']
    ax = sns.lineplot(x='NFE', y='value', hue='variable', data=pd.melt(df1, ['NFE']))
    ax.plot()
    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()




def compare_plot2():
    df1 = get_dataframe(hand_crafted_crossover2, 'EXPERT_FORMULATION')
    df2 = get_dataframe(add_dir2, 'ADD_FORMULATION')
    df3 = get_dataframe(add_dir3, 'ADD_FORMULATION2')

    df1['ADD_FORMULATION'] = df2['ADD_FORMULATION']
    df1['ADD_FORMULATION2'] = df3['ADD_FORMULATION2']

    ax = sns.lineplot(x='NFE', y='value', hue='variable', data=pd.melt(df1, ['NFE']))
    ax.plot()
    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()




# Expert formulation vs ADD 1
def compare_plot():
    df1 = get_dataframe(hand_crafted_crossover2, 'EXPERT_FORMULATION')
    df2 = get_dataframe(add_dir2, 'ADD_FORMULATION')

    df1['ADD_FORMULATION'] = df2['ADD_FORMULATION']
    print(df1)

    ax = sns.lineplot(x='NFE', y='value', hue='variable', data=pd.melt(df1, ['NFE']))
    ax.plot()
    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()



def test_plot():
    df1 = get_dataframe(hand_crafted_dir, 'RANDOM')
    df2 = get_dataframe(hand_crafted_crossover, 'CROSSOVER')
    df3 = get_dataframe(hand_crafted_crossover2, 'CROSSOVER2')
    df4 = get_dataframe(add_dir, 'ADD')

    df1['CROSSOVER'] = df2['CROSSOVER']
    df1['CROSSOVER2'] = df3['CROSSOVER2']
    df1['ADD'] = df4['ADD']

    print(df1)


    ax = sns.lineplot(x='NFE', y='value', hue='variable', data=pd.melt(df1, ['NFE']))
    ax.plot()
    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()






def create_line_plot():
    print('--> CREATING LINE PLOT')

    df = pd.DataFrame()
    for filename in os.listdir(hand_crafted_dir):
        print(filename)
        if filename == 'config1':
            continue
        full_path = hand_crafted_dir + '/' + filename
        df = df.append(pd.read_csv(full_path))

    nfe_list = df['NFE'].tolist()
    hv_list = df[' HV'].tolist()

    ax = sns.lineplot(x=nfe_list, y=hv_list)



    ax.plot()


    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()



    return 0



if __name__ == '__main__':
    # compare_plot2()
    computer_nfe_1000_hv()