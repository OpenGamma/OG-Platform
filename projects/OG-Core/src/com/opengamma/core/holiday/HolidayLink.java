/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday;

import com.opengamma.core.Link;
import com.opengamma.util.PublicAPI;

/**
 * A flexible link between an object and a holiday.
 * <p>
 * The holiday link represents a connection from an entity to a holiday.
 * The connection can be held by an {@code ObjectId} or an {@code ExternalIdBundle}.
 * To obtain the target holiday, the link must be resolved.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface HolidayLink extends Link<Holiday> {

}
