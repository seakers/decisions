from parsing import parse_reliability_file
import plotly.graph_objects as go
import plotly.express as px





def plot_reliability(design_file):
    df = parse_reliability_file(design_file)

    fig = px.scatter(df, x='reliability', y='mass', title="Mass (adimensional) vs # 9's in R")
    fig.write_html('/home/gabe/Dropbox/Research/ADD/add_reliability.html')
    fig.show()