package com.annimon.stream;

import com.annimon.stream.function.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * A sequence of primitive int-valued elements supporting sequential operations. This is the {@code int}
 * primitive specialization of {@link Stream}.
 */
@SuppressWarnings("WeakerAccess")
public final class IntStream {

    /**
     * Single instance for empty stream. It is safe for multi-thread environment because it has no content.
     */
    private static final IntStream EMPTY = new IntStream(new PrimitiveIterator.OfInt() {
        @Override
        public int nextInt() {
            return 0;
        }

        @Override
        public boolean hasNext() {
            return false;
        }
    });

    /**
     * Returns an empty stream.
     *
     * @return the empty stream
     */
    public static IntStream empty() {
        return EMPTY;
    }

    /**
     * Creates a {@code IntStream} from {@code PrimitiveIterator.OfInt}.
     *
     * @param iterator  the iterator with elements to be passed to stream
     * @return the new {@code IntStream}
     * @throws NullPointerException if {@code iterator} is null
     */
    public static IntStream of(PrimitiveIterator.OfInt iterator) {
        Objects.requireNonNull(iterator);
        return new IntStream(iterator);
    }

    /**
     * Returns stream whose elements are the specified values.
     *
     * @param values the elements of the new stream
     * @return the new stream
     * @throws NullPointerException if {@code values} is null
     */
    public static IntStream of(final int... values) {
        Objects.requireNonNull(values);
        return new IntStream(new PrimitiveIterator.OfInt() {

            private int index = 0;

            @Override
            public int nextInt() {
                return values[index++];
            }

            @Override
            public boolean hasNext() {
                return index < values.length;
            }
        });
    }

    /**
     * Returns stream which contains single element passed as param
     *
     * @param t element of the stream
     * @return the new stream
     */
    public static IntStream of(final int t) {
        return new IntStream(new PrimitiveIterator.OfInt() {

            private int index = 0;

            @Override
            public int nextInt() {
                index++;
                return t;
            }

            @Override
            public boolean hasNext() {
                return index == 0;
            }
        });
    }

    /**
     * Returns a sequential ordered {@code IntStream} from {@code startInclusive}
     * (inclusive) to {@code endExclusive} (exclusive) by an incremental step of
     * {@code 1}.
     *
     * @param startInclusive the (inclusive) initial value
     * @param endExclusive the exclusive upper bound
     * @return a sequential {@code IntStream} for the range of {@code int}
     *         elements
     */
    public static IntStream range(final int startInclusive, final int endExclusive) {

        if(startInclusive >= endExclusive)
            return empty();

        return new IntStream(new PrimitiveIterator.OfInt() {

            private int current = startInclusive;

            @Override
            public int nextInt() {
                return current++;
            }

            @Override
            public boolean hasNext() {
                return current < endExclusive;
            }
        });
    }

    /**
     * Returns a sequential ordered {@code IntStream} from {@code startInclusive}
     * (inclusive) to {@code endInclusive} (inclusive) by an incremental step of
     * {@code 1}.
     *
     * @param startInclusive the (inclusive) initial value
     * @param endInclusive the inclusive upper bound
     * @return a sequential {@code IntStream} for the range of {@code int}
     *         elements
     */
    public static IntStream rangeClosed(int startInclusive, int endInclusive) {
        return range(startInclusive, endInclusive+1);
    }

    /**
     * Returns an infinite sequential unordered stream where each element is
     * generated by the provided {@code IntSupplier}.  This is suitable for
     * generating constant streams, streams of random elements, etc.
     *
     * @param s the {@code IntSupplier} for generated elements
     * @return a new infinite sequential {@code IntStream}
     * @throws NullPointerException if {@code s} is null
     */
    public static IntStream generate(final IntSupplier s) {
        Objects.requireNonNull(s);
        return new IntStream(new PrimitiveIterator.OfInt() {
            @Override
            public int nextInt() {
                return s.getAsInt();
            }

            @Override
            public boolean hasNext() {
                return true;
            }
        });
    }

