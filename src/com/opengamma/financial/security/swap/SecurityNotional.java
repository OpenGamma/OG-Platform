/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;

/**
 * A notional that holds a unique id to identify a security to use as a notional.
 *  e.g. an Index or an Equity.
 */
public class SecurityNotional extends Notional {

  private Identifier _notionalIdentifier;

  public SecurityNotional(Identifier notionalIdentifier) {
    _notionalIdentifier = notionalIdentifier;
  }
  
  public Identifier getNotionalIdentifier() {
    return _notionalIdentifier;
  }
}
