from matplotlib_venn import venn2
from matplotlib.pyplot import savefig
from sys import argv
from numpy import add

prefix = "[INFO] Running Mutant "
trim = (lambda strng : strng[len(prefix):-1])

def getVals(i):
    zestFile = open("filters/zest-filter" + str(i) + ".txt", "r")
    mutateFile = open("filters/mutate-filter" + str(i) + ".txt", "r")

    zestLs = set([trim(strng) for strng in zestFile.readlines()])
    mutateLs = set([trim(strng) for strng in mutateFile.readlines()])

    zestUniq = zestLs - mutateLs
    mutateUniq = mutateLs - zestLs
    bothFound = zestLs.intersection(mutateLs)

    print("\nUnique to Zest:")
    print(*list(zestUniq), sep = "\n")
    print("\nUnique to Mu2:")
    print(*list(mutateUniq), sep = "\n")
    print("\nBoth found another", len(bothFound))
    
    return len(zestUniq), len(mutateUniq), len(bothFound)

av0 = int(argv[1])
zestUniques = 0.0
mutateUniques = 0.0
bothFoundElems = 0.0
for i in range(1, av0 + 1):
    zU, mU, bF = getVals(i)
    zestUniques += zU
    mutateUniques += mU
    bothFoundElems += bF

fig = venn2(subsets = (round(100 * zestUniques / av0) / 100.0, round(100 * mutateUniques / av0) / 100.0, round(100 * bothFoundElems / av0) / 100.0), set_labels = ["Zest           ", "           Mu2"])
savefig("venn.png")