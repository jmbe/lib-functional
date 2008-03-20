package se.internetapplications.collections.functional;

import se.internetapplications.collections.functional.Do.ReduceExpression;

/**
 * Uses a <code>StringBuilder</code> to join a collection of
 * <code>String</code>s using the given separator..
 */
public class ReduceToStringBuilder implements
        ReduceExpression<String, StringBuilder> {
    private String separator;

    /**
     * @param separator
     *            string to use to join elements. May be <code>null</code>,
     *            in which case no separator is used.
     */
    public ReduceToStringBuilder(final String separator) {
        this.separator = separator;
    }

    public StringBuilder reduce(final StringBuilder builder,
            final String element) {
        if (this.separator != null && builder.length() > 0) {
            builder.append(this.separator);
        }
        builder.append(element);

        return builder;
    }

}
