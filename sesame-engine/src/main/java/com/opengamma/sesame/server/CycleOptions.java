/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.server;

import com.opengamma.sesame.engine.CalculationArguments;

/**
 * This is a marker interface for the cycle options to be run.
 *
 * @deprecated use {@link CalculationArguments}
 */
@Deprecated
public interface CycleOptions extends Iterable<IndividualCycleOptions> {

}
