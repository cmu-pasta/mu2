def getUnkilledMutants(benchmark, tests):
    liveMutants = []
    for i in range(tests):
        liveMutants.append(set([]))
        with open("../three_hours/repro-out/" + benchmark + "-results/mutate-results-" + str(i + 1) + "/mutant-report.csv", "r") as f:
            lines = f.readlines()
            for line in lines:
                if "Alive" in line:
                    liveMutants[-1].add(line.split(",")[0].replace("::", ",").replace(" ", ","))
    return set.intersection(*liveMutants)

#print("ChocoPy Mu2 Unkilled Mutants:")
for mutant in getUnkilledMutants("ChocoPy", 20):
    print(mutant)
#print("Jackson Unkilled Mutants:")
for mutant in getUnkilledMutants("jacksonjson", 20):
    print(mutant)
#print("Tomcat Unkilled Mutants:")
for mutant in getUnkilledMutants("apachetomcat", 20):
    print(mutant)
#print("Gson Unkilled Mutants:")
for mutant in getUnkilledMutants("gson", 20):
    print(mutant)
#print("Rhino Unkilled Mutants:")
for mutant in getUnkilledMutants("rhino", 20):
    print(mutant)
#print("Closure Unkilled Mutants:")
for mutant in getUnkilledMutants("closure", 10):
    print(mutant)
