import plotly.express as px
from plotly.subplots import make_subplots
import plotly.graph_objects as go
import numpy as np
import pandas as pd
from pygmo import hypervolume


marker_size = 4







def get_3d_merge_trace(df):
    trace = go.Scatter3d(name='test',
                         x=df['data_continuity'],
                         y=df['science'],
                         z=df['cost'],
                         mode='markers',
                         marker={
                             'size': marker_size,
                             'color': df['COLOR']
                         },

                         )
    return trace

def get_3d_merge_name_trace(df):
    trace = go.Scatter3d(name='test',
                         x=df['data_continuity'],
                         y=df['science'],
                         z=df['cost'],
                         mode='markers',
                         marker={
                             'size': marker_size,
                             'color': df['COLOR']
                         },
                         hovertext=df['STRING'],
                         hoverlabel={
                             'bgcolor': df['COLOR']
                         }

                         )
    return trace


def get_3d_trace(df):
    colors = df['COLOR'].tolist()
    print(colors)
    trace = go.Scatter3d(name=df['NAME'].tolist()[0],
                         x=df['data_continuity'],
                         y=df['science'],
                         z=df['cost'],
                         mode='markers',
                         marker={
                             'size': marker_size,
                             'color': colors,
                             'symbol': df['SYMBOL']
                         },
                         hovertemplate= '<b>Design</b><br>'+df['STRING']+'<br><b>Scores</b><br>Science: %{y:.5f}<br>Cost: %{z:$.2f}<br>Data Continuity: %{z:.2f}<br><extra></extra>',
                         hovertext=df['STRING'],


                         )
    return trace

def get_3d_trace_basic(df):
    trace = go.Scatter3d(name='test',
                         x=df['data_continuity'],
                         y=df['science'],
                         z=df['cost'],
                         mode='markers',
                         marker={
                             'size': marker_size,
                         },


                         )
    return trace


