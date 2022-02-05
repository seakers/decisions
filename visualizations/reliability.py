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

def combine_data(csv_dir):
    masses = []
    reliabilities = []
    reliabilities2 = []
    points = []
    for filename in os.listdir(csv_dir):
        if('designs_paper' not in filename):
            continue
        with open(csv_dir + filename) as d_file:
            file_data = json.load(d_file)
            for design in file_data:
                masses.append(design['mass'])
                reliabilities.append(design['reliability']* -1)
                reliabilities2.append(design['reliability'])
                points.append([design['reliability']* -1, design['mass']])

    ndf, dl, dc, ndr = pg.fast_non_dominated_sorting(points=points)
    x_vals = []
    y_vals = []
    df = pd.DataFrame()
    for idx in ndf[0]:
        point = points[idx]
        x_vals.append(point[0]*-1)
        y_vals.append(point[1])
        print(points[idx])
    df['RELIABILITY'] = x_vals
    df['MASS'] = y_vals
    fig = px.scatter(df, x='RELIABILITY', y='MASS', title="Mass (adimensional) vs # 9's in R")
    fig.show()



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
    df['RELIABILITY'] = x_vals
    df['MASS'] = y_vals
    df['Formulation'] = colors
    return df


def find_total_pareto_front():
    df1 = get_non_dominated_data(expert_formulation_design_dir, "Expert")
    df2 = get_non_dominated_data(add_formulation_design_dir, "ADD")
    df1 = df1.append(df2, ignore_index=True)
    fig = px.scatter(df1, x="RELIABILITY", y="MASS", color="Formulation", title="")
    fig.show()






def parse_reliability_file(reliability_file):
    with open(reliability_file) as d_file:
        file_data = json.load(d_file)
        data = []
        for design in file_data:
            reliability_score = design['reliability']
            mass_score = design['mass']
            data.append([float(reliability_score), float(mass_score)])
        df = pd.DataFrame(data, columns=['reliability', 'mass'])
        return df


def plot_reliability(design_file):
    df = parse_reliability_file(design_file)
    fig = px.scatter(df, x='reliability', y='mass', title="Mass (adimensional) vs # 9's in R")
    fig.show()




if __name__ == '__main__':
    # plot_reliability(expert_formulation_designs)
    # plot_reliability(add_formulation_designs)
    find_total_pareto_front()
