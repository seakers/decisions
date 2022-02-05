import plotly.graph_objects as go
import plotly.express as px
import json
import pandas as pd
import os
from pygmo import hypervolume
import pygmo as pg

expert_formulation_designs = '/home/gabe/repos/seakers/reliability/results/design/designs.json'
add_formulation_designs = '/home/gabe/repos/seakers/decisions/gnc_formulation/designs/designs.json'


expert_formulation_design_dir = '/home/gabe/repos/seakers/reliability/results/design/'
add_formulation_design_dir = '/home/gabe/repos/seakers/decisions/gnc_formulation/designs/'



def get_non_dominated_data(csv_dir, color):
    points = []
    for filename in os.listdir(csv_dir):
        if('designs_paper' not in filename):
            continue
        with open(csv_dir + filename) as d_file:
            file_data = json.load(d_file)
            for design in file_data:
                points.append([design['reliability']* -1, design['mass']])
    ndf, dl, dc, ndr = pg.fast_non_dominated_sorting(points=points)
    x_vals = []
    y_vals = []
    colors = []
    for idx in ndf[0]:
        point = points[idx]
        x_vals.append(point[0]*-1)
        y_vals.append(point[1])
        colors.append(color)
        print(points[idx])
    df = pd.DataFrame()
    df['RELIABILITY (#9s)'] = x_vals
    df['MASS (adimensional)'] = y_vals
    df['Formulation'] = colors
    return df


def find_total_pareto_front():
    df1 = get_non_dominated_data(expert_formulation_design_dir, "Expert")
    df2 = get_non_dominated_data(add_formulation_design_dir, "ADD")
    df1 = df1.append(df2, ignore_index=True)
    fig = px.scatter(df1, x="RELIABILITY (#9s)", y="MASS (adimensional)", color="Formulation", title="ADD Formulation vs Expert Formulation - Combined Pareto Front")
    fig.update_layout(font=dict(
        size=18
    ))
    fig.show()






if __name__ == '__main__':
    find_total_pareto_front()
