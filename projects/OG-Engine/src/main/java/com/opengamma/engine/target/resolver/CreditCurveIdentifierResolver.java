/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.id.UniqueId;
import com.opengamma.util.credit.CreditCurveIdentifier;

/**
 * 
 */
public class CreditCurveIdentifierResolver extends AbstractPrimitiveResolver<CreditCurveIdentifier> {

  // REVIEW: 2013-08-05 Andrew -- Why is this in OG-Engine? It should be in OG-Financial where integration
  // with credit stuff is based, unless a curve identifier really is a fundamental primitive like ExternalId
  // or UniqueId.

  public CreditCurveIdentifierResolver() {
    super(CreditCurveIdentifier.OBJECT_SCHEME);
  }

  @Override
  protected CreditCurveIdentifier resolveObject(final UniqueId identifier) {
    return CreditCurveIdentifier.of(identifier);
  }

}
