from mplcursors import cursor
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import argparse

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("mutant_execution_file")


    args = parser.parse_args()
    df = pd.read_csv(args.mutant_execution_file)
    df['execution_count_log'] = np.log10(df['execution_count'])

    ax = sns.relplot(data=df, x='execution_count_log', y='mutant', hue='killed')
    plt.title("Mutant Execution Counts")
    plt.xlabel("Execution Count")
    plt.ylabel("Mutants")
    plt.yticks([])
    cursor(hover=True)
    plt.show()
