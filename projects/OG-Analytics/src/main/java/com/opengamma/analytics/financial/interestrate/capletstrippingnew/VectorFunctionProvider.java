/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.List;

/**
 * 
 */
interface VectorFunctionProvider<T> {

  VectorFunction from(final List<T> x);

  VectorFunction from(final T[] x);

}
