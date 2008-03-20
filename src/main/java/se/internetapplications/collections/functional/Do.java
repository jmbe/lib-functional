package se.internetapplications.collections.functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Utility class for dealing with collections in a functional way.
 * <p>
 * Heavily modified version of
 * http://www.ugrad.cs.jhu.edu/~wsix/collections.pdf.
 * </p>
 * <p>
 * Map operation:
 * <ul>
 * <li><code>Do.with(collection).mapTo(Target.class).collect(expression)</li>
 * <li><code>Do.with(collection).mapTo(expression)</li>
 * </ul>
 * </p>
 * 
 * <p>Filter operations:
 * <ul>
 * <li><code>Do.with(collection).select(expression)</code></li>
 * <li><code>Do.with(collection).reject(expression)</code></li>
 * <li><code>Do.with(collection).detect(expression)</code></li>
 * </ul>
 * </p>
 * 
 * <p>Reduce operation:
 * <ul>
 * <li><code>Do.with(collection).withInitialValue(start).reduce(expression)</code></li>
 * </ul>
 * </p>
 * 
 * @author Jan-Mikael Bergqvist
 */
public class Do<E, R> implements Collection<E> {

    /**
     * Collection to operate on.
     */
    private final Collection<E> collection;

    /**
     * Intermediate value in reduce operations.
     */
    private R reduceResult;

    protected Do(final Collection<E> collection) {
        this.collection = collection;
    }

    /**
     * Sets collection to use in operations.
     */
    public static <F> Do<F, F> withArray(final F... f) {
        return withCollection(Arrays.asList(f));
    }

    /**
     * Sets collection to use in operations.
     */
    public static <F> Do<F, F> withCollection(final Collection<F> collection) {
        return new Do<F, F>(collection);
    }

    /**
     * Alias for withCollection.
     * 
     * @see Do#withCollection(Collection)
     */
    public static <F> Do<F, F> with(final Collection<F> collection) {
        return withCollection(collection);
    }

    /**
     * Alias for withArray.
     * 
     * @see Do#withArray(Object...)
     */
    public static <F> Do<F, F> with(final F... f) {
        return withArray(f);
    }

    /**
     * Sets target type for operations.
     * 
     * @param target
     *            target type
     */
    public <S> Do<E, S> mapTo(final Class<S> target) {
        return new Do<E, S>(collection);
    }

    /**
     * @return a new collection containing elements mapped to a new value by the
     *         expression.
     */
    public Do<R, R> collect(final MapExpression<E, R> expression) {
        Collection<R> result = new ArrayList<R>();

        for (E element : this.collection) {
            R r = expression.transform(element);
            if (r != null) {
                result.add(r);
            }
        }
        return new Do<R, R>(result);
    }

    /**
     * @return a new collection containing elements mapped to a new value by the
     *         expression.
     */
    public <S> Do<S, S> mapTo(final MapExpression<E, S> expression) {
        Collection<S> result = new ArrayList<S>();

        for (E element : this.collection) {
            S s = expression.transform(element);
            if (s != null) {
                result.add(s);
            }
        }
        return new Do<S, S>(result);
    }
    
    /**
     * Alias for mapTo.
     */
    public <S> Do<S, S> map(final MapExpression<E, S> expression) {
        return mapTo(expression);
    }

    /**
     * @return a new collection containing all elements matching the expression.
     */
    public Do<E, E> select(final BooleanExpression<E> expression) {
        Collection<E> result = new ArrayList<E>();
        for (E element : this.collection) {
            if (expression.predicate(element)) {
                result.add(element);
            }
        }
        return new Do<E, E>(result);
    }

    /**
     * @return first value matching expression or <code>null</code> if no
     *         matching expression could be found.
     */
    public E detect(final BooleanExpression<E> expression) {
        for (E element : this.collection) {
            if (expression.predicate(element)) {
                return element;
            }
        }
        return null;
    }

    /**
     * @return new collection containing all elements except those matching the
     *         expression.
     */
    public Do<E, E> reject(final BooleanExpression<E> expression) {
        Collection<E> result = new ArrayList<E>();

        for (E element : this.collection) {
            if (!expression.predicate(element)) {
                result.add(element);
            }
        }
        return new Do<E, E>(result);
    }

    /**
     * Removes the first element in the collection equal to the given element.
     * If you want to remove all elements then call <code>unique()</code>
     * first.
     * 
     * @see Do#unique()
     */
    public Do<E, E> rejectElement(final E... elements) {
        Collection<E> result = new ArrayList<E>(this.collection);
        for (int i = 0; i < elements.length; i++) {
            result.remove(elements[i]);
        }
        return new Do<E, E>(result);
    }

    /**
     * Adds an element to the collection.
     */
    public Do<E, E> injectElement(final E... elements) {
        Collection<E> result = new ArrayList<E>(this.collection);
        for (int i = 0; i < elements.length; i++) {
            result.add(elements[i]);
        }
        return new Do<E, E>(result);
    }

    /**
     * @param start
     *            initial value in a reduce operation.
     */
    public <S> Do<E, S> withInitialValue(final S start) {
        Do<E, S> result = new Do<E, S>(this.collection);
        result.reduceResult = start;
        return result;
    }

    /**
     * @throws IllegalStateException
     *             if no starting value is set
     */
    public R reduce(final ReduceExpression<E, R> expr)
            throws IllegalStateException {

        if (this.reduceResult == null) {
            throw new IllegalStateException("Must set starting value before "
                    + "reducing. Use 'withStartValue'");
        }

        for (E element : this.collection) {
            this.reduceResult = expr.reduce(this.reduceResult, element);
        }

        return this.reduceResult;
    }

    public Do<E, R> and() {
        return this;
    }

    public Do<E, R> then() {
        return this;
    }

    /**
     * Retains only unique values in the collection. Duplicates are removed.
     */
    public Do<E, E> unique() {
        return new Do<E, E>(this.toSet());
    }

    /**
     * Alias for <code>unique</code>.
     */
    public Do<E, E> removeDuplicates() {
        return new Do<E, E>(this.toSet());
    }

    public Set<E> toSet() {
        return new HashSet<E>(this.collection);
    }

    public List<E> toList() {
        return new LinkedList<E>(this.collection);
    }

    /*
     * Collection implementation.
     */
    public boolean add(E e) {
        return this.collection.add(e);
    }

    public boolean addAll(Collection<? extends E> c) {
        return this.collection.addAll(c);
    }

    public void clear() {
        this.collection.clear();
    }

    public boolean contains(Object o) {
        return this.collection.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return this.collection.containsAll(c);
    }

    public boolean isEmpty() {
        return this.collection.isEmpty();
    }

    public Iterator<E> iterator() {
        return this.collection.iterator();
    }

    public boolean remove(Object o) {
        return this.collection.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return this.collection.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return this.collection.retainAll(c);
    }

    public int size() {
        return this.collection.size();
    }

    public Object[] toArray() {
        return this.collection.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return this.collection.toArray(a);
    }

    /*
     * Expression interfaces.
     */
    public static interface MapExpression<I, O> {
        O transform(I element);
    }

    public static interface BooleanExpression<E> {
        boolean predicate(E element);
    }

    public static interface ReduceExpression<E, R> {
        R reduce(R accumulatedValue, E element);
    }
}
