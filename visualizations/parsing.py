import json
import os
import pandas as pd




def parse_reliability_file(reliability_file):
    with open(reliability_file) as d_file:
        file_data = json.load(d_file)
        data = []
        for design in file_data:
            reliability_score = design['reliability']
            mass_score = design['mass']
            design_str = design['design']
            data.append([float(reliability_score), float(mass_score), design_str])
        df = pd.DataFrame(data, columns=['reliability', 'mass', 'STRING'])
        return df



def parse_design_file(design_file):
    with open(design_file) as d_file:
        file_data = json.load(d_file)
        data = []
        for design in file_data:
            design_string = ''
            science = 0
            cost = 0
            data_continuity = 0
            for item in design:
                if "elements" in item:
                    design_string += (get_satellite_text(item) + '\n<br>')
                elif "science" in item:
                    science = item['science']
                    cost = item['cost']
                    data_continuity = item['data_continuity']
            data.append([float(science), float(cost), float(data_continuity), design_string])
        df = pd.DataFrame(data, columns=['science', 'cost', 'data_continuity', 'STRING'])
        print(df)
        return df



def get_satellite_text(sat):
    final_text = sat['orbit'] + ' | ('
    for idx, instrument in enumerate(sat['elements']):
        name = instrument['name']
        if idx == 0:
            final_text += (name)
        else:
            final_text += (', ' + name)
    final_text += ')'
    return final_text







