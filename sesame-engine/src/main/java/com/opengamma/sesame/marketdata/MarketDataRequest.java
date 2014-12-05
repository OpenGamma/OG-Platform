/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A request for a single item or market data.
 */
public final class MarketDataRequest {

  private final ExternalIdBundle _id;
  private final FieldName _fieldName;

  private MarketDataRequest(ExternalIdBundle id, FieldName fieldName) {
    _id = ArgumentChecker.notNull(id, "id");
    _fieldName = ArgumentChecker.notNull(fieldName, "fieldName");
  }

  /**
   * Creates a request for a piece of market data with a specified ID and field name.
   *
   * @param id ID of the market data
   * @param fieldName name of the field holding the market data in the market data record
   * @return a request for a piece of market data with the specified ID and field name.
   */
  public static MarketDataRequest of(ExternalIdBundle id, FieldName fieldName) {
    return new MarketDataRequest(id, fieldName);
  }

  /**
   * Creates a request for a piece of market data with a specified ID
   * and field name {@link MarketDataUtils#MARKET_VALUE}.
   *
   * @param id ID of the market data
   * @return a request for a piece of market data with the specified ID and field name.
   */
  public static MarketDataRequest of(ExternalIdBundle id) {
    return new MarketDataRequest(id, MarketDataUtils.MARKET_VALUE);
  }

  /**
   * Creates a set of requests for market data with specified IDs
   * and field name {@link MarketDataUtils#MARKET_VALUE}.
   *
   * @param ids IDs of the market data
   * @return a request for a piece of market data with the specified ID and field name.
   */
  public static Set<MarketDataRequest> of(ExternalIdBundle... ids) {
    ImmutableSet.Builder<MarketDataRequest> requests = ImmutableSet.builder();

    for (ExternalIdBundle id : ids) {
      requests.add(new MarketDataRequest(id, MarketDataUtils.MARKET_VALUE));
    }
    return requests.build();
  }

  /**
   * @return the ID of the market data
   */
  public ExternalIdBundle getId() {
    return _id;
  }

  /**
   * @return the field name in the market data record that holds the data
   */
  public FieldName getFieldName() {
    return _fieldName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id, _fieldName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final MarketDataRequest other = (MarketDataRequest) obj;
    return Objects.equals(this._id, other._id) && Objects.equals(this._fieldName, other._fieldName);
  }

  @Override
  public String toString() {
    return "MarketDataRequest [_id=" + _id + ", _fieldName=" + _fieldName + "]";
  }
}
