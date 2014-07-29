/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import org.springframework.context.Lifecycle;
import org.springframework.context.Phased;

/**
 * Unifies the Spring {@code Lifecycle} and {@code Phased} interfaces.
 * <p>
 * The component repository does not require use of this interface, because
 * it operates on {@code Lifecycle} and {@code Phased} separately.
 * This interface is thus provided for convenience.
 */
public interface PhasedLifecycle extends Lifecycle, Phased {

}
