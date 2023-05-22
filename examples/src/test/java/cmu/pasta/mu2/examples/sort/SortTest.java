package cmu.pasta.mu2.examples.sort;

import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.Size;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;

@RunWith(JQF.class)
public class SortTest {
    
    protected static final int MAX_SIZE = 160;
    protected static final int MIN_ELEMENT = 0;
    protected static final int MAX_ELEMENT = 10;

    @Fuzz
    public void testBubbleSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new BubbleSort(), input);
    }

    @Fuzz
    public void fuzzBubbleSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        List<Integer> l1 = new BubbleSort().sort(input);
        input.sort(Integer::compareTo);
        assertEquals(l1, input);
        //this can kill 18 where testBubbleSort kills 24 - why?
    }

    @Fuzz
    public void testCocktailShakerSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new CocktailShakerSort(), input);
    }

    @Fuzz
    public void testCombSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new CombSort(), input);
    }

    @Fuzz
    public void testCycleSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new CycleSort(), input);
    }

    @Fuzz
    public void testGnomeSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new GnomeSort(), input);
    }

    @Fuzz
    public void testHeapSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new HeapSort(), input);
    }

    @Fuzz
    public void testInsertionSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new InsertionSort(), input);
    }

    @Fuzz
    public void testMergeSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new MergeSort(), input);
    }

    @Fuzz
    public void testPancakeSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new PancakeSort(), input);
    }

    @Fuzz
    public void testQuickSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new QuickSort(), input);
    }

    @Fuzz
    public void testSelectionSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new SelectionSort(), input);
    }

    @Fuzz
    public void testShellSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new ShellSort(), input);
    }

    @Fuzz
    public void testTimSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        testSort(new TimSort(), input);
    }

    public static <T extends Comparable<T>> void testSort(SortAlgorithm algorithm, List<T> input) {
        List<T> output = algorithm.sort(input);
        int n = input.size();
        // Check length
        assertEquals("Length should match",
                n, output.size());
        // Check integrity
        for(T x : input) {
            assertTrue("Output should contain same elements as input",
                    output.contains(x));
        }
        // Check sortedness
        for (int i = 0; i < n-1; i++) {
            assertThat(output.get(i), lessThanOrEqualTo(output.get(i+1)));
        }
    }

    public static <T extends Comparable<T>> void testSort(SortAlgorithm algorithm, T... input) {
        testSort(algorithm, Arrays.asList(input));
    }
}
