import argparse
import matplotlib.pyplot as plt
import subprocess

#dumb "parser" that  picks out the current input and its source
def parseFile(file_name):
    source_info = []
    with open(file_name, 'r') as file:
        for line in file.readlines():
            words = line.split(" ")
            if words[0] != "Saved":
                continue
            current_input = int(words[2][-6:])
            source_input = words[3].split(',')[0].split(":")
            if source_input[0] !='src':
                continue
            source_input = int(source_input[1])
            source_info.append((source_input,current_input))
    return source_info

def count_source(data):
    dic = {}
    for (src,res) in data:
        try:
            dic[src] += 1
        except KeyError:
            dic[src] = 1
    return dic

def plot_freq_data(freqs,out_file):
    labels = list(freqs.keys())
    vals = list(freqs.values())
    plt.bar(labels, vals, width=1)
    plt.ylabel('# inputs generated from')
    plt.savefig(out_file, bbox_inches='tight')

def emit_dot(data,dot_file,out_file):
    with open(dot_file,"w") as file:
        file.write("digraph D { \n")
        for (src,res) in data:
            file.write(f"   {src}->{res}\n")
        file.write("}")
    try:
        subprocess.call(f"dot {dot_file} -Tpng -o {out_file}",shell=True)
    except FileNotFoundError:
        print("ERROR: graphviz dot does not appear to be installed")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("input_log", type=str,help="log to be parsed and graphed")
    parser.add_argument('-oc', '--chartout', nargs='?', const='chart.png', default="chart.png", help='output for the bar graph representation of the logs')
    parser.add_argument('-og', '--graphout', nargs='?', const='graph.png', default="graph.png", help='output for digraph representation of the logs')
    parser.add_argument('-od', '--dotout', nargs='?', const='/tmp/graph.dot', default="/tmp/graph.dot", help='intermediate output for graphiv dot representation of the logs')
    args = parser.parse_args()

    data = parseFile(args.input_log)
    emit_dot(data,args.dotout,args.graphout)

    freqs = count_source(data)
    freqs = dict(sorted(freqs.items(), key=lambda item: item[1], reverse=True))
    plot_freq_data(freqs,args.chartout)