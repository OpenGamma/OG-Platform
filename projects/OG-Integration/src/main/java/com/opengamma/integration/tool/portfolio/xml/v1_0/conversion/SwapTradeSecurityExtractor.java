/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import java.math.BigDecimal;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FixedLeg;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FixingIndex;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FloatingLeg;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwapLeg;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwapTrade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Security extractor for swap trades.
 */
public class SwapTradeSecurityExtractor extends TradeSecurityExtractor<SwapTrade> {

  /**
   * Create a security extractor for the supplied trade.
   *
   * @param trade the trade to perform extraction on
   */
  public SwapTradeSecurityExtractor(SwapTrade trade) {
    super(trade);
  }

  @Override
  public ManageableSecurity[] extractSecurities() {

    FixedLeg fixedLeg = _trade.getFixedLeg();
    FloatingLeg floatingLeg = _trade.getFloatingLeg();

    com.opengamma.financial.security.swap.SwapLeg payLeg = fixedLeg.getDirection() == SwapLeg.Direction.PAY ?
        convertFixedLeg(fixedLeg) : convertFloatingLeg(floatingLeg);
    com.opengamma.financial.security.swap.SwapLeg receiveLeg = fixedLeg.getDirection() == SwapLeg.Direction.RECEIVE ?
        convertFixedLeg(fixedLeg) : convertFloatingLeg(floatingLeg);

    if (payLeg == receiveLeg) {
      throw new OpenGammaRuntimeException("One leg should be Pay and one Receive");
    }

    ManageableSecurity security = new SwapSecurity(convertLocalDate(_trade.getTradeDate()),
                                             convertLocalDate(_trade.getEffectiveDate()),
                                             convertLocalDate(_trade.getMaturityDate()),
                                             _trade.getCounterparty().getExternalId().getId(),
                                             payLeg,
                                             receiveLeg);

    return securityArray(addIdentifier(security));
  }

  private com.opengamma.financial.security.swap.SwapLeg convertFixedLeg(FixedLeg fixedLeg) {

    Notional notional = extractNotional(fixedLeg);

    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(fixedLeg.getDayCount());
    Frequency frequency = SimpleFrequencyFactory.INSTANCE.getFrequency(fixedLeg.getFrequency());
    ExternalId region = extractRegion(fixedLeg);
    BusinessDayConvention businessDayConvention =
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(fixedLeg.getBusinessDayConvention());
    boolean isEndOfMonth = fixedLeg.isEndOfMonth();
    return new FixedInterestRateLeg(dayCount, frequency, region, businessDayConvention, notional, isEndOfMonth,
                                    convertRate(fixedLeg.getRate()));
  }

  private com.opengamma.financial.security.swap.SwapLeg convertFloatingLeg(FloatingLeg floatingLeg) {

    Notional notional = extractNotional(floatingLeg);

    ExternalId region = extractRegion(floatingLeg);
    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(floatingLeg.getDayCount());
    Frequency frequency = SimpleFrequencyFactory.INSTANCE.getFrequency(floatingLeg.getFrequency());
    BusinessDayConvention businessDayConvention =
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(floatingLeg.getBusinessDayConvention());
    boolean isEndOfMonth = floatingLeg.isEndOfMonth();

    FixingIndex fixingIndex = floatingLeg.getFixingIndex();
    ExternalId referenceRate = fixingIndex.getIndex().toExternalId();
    FloatingRateType rateType = FloatingRateType.valueOf(fixingIndex.getRateType().toString());

    return new FloatingInterestRateLeg(dayCount, frequency, region, businessDayConvention, notional, isEndOfMonth,
                                       referenceRate, rateType);
  }

  private Notional extractNotional(SwapLeg floatingLeg) {
    return new InterestRateNotional(floatingLeg.getCurrency(), floatingLeg.getNotional().doubleValue());
  }

  private ExternalId extractRegion(SwapLeg floatingLeg) {
    return extractRegion(floatingLeg.getPaymentCalendars());
  }

  private double convertRate(BigDecimal rate) {
    return rate.divide(new BigDecimal(100)).doubleValue();
  }
}
