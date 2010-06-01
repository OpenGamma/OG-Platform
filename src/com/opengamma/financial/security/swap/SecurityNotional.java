/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import com.opengamma.id.UniqueIdentifier;

/**
 * A notional that holds a unique id to identify a security to use as a notional.
 *  e.g. an Index or an Equity.
 */
public class SecurityNotional extends Notional {

  private UniqueIdentifier _notionalIdentifier;

  public SecurityNotional(UniqueIdentifier notionalIdentifier) {
    _notionalIdentifier = notionalIdentifier;
  }
  
  public UniqueIdentifier getNotionalIdentifier() {
    return _notionalIdentifier;
  }
}