    /**
     * Returns an infinite sequential ordered {@code IntStream} produced by iterative
     * application of a function {@code f} to an initial element {@code seed},
     * producing a {@code Stream} consisting of {@code seed}, {@code f(seed)},
     * {@code f(f(seed))}, etc.
     *
     * <p> The first element (position {@code 0}) in the {@code IntStream} will be
     * the provided {@code seed}.  For {@code n > 0}, the element at position
     * {@code n}, will be the result of applying the function {@code f} to the
     * element at position {@code n - 1}.
     *
     * <p>Example:
     * <pre>
     * seed: 1
     * f: (a) -&gt; a + 5
     * result: [1, 6, 11, 16, ...]
     * </pre>
     *
     * @param seed the initial element
     * @param f a function to be applied to to the previous element to produce
     *          a new element
     * @return a new sequential {@code IntStream}
     * @throws NullPointerException if {@code f} is null
     */
    public static IntStream iterate(final int seed, final IntUnaryOperator f) {
        Objects.requireNonNull(f);
        return new IntStream(new PrimitiveIterator.OfInt() {

            private int current = seed;

            @Override
            public int nextInt() {

                int old = current;
                current = f.applyAsInt(current);

                return old;
            }

            @Override
            public boolean hasNext() {
                return true;
            }
        });
    }

    /**
     * Creates a lazily concatenated stream whose elements are all the
     * elements of the first stream followed by all the elements of the
     * second stream.
     *
     * <p>Example:
     * <pre>
     * stream a: [1, 2, 3, 4]
     * stream b: [5, 6]
     * result:   [1, 2, 3, 4, 5, 6]
     * </pre>
     *
     * @param a the first stream
     * @param b the second stream
     * @return the concatenation of the two input streams
     * @throws NullPointerException if {@code a} or {@code b} is null
     */
    public static IntStream concat(final IntStream a, final IntStream b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        return new IntStream(new PrimitiveIterator.OfInt() {

            private boolean firstStreamIsCurrent = true;

            @Override
            public int nextInt() {
                return firstStreamIsCurrent ? a.iterator.nextInt() : b.iterator.nextInt();
            }

            @Override
            public boolean hasNext() {
                if(firstStreamIsCurrent) {
                    if(a.iterator.hasNext())
                        return true;

                    firstStreamIsCurrent = false;
                }

                return b.iterator.hasNext();
            }
        });
    }

    private final PrimitiveIterator.OfInt iterator;

    private IntStream(PrimitiveIterator.OfInt iterator) {
        this.iterator = iterator;
    }

    /**
     * Returns internal {@code IntStream} iterator.
     *
     * @return internal {@code IntStream} iterator.
     */
    public PrimitiveIterator.OfInt iterator() {
        return iterator;
    }

    /**
     * Applies custom operator on stream.
     *
     * Transforming function can return {@code IntStream} for intermediate operations,
     * or any value for terminal operation.
     *
     * <p>Operator examples:
     * <pre><code>
     *     // Intermediate operator
     *     public class Zip&lt;T&gt; implements Function&lt;IntStream, IntStream&gt; {
     *         &#64;Override
     *         public IntStream apply(IntStream firstStream) {
     *             final PrimitiveIterator.OfInt it1 = firstStream.iterator();
     *             final PrimitiveIterator.OfInt it2 = secondStream.iterator();
     *             return IntStream.of(new PrimitiveIterator.OfInt() {
     *                 &#64;Override
     *                 public boolean hasNext() {
     *                     return it1.hasNext() &amp;&amp; it2.hasNext();
     *                 }
     *
     *                 &#64;Override
     *                 public int nextInt() {
     *                     return combiner.applyAsInt(it1.nextInt(), it2.nextInt());
     *                 }
     *             });
     *         }
     *     }
     *
     *     // Intermediate operator based on existing stream operators
     *     public class SkipAndLimit implements UnaryOperator&lt;IntStream&gt; {
     *
     *         private final int skip, limit;
     *
     *         public SkipAndLimit(int skip, int limit) {
     *             this.skip = skip;
     *             this.limit = limit;
     *         }
     *
     *         &#64;Override
     *         public IntStream apply(IntStream stream) {
     *             return stream.skip(skip).limit(limit);
     *         }
     *     }
     *
     *     // Terminal operator
     *     public class Average implements Function&lt;IntStream, Double&gt; {
     *         long count = 0, sum = 0;
     *
     *         &#64;Override
     *         public Double apply(IntStream stream) {
     *             final PrimitiveIterator.OfInt it = stream.iterator();
     *             while (it.hasNext()) {
     *                 count++;
     *                 sum += it.nextInt();
     *             }
     *             return (count == 0) ? 0 : sum / (double) count;
     *         }
     *     }
     * </code></pre>
     *
     * @param <R> the type of the result
     * @param function  a transforming function
     * @return a result of the transforming function
     * @see Stream#custom(com.annimon.stream.function.Function)
     * @throws NullPointerException if {@code function} is null
     */
    public <R> R custom(final Function<IntStream, R> function) {
        Objects.requireNonNull(function);
        return function.apply(this);
    }

