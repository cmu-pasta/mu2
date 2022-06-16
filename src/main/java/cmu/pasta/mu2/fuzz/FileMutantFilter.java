package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.util.ArraySet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Filters an input list of MutationInstances based on if they are in a given file or not.
 * Each line in the file can either take the form of ClassName:MutatorName:sequenceIdx or FileName:LineNumber
 * e.g, an example of a valid file is
 * <pre>
 * {@code
 *  TimSort.java:593
 *  sort.TimSort$ComparableTimSort:I_ADD_TO_SUB:28
 * }
 * </pre>
 */
public class FileMutantFilter implements MutantFilter{
    //used for memoizing lookup into the file
    ArraySet haveSeen = new ArraySet();
    ArraySet allowed = new ArraySet();

    //lines that have not been associated with a mutation instance
    List<String> unAccountedForLines = new LinkedList<>();


    FileMutantFilter(String fileName) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(fileName));
        while(scanner.hasNextLine()){
            unAccountedForLines.add(scanner.nextLine());
        }
    }

    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {
        List<MutationInstance> muts = new ArrayList<>();
        for(MutationInstance m : toFilter){
            if(haveSeen.contains(m.id)){
                if(allowed.contains(m.id)){
                    muts.add(m);
                }
            } else {
                haveSeen.add(m.id);
                ListIterator<String> iterator = unAccountedForLines.listIterator();
                while(iterator.hasNext()){
                    String currLine = iterator.next();
                    if(currLine.equals(m.className + ":" + m.mutator + ":" + m.sequenceIdx)
                            || currLine.equals(m.getFileName() +":" + m.getLineNum())) {
                        allowed.add(m.id);
                        muts.add(m);
                        iterator.remove();
                    }
                }
            }
        }
        return muts;
    }
}
