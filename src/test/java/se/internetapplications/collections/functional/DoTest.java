package se.internetapplications.collections.functional;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import se.internetapplications.collections.functional.Do.BooleanExpression;
import se.internetapplications.collections.functional.Do.MapExpression;
import se.internetapplications.collections.functional.Do.ReduceExpression;


public class DoTest {

    private List<String> list;

    private ReduceExpression<Integer, Integer> toASum;

    private ReduceExpression<String, String> simpleStringJoin;

    private MapExpression<String, Integer> parsedInts;

    @Before
    public void setUp() {
        this.list = Arrays.asList("a", "b", "c", "d", "e");

        this.toASum = new ReduceExpression<Integer, Integer>() {
            public Integer reduce(final Integer accumulator,
                    final Integer element) {
                return element + accumulator;
            }
        };

        this.simpleStringJoin = new ReduceExpression<String, String>() {
            public String reduce(final String accumulatedValue,
                    final String element) {
                if (accumulatedValue != null
                        && accumulatedValue.trim().length() > 0) {
                    return accumulatedValue + ", " + element;
                } else {
                    return element;
                }
            }

        };

        this.parsedInts = new MapExpression<String, Integer>() {
            public Integer transform(String element) {
                return Integer.parseInt(element);
            }
        };

    }

    @Test
    public void identity() {
        Collection<String> result = Do.with(list);
        assertCollectionEquality(this.list, result);
    }

    @Test
    public void detect() {
        String result = Do.withCollection(list).detect(
                new BooleanExpression<String>() {
                    public boolean predicate(String element) {
                        return element.equalsIgnoreCase("B");
                    }
                });
        assertEquals("b", result);
    }

    @Test
    public void select() {
        Collection<String> actual = Do.withCollection(list).select(
                new BooleanExpression<String>() {
                    public boolean predicate(String element) {
                        return element.equalsIgnoreCase("B");
                    }
                });
        assertCollectionEquality(Arrays.asList("b"), actual);
    }

    @Test
    public void reject() {
        Collection<String> actual = Do.withCollection(list).reject(
                new BooleanExpression<String>() {
                    public boolean predicate(String element) {
                        return element.equalsIgnoreCase("B");
                    }
                });
        assertCollectionEquality(Arrays.asList("a", "c", "d", "e"), actual);
    }

    @Test
    public void rejectElement() {
        Collection<String> actual = Do.withCollection(list).rejectElement("b");
        assertCollectionEquality(Arrays.asList("a", "c", "d", "e"), actual);
    }

    @Test
    public void unique() {
        List<String> bag = new LinkedList<String>(list);
        bag.add("a");
        assertEquals(6, bag.size());
        List<String> actual = Do.with(bag).unique().toList();
        Collections.sort(actual);
        assertCollectionEquality(Arrays.asList("a", "b", "c", "d", "e"), actual);
    }
    
    @Test
    public void collect() {
        Collection<String> actual = Do.with(list).collect(
                new MapExpression<String, String>() {
                    public String transform(String element) {
                        return element.toUpperCase();
                    }
                });
        Collection<String> expected = Arrays.asList("A", "B", "C", "D", "E");
        assertCollectionEquality(expected, actual);
    }

    @Test
    public void map() {
        Collection<String> actual = Do.with(list).mapTo(
                new MapExpression<String, String>() {
                    public String transform(String element) {
                        return element.toUpperCase();
                    }
                });
        Collection<String> expected = Arrays.asList("A", "B", "C", "D", "E");
        assertCollectionEquality(expected, actual);
    }

    @Test
    public void mapToType() {
        Collection<String> input = Arrays.asList("1", "2", "3");
        Collection<Integer> actual = Do.with(input).mapTo(Integer.class).collect(
                parsedInts);

        Collection<Integer> expected = Arrays.asList(1, 2, 3);
        assertCollectionEquality(expected, actual);
    }

    @Test
    public void sum() {
        Collection<Integer> input = Arrays.asList(1, 2, 3);
        Integer result = Do.with(input).withInitialValue(0).reduce(toASum);
        assertEquals(6, result.intValue());
    }

    @Test
    public void sumArray() {
        Integer result = Do.with(1, 2, 3).withInitialValue(0).reduce(toASum);
        assertEquals(6, result.intValue());
    }

    @Test
    public void concat() {
        List<String> list = new ArrayList<String>() {
            {
                add("a");
                add("e");
                add("i");
                add("o");
                add("u");
            }
        };

        String actual = Do.with(list).withInitialValue("").reduce(
                new ReduceExpression<String, String>() {
                    public String reduce(String accumulatedValue, String element) {
                        return accumulatedValue + element;

                    }
                });

        assertEquals("aeiou", actual);
    }

    @Test
    public void concatStringBuffer() {
        String actual = Do.with(list).mapTo(StringBuilder.class).withInitialValue(
                new StringBuilder()).reduce(new ReduceToStringBuilder(", "))
                .toString();

        assertEquals("a, b, c, d, e", actual);
    }

    @Test
    public void commaSeparatedList() {
        String actual = Do.with(this.list).withInitialValue("").reduce(
                simpleStringJoin);
        assertEquals("a, b, c, d, e", actual);

    }

    @Test
    public void chain() {
        Collection<String> strings = Arrays.asList("1", "2", "3");
        Integer result = Do.with(strings).mapTo(Integer.class).collect(
                parsedInts).and().then().withInitialValue(0).reduce(toASum);
        assertEquals(6, result.intValue());
    }

    @Test
    public void reduceNoStartingValue() {
        try {
            Do.withCollection(this.list).reduce(
                    new ReduceExpression<String, String>() {
                        public String reduce(final String accumulator,
                                final String element) {
                            return null;
                        }
                    });
            fail("Should've thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }

    }

    private <T> void assertCollectionEquality(final Collection<T> expected,
            final Collection<T> actual) {
        Iterator<T> test = actual.iterator();
        for (T element : expected) {
            assertEquals(element, test.next());
        }
    }

}
