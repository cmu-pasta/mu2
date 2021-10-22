from matplotlib_venn import venn2
from scipy import stats
import matplotlib.pyplot as plt
import numpy as np
import argparse
import os

def get_killed_mutants(file_name):
    with open(file_name, "r") as f:
        lines = f.readlines()

    i = 0
    killed_mutants = set()
    while i < len(lines):
        if "Running Mutant" in lines[i]:
            killed = False
            mutant_name = lines[i][lines[i].index("Mutant"):].strip()
            i += 1
            while i < len(lines) and "Running Mutant" not in lines[i]:
                if "FAILURE" in lines[i]:
                    killed = True
                i += 1
        if killed:
            killed_mutants.add(mutant_name)
    return killed_mutants

def get_unique(zest_killed, mutate_killed):

    zest_uniq = zest_killed - mutate_killed
    mutate_uniq = mutate_killed - zest_killed
    both_found = zest_killed.intersection(mutate_killed)

    print("\nUnique to Zest:")
    print(*list(zest_uniq), sep = "\n")
    print("\nUnique to Mu2:")
    print(*list(mutate_uniq), sep = "\n")
    print("\nBoth found another", len(both_found))

    return len(zest_uniq), len(mutate_uniq), len(both_found)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--filters_dir", required=True, type=str)
    parser.add_argument("--num_experiments", required=True, type=int)
    parser.add_argument("--output_img", required=True, type=str)

    args = parser.parse_args()
    zest_killed = []
    mutate_killed = []

    zest_unique_count = []
    mutate_unique_count = []
    both_found = []
    for i in range(1, args.num_experiments + 1):
        zest = get_killed_mutants(os.path.join(args.filters_dir, "zest-filter-" + str(i) + ".txt"))
        mutate = get_killed_mutants(os.path.join(args.filters_dir, "mutate-filter-" + str(i) + ".txt"))

        zest_killed.append(len(zest))
        mutate_killed.append(len(mutate))

        zest_unique, mutate_unique, both = get_unique(zest, mutate)

        zest_unique_count.append(zest_unique)
        mutate_unique_count.append(mutate_unique)
        both_found.append(both)

    print("---------------------------------------------------\n\n")
    print("Zest Killed Counts: ")
    print(zest_killed)
    print("Mu2 Killed Counts: ")
    print(mutate_killed)
    print("Zest Mutant Killed Summary: ")
    print(stats.describe(zest_killed))
    print("Mu2 Mutant Killed Summary: ")
    print(stats.describe(mutate_killed))
    print("Zest Unique Mutant Killed Summary: ")
    print(stats.describe(zest_unique_count))
    print("Mu2 Unique Mutant Killed Summary: ")
    print(stats.describe(mutate_unique_count))
    print("Both Killed Summary: ")
    print(stats.describe(both_found))
    print("T-test between Killed Counts: ")
    print(stats.ttest_ind(zest_killed, mutate_killed))


    fig = venn2(subsets = (np.mean(zest_unique_count), np.mean(mutate_unique_count), np.mean(both_found)), set_labels = ["Zest           ", "           Mu2"])
    plt.savefig(args.output_img)

