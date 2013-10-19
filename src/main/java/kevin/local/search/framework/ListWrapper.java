package kevin.local.search.framework;

import java.util.AbstractList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 1/4/13
 * Time: 2:50 PM
 */

public class ListWrapper {

    public static List<Integer> wrap(final int[] array) {
        return new AbstractList<Integer>() {
                    @Override
                    public Integer get(int index) {
                        return array[index];
                    }

                    @Override
                    public int size() {
                        return array.length;
                    }
                };
    }
    public static <T>List<T> wrap(final T[] array) {
        return new AbstractList<T>() {
            @Override
            public T get(int index) {
                return array[index];
            }

            @Override
            public int size() {
                return array.length;
            }
        };
    }
}
