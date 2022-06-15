package cmu.pasta.mu2.fuzz;

import cmu.pasta.mu2.MutationInstance;
import cmu.pasta.mu2.util.ArraySet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileMutantFilter implements MutantFilter{
    ArraySet ids = new ArraySet();

    FileMutantFilter(String fileName) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(fileName));
        while(scanner.hasNextInt()){
            ids.add(scanner.nextInt());
        }
        scanner.close();

    }

    @Override
    public List<MutationInstance> filterMutants(List<MutationInstance> toFilter) {
        List<MutationInstance> muts = new ArrayList<>();
        for(MutationInstance m : toFilter){
            if(ids.contains(m.id)){
                muts.add(m);
            }
        }
        return muts;
    }
}
