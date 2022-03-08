import argparse
import pandas as pd
import matplotlib.pyplot as plt

def read_plot_data(input_plot_data):
    return pd.read_csv(input_plot_data, skipinitialspace=True)


def plot_mutants(df, output_file):
    df['total_time'] = df['total_time'] / 3600000.0
    plt.plot(df['total_time'], df['found_muts'], label="Found Mutants")
    plt.plot(df['total_time'], df['seen_muts'], label="Seen Mutants")
    plt.plot(df['total_time'], df['dead_muts'], label="Killed Mutants")
    plt.plot(df['total_time'], df['run_muts'], label="Run Mutants (moving average)")
    plt.title("Mutant Plots")
    plt.xlabel("Total Time (hours)")
    plt.ylabel("Number of Mutants")
    plt.legend(loc='upper right', bbox_to_anchor=(1.6, 1.02), fancybox=True)
    plt.savefig(output_file, bbox_inches='tight')

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("input_plot_data", type=str)
    parser.add_argument("output_file", type=str)
    args = parser.parse_args()

    df = read_plot_data(args.input_plot_data)
    plot_mutants(df, args.output_file)

