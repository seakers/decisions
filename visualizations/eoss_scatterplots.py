import plotly.graph_objects as go
import plotly.express as px
import json
import pandas as pd
import os
from pygmo import hypervolume
import pygmo as pg

expert_formulation = '/home/gabe/repos/seakers/eoss-search/results/moea1/designs/'
add_formulation = '/home/gabe/repos/seakers/decisions/eos_formulation/rrmoea1/designs/'



def get_non_dominated_add_data(data_dir):
    points = []
    for filename in os.listdir(data_dir):
        with open(data_dir + filename) as d_file:
            file_data = json.load(d_file)
            for design in file_data:
                for design_attr in design:
                    if 'science' in design_attr:
                        points.append([design_attr['science']* -1, design_attr['cost'], design_attr['data_continuity']])
    ndf, dl, dc, ndr = pg.fast_non_dominated_sorting(points=points)
    x_vals = []
    y_vals = []
    z_vals = []
    colors = []
    for idx in ndf[0]:
        point = points[idx]
        x_vals.append(point[1])   # x - cost
        y_vals.append(point[0]*-1)# y - science
        z_vals.append(point[2])   # z - data continuity
        colors.append("ADD")
    df = pd.DataFrame()
    df['Science (normalized)'] = y_vals
    df['Cost (millions)'] = x_vals
    df['Data Continuity'] = z_vals
    df['Formulation'] = colors
    return df.drop_duplicates()


def get_non_dominated_expert_data(csv_dir):
    points = []
    for filename in os.listdir(csv_dir):
        with open(csv_dir + filename) as d_file:
            file_data = json.load(d_file)
            for design in file_data:
                points.append([design['science']* -1, design['cost'], design['data_continuity']])
    ndf, dl, dc, ndr = pg.fast_non_dominated_sorting(points=points)
    x_vals = []
    y_vals = []
    z_vals = []
    colors = []
    for idx in ndf[0]:
        point = points[idx]
        y_vals.append(point[0]*-1)
        x_vals.append(point[1])
        z_vals.append(point[2])
        colors.append('Expert')
    df = pd.DataFrame()
    df['Science (normalized)'] = y_vals
    df['Cost (millions)'] = x_vals
    df['Data Continuity'] = z_vals
    df['Formulation'] = colors
    return df.drop_duplicates()



def add_plot():
    df1 = get_non_dominated_add_data(add_formulation)
    fig = px.scatter(df1, x="Science (normalized)", y="Cost (millions)", color="Data Continuity",
                     title="ADD Formulation - Combined Pareto Front", range_x=[0, 0.8], range_y=[0, 3000], range_color=[0, 1200])
    fig.show()

def expert_plot():
    df1 = get_non_dominated_expert_data(expert_formulation)
    fig = px.scatter(df1, x="Science (normalized)", y="Cost (millions)", color="Data Continuity",
                         title="Expert Formulation - Combined Pareto Front", range_x=[0, 0.8], range_y=[0, 3000], range_color=[0, 1200])
    fig.show()

def combined_plot():
    df1 = get_non_dominated_add_data(add_formulation)
    df2 = get_non_dominated_expert_data(expert_formulation)

def create_plots():
    df1 = get_non_dominated_add_data(add_formulation)
    print(df1)
    df2 = get_non_dominated_expert_data(expert_formulation)
    print(df2)
    df1 = df1.append(df2, ignore_index=True)
    print(df1)
    fig = px.scatter(df1, x="Cost (millions)", y="Science (normalized)", color="Formulation",
                     title="ADD Formulation vs Expert Formulation - Combined Pareto Front", range_x=[0, 1], range_y=[0, 3000])
    fig.show()


    # df1 = get_non_dominated_data(expert_formulation, "Expert")
    # print(df1.drop_duplicates())
    # df2 = get_non_dominated_data(add_formulation, "ADD")
    # print(df2)
    # exit(0)
    # df1 = df1.append(df2, ignore_index=True)
    # fig = px.scatter(df1, x="Science (normalized)", y="Cost (millions)", color="Formulation", title="ADD Formulation vs Expert Formulation - Combined Pareto Front")
    # fig.show()




if __name__ == '__main__':
    add_plot()
    expert_plot()
