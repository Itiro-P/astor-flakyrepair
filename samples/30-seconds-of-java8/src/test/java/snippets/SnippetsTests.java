package snippets;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SnippetsTests {

    @Test
    public void group_elements_by_length() throws Exception {
        Map<Integer, List<String>> groups = Snippets.groupBy(new String[]{"one", "two", "three"}, String::length);
        assertThat(groups)
                .containsExactly(
                        new SimpleEntry<>(3, Arrays.asList("one", "two")),
                        new SimpleEntry<>(5, Collections.singletonList("three"))
                );
    }

    @Test
    public void pick_should_pick_key_pairs_corresponding_to_keys() throws Exception {
        Map<String, Integer> obj = new HashMap<>();
        obj.put("a", 1);
        obj.put("b", 2);
        obj.put("c", 3);

        Map<String, Integer> picked = Snippets.pick(obj, new String[]{"a", "c"});
        assertThat(picked).containsExactly(new SimpleEntry<>("a", 1), new SimpleEntry<>("c", 3));
    }
}