/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.math.BigDecimal;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.id.UniqueId;

/**
 * A position or trade implementation that may not be fully resolved at construction but will appear fully resolved when used.
 */
/* package */abstract class LazyResolvedPositionOrTrade<T extends PositionOrTrade> extends LazyResolvedObject<T> implements PositionOrTrade {

  private volatile boolean _resolved;

  /**
   * Creates a new lazily resolved position.
   * 
   * @param underlying the underlying, un-resolved position
   * @param context the lazy resolution context
   */
  public LazyResolvedPositionOrTrade(final LazyResolveContext.AtVersionCorrection context, final T underlying) {
    super(context, underlying);
  }

  @Override
  public UniqueId getUniqueId() {
    return getUnderlying().getUniqueId();
  }

  @Override
  public BigDecimal getQuantity() {
    return getUnderlying().getQuantity();
  }

  @Override
  public SecurityLink getSecurityLink() {
    return getUnderlying().getSecurityLink();
  }

  @Override
  public Security getSecurity() {
    final SecurityLink link = getSecurityLink();
    if (_resolved) {
      return link.getTarget();
    } else {
      Security target = getLazyResolveContext().resolveLink(link);
      if (target == null) {
        throw new OpenGammaRuntimeException("Couldn't resolve " + link);
      }
      _resolved = true;
      return target;
    }
  }

}
