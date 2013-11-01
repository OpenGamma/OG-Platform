/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.convention;

import com.opengamma.core.SourceWithExternalBundle;
import com.opengamma.util.PublicSPI;

/**
 * A source of convention information as accessed by the main application.
 * <p>
 * This interface provides a simple read-only view of conventions.
 * This may be backed by a full-featured convention master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface ConventionSource extends SourceWithExternalBundle<Convention> {

}
