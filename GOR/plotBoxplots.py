import matplotlib.pyplot as plt
import numpy as np
import argparse


def main():
    parser = argparse.ArgumentParser(description='Generate box plots')
    parser.add_argument('input_file', type=str, help='Input data file from GOR Validation')
    args = parser.parse_args()

    plot_header = args.input_file.split('_toPlot')[0]

    # Read data from file
    data = np.genfromtxt(args.input_file, delimiter='\t', dtype=None, names=('Q3', 'SOV', 'Q_H', 'Q_E', 'Q_C', 'SOV_H', 'SOV_E', 'SOV_C'))

    # Extract data for each protein
    Q3 = data['Q3']
    SOV = data['SOV']
    Q_H = data['Q_H']
    Q_E = data['Q_E']
    Q_C = data['Q_C']
    SOV_H = data['SOV_H']
    SOV_E = data['SOV_E']
    SOV_C = data['SOV_C']

    # Create boxplots
    plt.figure(figsize=(12, 8))

    plt.boxplot([Q3, SOV, Q_H, Q_E, Q_C, SOV_H, SOV_E, SOV_C])
    plt.xticks(range(1, len(data.dtype.names) + 1), data.dtype.names)
    plt.title(f'Box Plots for {plot_header} GOR-Validation')
    plt.xlabel('Proteins')
    plt.ylabel('Values')

    plt.savefig(f'{plot_header}_boxplot.png')
    plt.show()


if __name__ == '__main__':
    main()