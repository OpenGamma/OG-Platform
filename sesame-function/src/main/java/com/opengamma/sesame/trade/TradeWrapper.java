/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.trade;

import com.opengamma.core.security.Security;
import com.opengamma.util.ArgumentChecker;

/**
 * Base class that wraps a trade object.
 * This is needed to provide the engine with a explicit trade type
 * @param <S> instance of Security
 */
public abstract class TradeWrapper<S extends Security> {

  private final Class<S> _clazz;

  /**
   * Base trade wrapper constructor that wraps a trade in an explicit instrument type.
   * @param clazz the type of instrument, not null.
   * @param tradeBundle object containing the trade
   */
  public TradeWrapper(Class<S> clazz, ImmutableTradeBundle tradeBundle) {
    Security security = tradeBundle.getSecurity();
    ArgumentChecker.isTrue(clazz.isAssignableFrom(security.getClass()), security + " is not a " + clazz);
    _clazz = ArgumentChecker.notNull(clazz, "clazz");
  }

  /**
   * Gets the trade.
   * @return the value of the property, not null
   */
  public ImmutableTrade getTrade() {
    return ImmutableTrade.of(getTradeBundle());
  }

  protected abstract ImmutableTradeBundle getTradeBundle();

  public S getSecurity() {
    return _clazz.cast(getTrade().getSecurity());
  }

}
