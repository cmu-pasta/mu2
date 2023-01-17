import argparse
import os
import pandas as pd
import shutil

TIME = 43200

def get_num_files(exp_dir):
    df = pd.read_csv(os.path.join(exp_dir, "plot_data"))
    end_time = int(df.iloc[0]["# unix_time"]) + TIME
    print(end_time)
    rows = df[df["# unix_time"] == end_time]
    if len(rows) > 0:
        num_files = int(df[df["# unix_time"] == end_time].iloc[0][" paths_total"])
    else:
        num_files = int(df[" paths_total"].iloc[-1])
    return num_files

def copy_files(exp_dir, num_files):
    dest_dir = os.path.join(exp_dir, "time_constrained_corpus")
    if not os.path.isdir(dest_dir):
        os.mkdir(dest_dir)
    for n in range(num_files):
        len_file = len(str(n))
        file_name = "id_" + "0" * (6 - len_file) + str(n)
        src_file = os.path.join(exp_dir, "corpus", file_name)
        dest_file = os.path.join(dest_dir, file_name)
        shutil.copy(src_file, dest_file)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--exp_dir", required=True, type=str)

    args = parser.parse_args()
    num_files = get_num_files(args.exp_dir)
    copy_files(args.exp_dir, num_files)