    /**
     * Returns a {@code Stream} consisting of the elements of this stream,
     * each boxed to an {@code Integer}.
     *
     * <p>This is an lazy intermediate operation.
     *
     * @return a {@code Stream} consistent of the elements of this stream,
     *         each boxed to an {@code Integer}
     */
    public Stream<Integer> boxed() {
        return Stream.of(iterator);
    }

    /**
     * Returns a stream consisting of the elements of this stream that match
     * the given predicate.
     *
     * <p> This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a &gt; 2
     * stream: [1, 2, 3, 4, -8, 0, 11]
     * result: [3, 4, 11]
     * </pre>
     *
     * @param predicate non-interfering, stateless predicate to apply to each
     *                  element to determine if it should be included
     * @return the new stream
     */
    public IntStream filter(final IntPredicate predicate) {
        return new IntStream(new PrimitiveIterator.OfInt() {

            private int next;

            @Override
            public int nextInt() {
                return next;
            }

            @Override
            public boolean hasNext() {
                while(iterator.hasNext()) {
                    next = iterator.next();
                    if(predicate.test(next)) {
                        return true;
                    }
                }

                return false;
            }
        });
    }

    /**
     * Returns a stream consisting of the elements of this stream that don't
     * match the given predicate.
     *
     * <p> This is an intermediate operation.
     *
     * @param predicate non-interfering, stateless predicate to apply to each
     *                  element to determine if it should not be included
     * @return the new stream
     */
    public IntStream filterNot(final IntPredicate predicate) {
        return filter(IntPredicate.Util.negate(predicate));
    }

    /**
     * Returns an {@code IntStream} consisting of the results of applying the given
     * function to the elements of this stream.
     *
     * <p> This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * mapper: (a) -&gt; a + 5
     * stream: [1, 2, 3, 4]
     * result: [6, 7, 8, 9]
     * </pre>
     *
     * @param mapper a non-interfering stateless function to apply to
     *               each element
     * @return the new {@code IntStream}
     */
    public IntStream map(final IntUnaryOperator mapper) {
        return new IntStream(new PrimitiveIterator.OfInt() {
            @Override
            public int nextInt() {
                return mapper.applyAsInt(iterator.nextInt());
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
        });
    }

    /**
     * Returns a {@code Stream} consisting of the results of applying the given
     * function to the elements of this stream.
     *
     * <p> This is an intermediate operation.
     *
     * @param <R> the type result
     * @param mapper the mapper function used to apply to each element
     * @return the new {@code Stream}
     */
    public <R> Stream<R> mapToObj(final IntFunction<? extends R> mapper) {
        return Stream.of(new LsaIterator<R>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public R nextIteration() {
                return mapper.apply(iterator.nextInt());
            }
        });
    }

