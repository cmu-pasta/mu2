import argparse
import pandas as pd
import matplotlib.pyplot as plt

def read_plot_data(input_plot_data):
    return pd.read_csv(input_plot_data, skipinitialspace=True)


def plot_mutants(df, output_file):
    plt.plot(df.index, df['found_muts'], label="Found Mutants")
    if 'seen_muts' in df.columns:
        plt.plot(df.index, df['seen_muts'], label="Seen Mutants")
    if 'infected_muts' in df.columns:
        plt.plot(df.index, df['infected_muts'], label="Infected Mutants")
    plt.plot(df.index, df['dead_muts'], label="Killed Mutants")
    plt.plot(df.index, df['run_muts'], label="Run Mutants (moving average)")
    plt.title("Mutant Plots")
    plt.xlabel("Trials")
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

