package com.opengamma.math.minimization;

import com.opengamma.math.function.Function;

public interface MultidimensionalMinimizer<T, U extends Exception> extends Minimizer<T, T, Function<T, T, U>, T[], U> {

}
