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
   * @param tradeBundle object containing the trade data
   */
  public TradeWrapper(Class<S> clazz, ImmutableTradeBundle tradeBundle) {
    Security security = tradeBundle.getSecurity();

    ArgumentChecker.isTrue(clazz.isAssignableFrom(security.getClass()), "Failed to wrap trade, due to security type. " +
        "{} is not a {}", security, clazz);
    _clazz = ArgumentChecker.notNull(clazz, "clazz");
  }

  /**
   * Gets the trade.
   * @return the an ImmutableTrade, built from the TradeBundle, not null
   */
  public ImmutableTrade getTrade() {
    return ImmutableTrade.of(getTradeBundle());
  }

  /**
   * ImmutableTradeBundle stores the fields on the TradeWrapper 
   * in an immutable manner. This provides access to the bundle.
   * 
   * @return the underlying immutable trade bundle
   */
  public abstract ImmutableTradeBundle getTradeBundle();

  /**
   * The security instance for the underlying trade.
   * 
   * @return a security instance of type S
   */
  public S getSecurity() {
    return _clazz.cast(getTrade().getSecurity());
  }
  
  /**
   * Creates a new TradeWrapper, replacing the bundle of this instance
   * with the one supplied. This is provided for convenience when
   * the client needs to update fields on the bundle but does not
   * know the underlying type.
   * 
   * @param bundle the new tradle bundle
   * @return a new trade wrapper 
   */
  public abstract TradeWrapper<S> updateBundle(ImmutableTradeBundle bundle);

}
