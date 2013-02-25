/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.HashCodeBuilder;

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
import com.opengamma.util.money.Currency;

public class SwapTradeSecurityExtractor extends TradeSecurityExtractor<SwapTrade> {

  @Override
  public ManageableSecurity[] extractSecurity(SwapTrade swapTrade) {
    FixedLeg fixedLeg = swapTrade.getFixedLeg();
    FloatingLeg floatingLeg = swapTrade.getFloatingLeg();

    com.opengamma.financial.security.swap.SwapLeg payLeg = fixedLeg.getDirection() == SwapLeg.Direction.Pay ?
        convertFixedLeg(fixedLeg) : convertFloatingLeg(floatingLeg);
    com.opengamma.financial.security.swap.SwapLeg receiveLeg = fixedLeg.getDirection() == SwapLeg.Direction.Receive ?
        convertFixedLeg(fixedLeg) : convertFloatingLeg(floatingLeg);

    if (payLeg == receiveLeg) {
      throw new OpenGammaRuntimeException("One leg should be Pay and one Receive");
    }

    ManageableSecurity security = new SwapSecurity(convertLocalDate(swapTrade.getTradeDate()),
                                             convertLocalDate(swapTrade.getEffectiveDate()),
                                             convertLocalDate(swapTrade.getMaturityDate()),
                                             swapTrade.getCounterparty().getExternalId().getId(),
                                             payLeg,
                                             receiveLeg);

    // Generate the loader SECURITY_ID (should be uniquely identifying)
    security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
        new HashCodeBuilder()
            .append(security.getClass())
            .append(swapTrade.getTradeDate())
            .append(swapTrade.getEffectiveDate())
            .append(swapTrade.getMaturityDate())
            .append(floatingLeg.getFixingIndex())
            .append(fixedLeg.getRate())
            .toHashCode())));

    return securityArray(security);
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


  private Notional extractNotional(SwapLeg floatingLeg) {
    Currency currency = Currency.of(floatingLeg.getCurrency());
    return new InterestRateNotional(currency, floatingLeg.getNotional().doubleValue());
  }

  private ExternalId extractRegion(SwapLeg floatingLeg) {
    return extractRegion(floatingLeg.getPaymentCalendars());
  }

  private double convertRate(BigDecimal rate) {
    return rate.divide(new BigDecimal(100)).doubleValue();
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
}
