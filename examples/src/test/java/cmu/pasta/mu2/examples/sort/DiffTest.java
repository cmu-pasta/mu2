package cmu.pasta.mu2.examples.sort;

import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.Size;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.difffuzz.Comparison;
import edu.berkeley.cs.jqf.fuzz.difffuzz.DiffFuzz;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static cmu.pasta.mu2.examples.sort.SortTest.testSort;

@RunWith(JQF.class)
public class DiffTest {
    protected static final int MAX_SIZE = 160;
    protected static final int MIN_ELEMENT = 0;
    protected static final int MAX_ELEMENT = 10;

    @DiffFuzz(cmp = "compare")
    public List<Integer> testBubbleSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        List<Integer> toReturn = new BubbleSort().sort(input);
        return toReturn;
    }

    @Fuzz
    public void fuzzBubbleSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        new BubbleSort().sort(input);
    }


    @DiffFuzz(cmp = "noncompare")
    public List<Integer> testBubbleSortNonCompare(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        List<Integer> toReturn = new BubbleSort().sort(input);
        return toReturn;
    }

    @DiffFuzz(cmp = "compare")
    public List<Integer> testTimSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        List<Integer> toReturn = new TimSort().sort(input);
        return toReturn;
    }

    @DiffFuzz(cmp = "noncompare")
    public List<Integer> testTimSortNonCompare(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        List<Integer> toReturn = new TimSort().sort(input);
        return toReturn;
    }

    @Fuzz
    public void fuzzTimSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        new TimSort().sort(input);
    }

    @Comparison
    public static Boolean compare(List l1, List l2) {
        if(l1 == null || l2 == null) return false;
        if(l1.size() != l2.size()) return false;
        for(int c = 0; c < l1.size(); c++) {
            if(!l1.equals(l2)) return false;
        }
        return true;
    }

    @Comparison
    public static Boolean noncompare(List l1, List l2) {
        return true;
    }

}
