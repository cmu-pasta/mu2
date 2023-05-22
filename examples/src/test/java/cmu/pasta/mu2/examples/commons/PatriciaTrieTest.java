package cmu.pasta.mu2.examples.commons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.difffuzz.Comparison;
import edu.berkeley.cs.jqf.fuzz.difffuzz.DiffFuzz;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(JQF.class)
public class PatriciaTrieTest {

    @DiffFuzz(cmp = "compare")
    public List<Map<String, Integer>> testPrefixMap(HashMap<String, Integer> map, String prefix) {
        assumeTrue(prefix.length() > 0);
        PatriciaTrie trie = new PatriciaTrie(map);
        Map<String, Integer> tmp = new HashMap<>();
        tmp.put(prefix, 0);
        return Arrays.asList(trie.prefixMap(prefix), tmp);
    }

    @DiffFuzz(cmp = "noncompare")
    public List<Map<String, Integer>> otherPrefixMap(HashMap<String, Integer> map, String prefix) {
        assumeTrue(prefix.length() > 0);
        // Create new trie with input `map`
        PatriciaTrie trie = new PatriciaTrie(map);
        // Get sub-map whose keys start with `prefix`
        Map prefixMap = trie.prefixMap(prefix);
        // Ensure that it contains all keys that start with `prefix`
        for (String key : map.keySet()) {
            if (key.startsWith(prefix)) {
                assertTrue(prefixMap.containsKey(key));
            }
        }
        return null;
    }

    @Comparison
    public static Boolean noncompare(List<Map> l1, List<Map> l2) {
        return true;
    }

    @Comparison
    public static Boolean compare(List<Map<String, Integer>> l1, List<Map<String, Integer>> l2) {
        if(l1.get(1) != l2.get(1)) return false;
        for (String key : l1.get(0).keySet()) {
            if (key.startsWith((String) l1.get(1).keySet().toArray()[0])) {
                if(l1.get(0).containsKey(key) != l1.get(1).containsKey(key))
                    return false;
            }
        }
        return true;
    }
}
