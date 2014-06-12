/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative;

import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AgricultureFutureSecurity extends CommodityFutureSecurity {

  public AgricultureFutureSecurity(final double lastTradingTime, final CommodityUnderlying underlying, final String unitName, final double unitAmount, final double noticeFirstTime,
      final double noticeLastTime, final double firstDeliveryTime,
      final double lastDeliveryTime, final SettlementType settlementType, final double settlementTime, final String name, final Calendar calendar) {
    super(lastTradingTime, underlying, unitName, unitAmount, noticeFirstTime, noticeLastTime, firstDeliveryTime, lastDeliveryTime, settlementType, settlementTime, name, calendar);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitAgricultureFutureSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitAgricultureFutureSecurity(this);
  }

}
