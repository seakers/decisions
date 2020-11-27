import os

from three_dimensions import plot_pareto_front, plot_nfe_overlay_json, plot_pareto_overlap, scatter_3d_figure, plot_three_nfe, plot_nfe_overlay, plot_3_nfe_overlay, plot_3_separate_nfe_overlay
from hypervolume import check_domination

from parsing import parse_design_file

def run():






    add_file_500_1_design = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/500/designs_1.json'
    add_file_500_1 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/500/pop_1.csv'
    add_file_1000_1 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/1000/pop_1.csv'
    add_file_2000_1 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/2000/pop_1.csv'

    # HIGH PARTITIONING MUTATION PROBABILITY
    add_file_500_2_design = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/500/designs_2.json'
    add_file_500_2 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/500/pop_2.csv'
    add_file_1000_2 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/1000/pop_2.csv'
    add_file_2000_2 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/2000/pop_2.csv'

    # HIGH PARTITIONING MUTATION PROBABILITY + N SUBGROUP SPLIT OPERATOR
    add_file_300_3 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/300/pop_3.csv'
    add_file_500_3 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/500/pop_3.csv'
    add_file_1000_3 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/1000/pop_3.csv'
    add_file_2000_3 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/2000/pop_3.csv'

    # HIGH PARTITIONING MUTATION PROBABILITY + N SUBGROUP SPLIT OPERATOR + NEW DOWN-SELECTING MUTATION OPERATOR
    add_file_500_4_design = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/500/designs_4.json'
    add_file_2000_4_design = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/2000/designs_4.json'
    add_file_300_4 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/300/pop_4.csv'
    add_file_500_4 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/500/pop_4.csv'
    add_file_1000_4 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/1000/pop_4.csv'
    add_file_2000_4 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/2000/pop_4.csv'


    add_file_500 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/500/pop_0.csv'
    add_file_1000 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/1000/pop_0.csv'
    add_file_2000 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/add/2000/pop_0.csv'


    selecting_file_500_json = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/selecting/500/designs_0.json'
    selecting_file_1000_json = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/selecting/1000/designs_0.json'
    selecting_file_2000_json = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/selecting/2000/designs_0.json'
    selecting_file_200 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/selecting/200/pop_0.csv'
    selecting_file_300 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/selecting/300/pop_1.csv'
    selecting_file_500 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/selecting/500/pop_0.csv'
    selecting_file_1000 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/selecting/1000/pop_0.csv'
    selecting_file_2000 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/selecting/2000/pop_0.csv'

    # plot_3_separate_nfe_overlay(selecting_file_500, add_file_500, add_file_1000, add_file_2000)

    # plot_3_nfe_overlay(selecting_file_200, selecting_file_300, selecting_file_500)


    add_decadal2007_50_designs = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal2007/add/50/designs_0.json'
    add_decadal2007_50_designs_1 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal2007/add/50/designs_1.json'
    add_decadal2007_300_designs = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal2007/add/300/designs_0.json'
    add_decadal2007_500_designs = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal2007/add/500/designs_0.json'
    add_decadal2007_1000_designs = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal2007/add/1000/designs_0.json'
    add_decadal2007_2000_designs = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal2007/add/2000/designs_0.json'
    add_decadal2007_2000_designs_1 = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal2007/add/2000/designs_1.json'



    # parse_design_file(add_file_500_1_design)


    # ---------------- HERE
    # first param: blue
    # second param: green
    # plot_nfe_overlay(selecting_file_2000, add_file_2000_1, 'SEQUENTIAL (SELECTING)', 'COMBINED')
    # plot_nfe_overlay(selecting_file_2000, add_file_2000_2, 'SEQUENTIAL (SELECTING)', 'COMBINED')
    # plot_nfe_overlay(add_file_2000_4, selecting_file_2000, '<br>ADD - HIGH PARTITIONING MUTATION + NEW OPERATOR', '<br>SELECTING')
    #
    #
    # check_domination(add_decadal2007_300_designs, add_decadal2007_500_designs, "SELECTING", "COMBINED")

    plot_nfe_overlay_json(add_decadal2007_2000_designs, add_decadal2007_2000_designs_1, 'ADD - 2000 NFE - POP 0', 'ADD - 2000 NFE - POP 1', 'Decadal Survey 2007')



    # plot_three_nfe(add_file_500_1, add_file_1000_1, add_file_2000_1)


    selecting_file = '/home/gabe/repos/seakers/decisions/eos_formulation/decadal/selecting/500/pop/pop_0.csv'


    save_fileD = '/home/gabe/Dropbox/Research/ADD/selecting_ga_decadal_non_dominated_500.html'




    # scatter_3d_figure(add_file)

    # plot_pareto_overlap(add_file, selecting_file, "ADD GA vs SOLVING SELECTION", save_fileD)
















if __name__ == '__main__':
    run()

