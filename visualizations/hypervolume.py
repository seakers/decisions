import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from pygmo import hypervolume



def check_domination(file_name, non_dominated_file, f1_name='first', f2_name='second'):

    df_pop = pd.read_csv(file_name, sep=',')
    df_pop['GROUP'] = f1_name
    df_non_dom = pd.read_csv(non_dominated_file, sep=',')
    df_non_dom['GROUP'] = f2_name
    # print(df_pop)
    # print(df_non_dom)

    df_pop.loc[:, df_pop.columns == 'science'] -= 1.0
    df_pop.loc[:, df_pop.columns == 'science'] *= -1.0

    df_non_dom.loc[:, df_non_dom.columns == 'science'] -= 1.0
    df_non_dom.loc[:, df_non_dom.columns == 'science'] *= -1.0

    hyp_pop = hypervolume(df_pop[['science','cost', 'data_continuity']].values)
    print(file_name)
    print(hyp_pop.compute([1, 10000, 5000]), '\n')

    hyp_non_dom_pop = hypervolume(df_non_dom[['science','cost', 'data_continuity']].values)
    print(non_dominated_file)
    print(hyp_non_dom_pop.compute([1, 10000, 5000]), '\n')

    df_combined = df_non_dom.append(df_pop, ignore_index=True)
    hyp_combined_pop = hypervolume(df_combined[['science','cost', 'data_continuity']].values)
    print(hyp_combined_pop.compute([1, 10000, 5000]))

    # pnt = hyp_combined_pop.greatest_contributor([1, 10000, 5000])
    # print(df_combined.iloc[pnt])

    contributors = hyp_combined_pop.contributions([1, 10000, 5000])
    # print(contributors)

    for idx, cont in enumerate(contributors):
        if cont > 0.001:
            print(idx, df_combined.iloc[idx])

