/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import static java.lang.String.format;

import java.math.BigDecimal;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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

  //-------------------------------------------------------------------------
  @Override
  public ManageableSecurity[] extractSecurities() {
    SwapTrade trade = getTrade();

    List<SwapLeg> payLegs = Lists.newArrayList(trade.getPayLegs());
    List<SwapLeg> recLegs = Lists.newArrayList(trade.getReceiveLegs());

    Preconditions.checkState(payLegs.size() == 1, format("Swaps must have one (and only one) pay leg. Found %d", payLegs.size()));
    Preconditions.checkState(recLegs.size() == 1, format("Swaps must have one (and only one) receive leg. Found %d", recLegs.size()));

    ManageableSecurity security = new SwapSecurity(
        convertLocalDate(trade.getTradeDate()),
        convertLocalDate(trade.getEffectiveDate()),
        convertLocalDate(trade.getMaturityDate()),
        trade.getCounterparty().getExternalId().getId(),
        convertLeg(payLegs.get(0)),
        convertLeg(recLegs.get(0)));

    return securityArray(addIdentifier(security));
  }

  /**
   * Converts the given leg to the appropriate type of leg in security lib.
   * @param leg the leg to convert
   * @return the converted leg.
   * @throws PortfolioParsingException if the leg is not of type {@link FixedLeg} or {@link FloatingLeg}.
   */
  private com.opengamma.financial.security.swap.SwapLeg convertLeg(SwapLeg leg) {
    if (leg instanceof FixedLeg) {
      return convertFixedLeg((FixedLeg) leg);
    } else if (leg instanceof FloatingLeg) {
      return convertFloatingLeg((FloatingLeg) leg);
    } else {
      throw new PortfolioParsingException(format("Unknown leg type detected: %s", leg.getClass().getName()));
    }
  }

  private com.opengamma.financial.security.swap.SwapLeg convertFixedLeg(FixedLeg fixedLeg) {

    Notional notional = extractNotional(fixedLeg);

    DayCount dayCount = DayCountFactory.of(fixedLeg.getDayCount());
    Frequency frequency = SimpleFrequencyFactory.of(fixedLeg.getFrequency());
    ExternalId region = extractRegion(fixedLeg);
    BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.of(fixedLeg.getBusinessDayConvention());
    boolean isEndOfMonth = fixedLeg.isEndOfMonth();
    return new FixedInterestRateLeg(dayCount, frequency, region, businessDayConvention, notional, isEndOfMonth,
        convertRate(fixedLeg.getRate()));
  }

  private com.opengamma.financial.security.swap.SwapLeg convertFloatingLeg(FloatingLeg floatingLeg) {

    Notional notional = extractNotional(floatingLeg);

    ExternalId region = extractRegion(floatingLeg);
    DayCount dayCount = DayCountFactory.of(floatingLeg.getDayCount());
    Frequency frequency = SimpleFrequencyFactory.of(floatingLeg.getFrequency());
    BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.of(floatingLeg.getBusinessDayConvention());
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
