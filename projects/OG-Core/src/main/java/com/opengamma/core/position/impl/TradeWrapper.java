/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.joda.beans.Property;
import org.joda.beans.Bean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.BeanBuilder;
import java.util.NoSuchElementException;
import java.util.Set;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;

/**
 * Base class that wraps a trade object.
 * This is needed to provide the engine with a explicit trade type
 * @param <S> instance of Security
 */
public abstract class TradeWrapper<S extends Security> implements Trade, Bean {

  private final Class<S> _clazz;
  
  /**
   * Base trade wrapper constructor that wraps a trade in an explicit instrument type.
   * @param trade the trade containing the instrument, not null.
   * @param clazz the type of instrument, not null.
   */
  public TradeWrapper(Class<S> clazz, Trade trade) {

    ArgumentChecker.isTrue(clazz.isAssignableFrom(trade.getSecurity().getClass()),
                           trade.getSecurity() + " is not a " + clazz);
    _clazz = ArgumentChecker.notNull(clazz, "clazz");

  }

  @Override
  public UniqueId getUniqueId() {
    return getTrade().getUniqueId();
  }

  protected abstract Trade getTrade();

  @Override
  public BigDecimal getQuantity() {
    return getTrade().getQuantity();
  }

  @Override
  public SecurityLink getSecurityLink() {
    return getTrade().getSecurityLink();
  }

  @Override
  public S getSecurity() {
    return _clazz.cast(getTrade().getSecurity());
  }

  @Override
  public Map<String, String> getAttributes() {
    return getTrade().getAttributes();
  }

  @Override
  public void setAttributes(Map<String, String> attributes) {
    getTrade().setAttributes(attributes);
  }

  @Override
  public void addAttribute(String key, String value) {
    getTrade().addAttribute(key, value);
  }

  @Override
  public Counterparty getCounterparty() {
    return getTrade().getCounterparty();
  }

  @Override
  public LocalDate getTradeDate() {
    return getTrade().getTradeDate();
  }

  @Override
  public OffsetTime getTradeTime() {
    return getTrade().getTradeTime();
  }

  @Override
  public Double getPremium() {
    return getTrade().getPremium();
  }

  @Override
  public Currency getPremiumCurrency() {
    return getTrade().getPremiumCurrency();
  }

  @Override
  public LocalDate getPremiumDate() {
    return getTrade().getPremiumDate();
  }

  @Override
  public OffsetTime getPremiumTime() {
    return getTrade().getPremiumTime();
  }

}
