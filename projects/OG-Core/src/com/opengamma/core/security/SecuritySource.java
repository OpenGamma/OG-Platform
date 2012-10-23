/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import com.opengamma.core.SourceWithExternalBundle;
import com.opengamma.util.PublicSPI;

/**
 * A source of security information as accessed by the main application.
 * <p>
 * This interface provides a simple view of securities as needed by the engine.
 * This may be backed by a full-featured security master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface SecuritySource extends SourceWithExternalBundle<Security> {

}
