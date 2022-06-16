package cmu.pasta.mu2.fuzz;

import java.util.List;

import cmu.pasta.mu2.MutationInstance;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;

import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class KRandomFilterTest {

    @Property
    public void filteredListSizeIsCorrect(int k, List<MutationInstance> toFilter){
        // instantiate random filter with parameter k
        KRandomFilter filter = new KRandomFilter(k);
        // call filter method to obtain filtered list
        List<MutationInstance> filteredList = filter.filterMutants(toFilter);
        
        // if k is smaller than the size of the list, then the filtered list should be of size k
        // if not the filtered list should contain all of the original elements in toFilter
        if (toFilter.size() >= k) {
            assertEquals(filteredList.size(), k);
        } else {
            assertEquals(filteredList.size(), toFilter.size());
        }
    }

}
