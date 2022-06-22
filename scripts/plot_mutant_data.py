import argparse
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

def read_plot_data(input_plot_data):
    return pd.read_csv(input_plot_data, skipinitialspace=True)


def plot_mutants(df, output_file):
    plt.plot(df['total_time'], df['found_muts'], label="Found Mutants")
    plt.plot(df['total_time'], df['seen_muts'], label="Seen Mutants")
    plt.plot(df['total_time'], df['dead_muts'], label="Killed Mutants")
    plt.plot(df['total_time'], df['run_muts'], label="Run Mutants (moving average)")

    func_of_best_fit = np.poly1d(np.polyfit(df['total_time'], df['run_muts'], deg=1))
    plt.plot(df['total_time'], list(map(func_of_best_fit,df['total_time'])),label="Run Mutants (Line Of Best Fit)")

    plt.title("Mutant Plots")
    plt.xlabel("Total Time (ms)")
    plt.ylabel("Number of Mutants")
    plt.legend(loc='upper right', bbox_to_anchor=(1.6, 1.02), fancybox=True)
    plt.savefig(output_file, bbox_inches='tight')

def print_summary_data(df):
    print(f"Total Found Mutants: {df['found_muts'].iloc[-1]}")
    print(f"Total Seen Mutants: {df['seen_muts'].iloc[-1]}")

    time = df['total_time'] .iloc[-1]
    ms = time % 1000
    total_s = time // 1000
    s = total_s % 60
    total_m = total_s // 60
    m = total_m % 60
    h = total_m // 60
    print(f"Total Time For Campaign (H:M:S:ms): {h}:{m}:{s}:{ms}")

    total_trials = df['valid_inputs'].iloc[-1] + df['invalid_inputs'].iloc[-1]
    map_time = df['map_time'].iloc[-1]
    print(f"Average mapping time per trial (mS): {map_time/total_trials}")
    print(f"Average total time per trial (mS): {time/total_trials}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("input_plot_data", type=str)
    parser.add_argument("output_file", type=str)
    args = parser.parse_args()

    df = read_plot_data(args.input_plot_data)
    plot_mutants(df, args.output_file)
    print_summary_data(df)
