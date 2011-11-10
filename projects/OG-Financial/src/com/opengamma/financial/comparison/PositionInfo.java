/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;

/* package */final class PositionInfo extends PositionOrTradeInfo<Position> {

  private final Set<TradeInfo> _trades;

  private final Map<String, String> _attributes;

  public PositionInfo(final ComparisonContext context, final Position position) {
    super(context, position);
    final Set<Trade> trades = position.getTrades();
    if (trades.isEmpty()) {
      _trades = Collections.emptySet();
    } else {
      final Set<TradeInfo> tradeInfos = Sets.newHashSetWithExpectedSize(trades.size());
      for (Trade trade : trades) {
        tradeInfos.add(new TradeInfo(context, trade));
      }
      _trades = Collections.unmodifiableSet(tradeInfos);
    }
    _attributes = context.isIgnorePositionAttributes() ? Collections.<String, String>emptyMap() : position.getAttributes();
  }

  public Set<TradeInfo> getTrades() {
    return _trades;
  }

  public Map<String, String> getAttributes() {
    return _attributes;
  }
  
  @Override
  public String toString() {
    return "PositionInfo[quantity=" + getQuantity() + ", security=" + getSecurity() + ", trades=" + getTrades() + ", attributes=" + getAttributes() + "]";
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof PositionInfo)) {
      return false;
    }
    final PositionInfo other = (PositionInfo) o;
    if (!equalsImpl(other)) {
      return false;
    }
    return getTrades().equals(other.getTrades())
        && getAttributes().equals(other.getAttributes());
  }

  @Override
  public int hashCode() {
    int hc = hashCodeImpl();
    hc += (hc << 4) + getTrades().hashCode();
    hc += (hc << 4) + getAttributes().hashCode();
    return hc;
  }

}
