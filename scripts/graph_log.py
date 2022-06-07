#emits graph(s) interpretations of the fuzz.log output
import argparse
import re
import numpy as np
import matplotlib.pyplot as plt
import subprocess

#dumb "parser" that  picks out the current input and its source
def parseFile(file_name):
    source_info = {}
    reason_info = {}
    with open(file_name, 'r') as file:
        for line in file.readlines():
            #takes all relevant information (id, source, reasons) and places them into capture groups
            res = re.search(r"Saved - .*id_(\d+) src:(\d+),havoc:\d+ ((?:\+[a-zA-Z]*(?: |$))*)(?:$|\+\d+ (mutants))",line)
            if res is not None:
                info = res.groups()
                try:
                    source_info[int(info[1])].append(int(info[0]))
                except KeyError:
                    source_info[int(info[1])] = [int(info[0])]
                reasons = ""
                if info[2] is not None:
                    reasons = info[2]
                #if one of the reasons is killed mutants
                if info[3] is not None:
                    reasons+= "+mutants"
                try:
                    reason_info[reasons].append(int(info[0]))
                except KeyError:
                    reason_info[reasons] = [int(info[0])]

    return source_info, reason_info

def emit_dot(data,dot_file,out_file):
    with open(dot_file,"w") as file:
        file.write("digraph D { \n")
        for src in data:
            for res in data[src]:
                file.write(f"   {src}->{res}\n")
        file.write("}")
    try:
        subprocess.call(f"dot {dot_file} -Tpng -o {out_file}",shell=True)
    except FileNotFoundError:
        print("ERROR: graphviz dot does not appear to be installed")

def plot_freq_data(sources,reasons,out_file):
    reason_total_children = dict.fromkeys(reasons.keys(),0)
    for key in reasons:
        for i in reasons[key]:
            try:
                reason_total_children[key] += len(sources[i])
            except KeyError:
                pass
    labels = list(reasons.keys())
    children_per_reason = list(reason_total_children.values())
    inputs_per_reason = list(map(len,reasons.values()))
    x = list(np.arange(len(labels)))
    print(inputs_per_reason)
    plt.figure(figsize=(12,3))
    plt.bar(list(map(lambda y: y-0.20,x)),children_per_reason, width=0.4,label='Number of children inputs generated with this reason have')
    plt.bar(list(map(lambda y: y+0.20,x)),inputs_per_reason, width=0.4,label='Number inputs saved for this reason')
    plt.xticks(x, labels)

    plt.legend()
    plt.ylabel('# inputs')
    plt.savefig(out_file)


if __name__ == "__main__":
    #the argument parsing takes much more time than the rest of the program (for small-ish inputs), annoyingly
    parser = argparse.ArgumentParser()
    parser.add_argument("input_log", type=str,help="log to be parsed and graphed")
    parser.add_argument('-oc', '--chartout', nargs='?', const='chart.png', default="chart.png", help='output for the bar chart representation of the logs')
    parser.add_argument('-og', '--graphout', nargs='?', const='graph.png', help='output for digraph representation of the logs')
    parser.add_argument('-od', '--dotout', nargs='?', const='/tmp/graph.dot', help='intermediate output for graphiv dot representation of the logs')
    args = parser.parse_args()

    sources,reasons = parseFile(args.input_log)
    if args.graphout is not None or args.dotout is not None:
        emit_dot(sources,args.dotout,args.graphout)
    plot_freq_data(sources,reasons,args.chartout)