    /**
     * Returns a {@code DoubleStream} consisting of the results of applying the given
     * function to the elements of this stream.
     *
     * <p> This is an intermediate operation.
     *
     * @param mapper  the mapper function used to apply to each element
     * @return the new {@code DoubleStream}
     * @since 1.1.4
     * @see #flatMap(com.annimon.stream.function.IntFunction)
     */
    public DoubleStream mapToDouble(final IntToDoubleFunction mapper) {
        return DoubleStream.of(new PrimitiveIterator.OfDouble() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public double nextDouble() {
                return mapper.applyAsDouble(iterator.nextInt());
            }
        });
    }

    /**
     * Returns a stream consisting of the results of replacing each element of
     * this stream with the contents of a mapped stream produced by applying
     * the provided mapping function to each element.
     *
     * <p>This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * mapper: (a) -&gt; [a, a + 5]
     * stream: [1, 2, 3, 4]
     * result: [1, 6, 2, 7, 3, 8, 4, 9]
     * </pre>
     *
     * @param mapper a non-interfering stateless function to apply to each
     *               element which produces an {@code IntStream} of new values
     * @return the new stream
     * @see Stream#flatMap(Function)
     */
    public IntStream flatMap(final IntFunction<? extends IntStream> mapper) {
        return new IntStream(new PrimitiveIterator.OfInt() {

            private PrimitiveIterator.OfInt inner;

            @Override
            public int nextInt() {
                return inner.nextInt();
            }

            @Override
            public boolean hasNext() {

                if(inner != null && inner.hasNext()) {
                    return true;
                }

                while(iterator.hasNext()) {
                    int arg = iterator.next();

                    IntStream result = mapper.apply(arg);
                    if(result == null) {
                        continue;
                    }

                    if(result.iterator.hasNext()) {
                        inner = result.iterator;
                        return true;
                    }
                }

                return false;
            }
        });
    }

    /**
     * Returns a stream consisting of the distinct elements of this stream.
     *
     * <p>This is a stateful intermediate operation.
     *
     * <p>Example:
     * <pre>
     * stream: [1, 4, 2, 3, 3, 4, 1]
     * result: [1, 4, 2, 3]
     * </pre>
     *
     * @return the new stream
     */
    public IntStream distinct() {
        // While functional and quick to implement, this approach is not very efficient.
        // An efficient version requires an int-specific map/set implementation.
        return boxed().distinct().mapToInt(UNBOX_FUNCTION);
    }

    /**
     * Returns a stream consisting of the elements of this stream in sorted
     * order.
     *
     * <p>This is a stateful intermediate operation.
     *
     * <p>Example:
     * <pre>
     * stream: [3, 4, 1, 2]
     * result: [1, 2, 3, 4]
     * </pre>
     *
     * @return the new stream
     */
    public IntStream sorted() {
        return new IntStream(new PrimitiveExtIterator.OfInt() {

            private int index = 0;
            private int[] array;

            @Override
            protected void nextIteration() {
                if (!isInit) {
                    array = toArray();
                    Arrays.sort(array);
                }
                hasNext = index < array.length;
                if (hasNext) {
                    next = array[index++];
                }
            }
        });
    }

    /**
     * Returns {@code IntStream} with sorted elements (as determinated by provided {@code Comparator}).
     *
     * <p>This is a stateful intermediate operation.
     *
     * <p>Example:
     * <pre>
     * comparator: (a, b) -&gt; -a.compareTo(b)
     * stream: [1, 2, 3, 4]
     * result: [4, 3, 2, 1]
     * </pre>
     *
     * @param comparator  the {@code Comparator} to compare elements
     * @return the new {@code IntStream}
     */
    public IntStream sorted(Comparator<Integer> comparator) {
        return boxed().sorted(comparator).mapToInt(UNBOX_FUNCTION);
    }

    /**
     * Samples the {@code IntStream} by emitting every n-th element.
     *
     * <p>This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * stepWidth: 3
     * stream: [1, 2, 3, 4, 5, 6, 7, 8]
     * result: [1, 4, 7]
     * </pre>
     *
     * @param stepWidth  step width
     * @return the new {@code IntStream}
     * @throws IllegalArgumentException if {@code stepWidth} is zero or negative
     * @see Stream#sample(int)
     */
    public IntStream sample(final int stepWidth) {
        if (stepWidth <= 0) throw new IllegalArgumentException("stepWidth cannot be zero or negative");
        if (stepWidth == 1) return this;
        return new IntStream(new PrimitiveIterator.OfInt() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public int nextInt() {
                final int result = iterator.nextInt();
                int skip = 1;
                while (skip < stepWidth && iterator.hasNext()) {
                    iterator.nextInt();
                    skip++;
                }
                return result;
            }
        });
    }

    /**
     * Returns a stream consisting of the elements of this stream, additionally
     * performing the provided action on each element as elements are consumed
     * from the resulting stream. Handy method for debugging purposes.
     *
     * <p>This is an intermediate operation.
     *
     * @param action the action to be performed on each element
     * @return the new stream
     */
    public IntStream peek(final IntConsumer action) {
        return new IntStream(new PrimitiveIterator.OfInt() {
            @Override
            public int nextInt() {
                int value = iterator.nextInt();
                action.accept(value);
                return value;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
        });
    }

    /**
     * Takes elements while the predicate is true.
     *
     * <p>This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a &lt; 3
     * stream: [1, 2, 3, 4, 1, 2, 3, 4]
     * result: [1, 2]
     * </pre>
     *
     * @param predicate  the predicate used to take elements
     * @return the new {@code IntStream}
     */
    public IntStream takeWhile(final IntPredicate predicate) {
        return new IntStream(new PrimitiveExtIterator.OfInt() {

            @Override
            protected void nextIteration() {
                hasNext = iterator.hasNext() && predicate.test(next = iterator.next());
            }
        });
    }

    /**
     * Drops elements while the predicate is true and returns the rest.
     *
     * <p>This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a &lt; 3
     * stream: [1, 2, 3, 4, 1, 2, 3, 4]
     * result: [3, 4, 1, 2, 3, 4]
     * </pre>
     *
     * @param predicate  the predicate used to drop elements
     * @return the new {@code IntStream}
     */
    public IntStream dropWhile(final IntPredicate predicate) {
        return new IntStream(new PrimitiveExtIterator.OfInt() {

            @Override
            protected void nextIteration() {
                if (!isInit) {
                    // Skip first time
                    while (hasNext = iterator.hasNext()) {
                        next = iterator.next();
                        if (!predicate.test(next)) {
                            return;
                        }
                    }
                }

                hasNext = hasNext && iterator.hasNext();
                if (!hasNext) return;

                next = iterator.next();
            }
        });
    }

    /**
     * Returns a stream consisting of the elements of this stream, truncated
     * to be no longer than {@code maxSize} in length.
     *
     * <p> This is a short-circuiting stateful intermediate operation.
     *
     * <p>Example:
     * <pre>
     * maxSize: 3
     * stream: [1, 2, 3, 4, 5]
     * result: [1, 2, 3]
     *
     * maxSize: 10
     * stream: [1, 2]
     * result: [1, 2]
     * </pre>
     *
     * @param maxSize the number of elements the stream should be limited to
     * @return the new stream
     * @throws IllegalArgumentException if {@code maxSize} is negative
     */
    public IntStream limit(final long maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize cannot be negative");
        }
        if (maxSize == 0) {
            return IntStream.empty();
        }
        return new IntStream(new PrimitiveIterator.OfInt() {

            private long index = 0;

            @Override
            public int nextInt() {
                index++;
                return iterator.nextInt();
            }

            @Override
            public boolean hasNext() {
                return (index < maxSize) && iterator.hasNext();
            }
        });
    }

    /**
     * Returns a stream consisting of the remaining elements of this stream
     * after discarding the first {@code n} elements of the stream.
     * If this stream contains fewer than {@code n} elements then an
     * empty stream will be returned.
     *
     * <p>This is a stateful intermediate operation.
     *
     * <p>Example:
     * <pre>
     * n: 3
     * stream: [1, 2, 3, 4, 5]
     * result: [4, 5]
     *
     * n: 10
     * stream: [1, 2]
     * result: []
     * </pre>
     *
     * @param n the number of leading elements to skip
     * @return the new stream
     * @throws IllegalArgumentException if {@code n} is negative
     */
    public IntStream skip(final long n) {
        if(n < 0)
            throw new IllegalArgumentException("n cannot be negative");

        if(n == 0)
            return this;
        else
            return new IntStream(new PrimitiveIterator.OfInt() {
                long skipped = 0;
                @Override
                public int nextInt() {
                    return iterator.nextInt();
                }

                @Override
                public boolean hasNext() {

                    while(iterator.hasNext()) {

                        if(skipped == n) break;

                        skipped++;
                        iterator.nextInt();
                    }

                    return iterator.hasNext();
                }
            });
    }

    /**
     * Performs an action for each element of this stream.
     *
     * <p>This is a terminal operation.
     *
     * @param action a non-interfering action to perform on the elements
     */
    public void forEach(IntConsumer action) {
        while(iterator.hasNext()) {
            action.accept(iterator.nextInt());
        }
    }

    /**
     * Performs a reduction on the elements of this stream, using the provided
     * identity value and an associative accumulation function, and returns the
     * reduced value.
     *
     * <p>The {@code identity} value must be an identity for the accumulator
     * function. This means that for all {@code x},
     * {@code accumulator.apply(identity, x)} is equal to {@code x}.
     * The {@code accumulator} function must be an associative function.
     *
     * <p>This is a terminal operation.
     *
     * <p>Example:
     * <pre>
     * identity: 0
     * accumulator: (a, b) -&gt; a + b
     * stream: [1, 2, 3, 4, 5]
     * result: 15
     * </pre>
     *
     * @param identity the identity value for the accumulating function
     * @param op an associative non-interfering stateless function for
     *           combining two values
     * @return the result of the reduction
     * @see #sum()
     * @see #min()
     * @see #max()
     */
    public int reduce(int identity, IntBinaryOperator op) {
        int result = identity;
        while(iterator.hasNext()) {
            int value = iterator.nextInt();
            result = op.applyAsInt(result, value);
        }
        return result;
    }

    /**
     * Performs a reduction on the elements of this stream, using an
     * associative accumulation function, and returns an {@code OptionalInt}
     * describing the reduced value, if any.
     *
     * <p>The {@code op} function must be an associative function.
     *
     * <p>This is a terminal operation.
     *
     * @param op an associative, non-interfering, stateless function for
     *           combining two values
     * @return the result of the reduction
     * @see #reduce(int, IntBinaryOperator)
     */
    public OptionalInt reduce(IntBinaryOperator op) {
        boolean foundAny = false;
        int result = 0;
        while(iterator.hasNext()) {
            int value = iterator.nextInt();

            if(!foundAny) {
                foundAny = true;
                result = value;
            } else {
                result = op.applyAsInt(result, value);
            }
        }
        return foundAny ? OptionalInt.of(result) : OptionalInt.empty();
    }

    /**
     * Returns an array containing the elements of this stream.
     *
     * <p>This is a terminal operation.
     *
     * @return an array containing the elements of this stream
     */
    public int[] toArray() {
        SpinedBuffer.OfInt b = new SpinedBuffer.OfInt();

        forEach(b);

        return b.asPrimitiveArray();
    }

    /**
     * Collects elements to {@code supplier} provided container by applying the given accumulation function.
     *
     * <p>This is a terminal operation.
     *
     * @param <R> the type of the result
     * @param supplier  the supplier function that provides container
     * @param accumulator  the accumulation function
     * @return the result of collect elements
     * @see Stream#collect(com.annimon.stream.function.Supplier, com.annimon.stream.function.BiConsumer)
     */
    public <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator) {
        R result = supplier.get();
        while (iterator.hasNext()) {
            final int value = iterator.nextInt();
            accumulator.accept(result, value);
        }
        return result;
    }

    /**
     * Returns the sum of elements in this stream.
     *
     * @return the sum of elements in this stream
     */
    public int sum() {
        int sum = 0;
        while(iterator.hasNext()) {
            sum += iterator.nextInt();
        }

        return sum;
    }

    /**
     * Returns an {@code OptionalInt} describing the minimum element of this
     * stream, or an empty optional if this stream is empty.
     *
     * <p>This is a terminal operation.
     *
     * @return an {@code OptionalInt} containing the minimum element of this
     *         stream, or an empty {@code OptionalInt} if the stream is empty
     */
    public OptionalInt min() {
        return reduce(new IntBinaryOperator() {
            @Override
            public int applyAsInt(int left, int right) {
                return left < right ? left : right;
            }
        });
    }

    /**
     * Returns an {@code OptionalInt} describing the maximum element of this
     * stream, or an empty optional if this stream is empty.
     *
     * <p>This is a terminal operation.
     *
     * @return an {@code OptionalInt} containing the maximum element of this
     *         stream, or an empty {@code OptionalInt} if the stream is empty
     */
    public OptionalInt max() {
        return reduce(new IntBinaryOperator() {
            @Override
            public int applyAsInt(int left, int right) {
                return left > right ? left : right;
            }
        });
    }

    /**
     * Returns the count of elements in this stream.
     *
     * <p>This is a terminal operation.
     *
     * @return the count of elements in this stream
     */
    public long count() {
        long count = 0;
        while(iterator.hasNext()) {
            iterator.nextInt();
            count++;
        }
        return count;
    }

    /**
     * Returns whether any elements of this stream match the provided
     * predicate. May not evaluate the predicate on all elements if not
     * necessary for determining the result.  If the stream is empty then
     * {@code false} is returned and the predicate is not evaluated.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a == 5
     * stream: [1, 2, 3, 4, 5]
     * result: true
     *
     * predicate: (a) -&gt; a == 5
     * stream: [5, 5, 5]
     * result: true
     * </pre>
     *
     * @param predicate a non-interfering stateless predicate to apply
     *                  to elements of this stream
     * @return {@code true} if any elements of the stream match the provided
     *         predicate, otherwise {@code false}
     */
    public boolean anyMatch(IntPredicate predicate) {
        while(iterator.hasNext()) {
            if(predicate.test(iterator.nextInt()))
                return true;
        }

        return false;
    }

    /**
     * Returns whether all elements of this stream match the provided predicate.
     * May not evaluate the predicate on all elements if not necessary for
     * determining the result.  If the stream is empty then {@code true} is
     * returned and the predicate is not evaluated.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a == 5
     * stream: [1, 2, 3, 4, 5]
     * result: false
     *
     * predicate: (a) -&gt; a == 5
     * stream: [5, 5, 5]
     * result: true
     * </pre>
     *
     * @param predicate a non-interfering stateless predicate to apply to
     *                  elements of this stream
     * @return {@code true} if either all elements of the stream match the
     *         provided predicate or the stream is empty, otherwise {@code false}
     */
    public boolean allMatch(IntPredicate predicate) {
        while(iterator.hasNext()) {
            if(!predicate.test(iterator.nextInt()))
                return false;
        }

        return true;
    }

    /**
     * Returns whether no elements of this stream match the provided predicate.
     * May not evaluate the predicate on all elements if not necessary for
     * determining the result.  If the stream is empty then {@code true} is
     * returned and the predicate is not evaluated.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a == 5
     * stream: [1, 2, 3, 4, 5]
     * result: false
     *
     * predicate: (a) -&gt; a == 5
     * stream: [1, 2, 3]
     * result: true
     * </pre>
     *
     * @param predicate a non-interfering stateless predicate to apply to
     *                  elements of this stream
     * @return {@code true} if either no elements of the stream match the
     *         provided predicate or the stream is empty, otherwise {@code false}
     */
    public boolean noneMatch(IntPredicate predicate) {

        if(!iterator.hasNext())
            return true;

        while(iterator.hasNext()) {
            if(predicate.test(iterator.nextInt()))
                return false;
        }

        return true;
    }

    /**
     * Returns an {@link OptionalInt} describing the first element of this
     * stream, or an empty {@code OptionalInt} if the stream is empty.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * @return an {@code OptionalInt} describing the first element of this stream,
     *         or an empty {@code OptionalInt} if the stream is empty
     */
    public OptionalInt findFirst() {
        if(iterator.hasNext()) {
            return OptionalInt.of(iterator.nextInt());
        } else {
            return OptionalInt.empty();
        }
    }

    /**
     * Returns the single element of stream.
     * If stream is empty, throws {@code NoSuchElementException}.
     * If stream contains more than one element, throws {@code IllegalStateException}.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * <p>Example:
     * <pre>
     * stream: []
     * result: NoSuchElementException
     *
     * stream: [1]
     * result: 1
     *
     * stream: [1, 2, 3]
     * result: IllegalStateException
     * </pre>
     *
     * @return single element of stream
     * @throws NoSuchElementException if stream is empty
     * @throws IllegalStateException if stream contains more than one element
     * @since 1.1.3
     */
    public int single() {
        if (iterator.hasNext()) {
            int singleCandidate = iterator.next();
            if (iterator.hasNext()) {
                throw new IllegalStateException("IntStream contains more than one element");
            } else {
                return singleCandidate;
            }
        } else {
            throw new NoSuchElementException("IntStream contains no element");
        }
    }

    /**
     * Returns the single element wrapped by {@code OptionalInt} class.
     * If stream is empty, returns {@code OptionalInt.empty()}.
     * If stream contains more than one element, throws {@code IllegalStateException}.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * <p>Example:
     * <pre>
     * stream: []
     * result: OptionalInt.empty()
     *
     * stream: [1]
     * result: OptionalInt.of(1)
     *
     * stream: [1, 2, 3]
     * result: IllegalStateException
     * </pre>
     *
     * @return an {@code OptionalInt} with single element or {@code OptionalInt.empty()} if stream is empty
     * @throws IllegalStateException if stream contains more than one element
     * @since 1.1.3
     */
    public OptionalInt findSingle() {
        if (iterator.hasNext()) {
            int singleCandidate = iterator.next();
            if (iterator.hasNext()) {
                throw new IllegalStateException("IntStream contains more than one element");
            } else {
                return OptionalInt.of(singleCandidate);
            }
        } else {
            return OptionalInt.empty();
        }
    }


    private static final ToIntFunction<Integer> UNBOX_FUNCTION = new ToIntFunction<Integer>() {
        @Override
        public int applyAsInt(Integer t) {
            return t;
        }
    };
}
