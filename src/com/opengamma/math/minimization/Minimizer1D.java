package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public interface Minimizer1D<T, U extends Exception> extends Minimizer<T, T, Function1D<T, T, U>, T, U> {

}
