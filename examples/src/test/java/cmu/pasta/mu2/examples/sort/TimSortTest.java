package cmu.pasta.mu2.examples.sort;

import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.Size;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(JQF.class)
public class TimSortTest {

    @Fuzz
    public void fuzzTimSort(@Size(max=SortTest.MAX_SIZE) List<Integer> input) {
        SortTest.testSort(new TimSort(), input);
    }

    @Test
    public void testTimSort() {
        TimSort sort = new TimSort();
        SortTest.testSort(sort, 4, 8, 15, 16, 23, 42);
        SortTest.testSort(sort, 4, 8, 15, 16, 23, 42, 108);
        SortTest.testSort(sort, 48, 15, 162, 342);
        SortTest.testSort(sort, 481, 5, 16, 2, 34, 2);
    }
}
