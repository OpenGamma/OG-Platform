/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;

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

  public static SecurityNotional fromFudgeMsg(final FudgeDeserializationContext context,
      final FudgeFieldContainer message) {
    return new SecurityNotional(context.fieldValueToObject(Identifier.class, message.getByName("notionalIdentifier")));
  }

}
