/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/* package */ class BlotterTestUtils {

  /* package */ static final FXForwardSecurity FX_FORWARD;
  /* package */ static final BeanDataSource FX_FORWARD_DATA_SOURCE;
  /* package */ static final SwapSecurity SWAP;
  /* package */ static final BeanDataSource SWAP_DATA_SOURCE;
  /* package */ static final EquityVarianceSwapSecurity EQUITY_VARIANCE_SWAP;
  /* package */ static final BeanDataSource EQUITY_VARIANCE_SWAP_DATA_SOURCE;

  static {
    ImmutableMap<String, String> attributes = ImmutableMap.of("attr1", "attrVal1", "attr2", "attrVal2");
    String forwardDateStr = "2012-12-21T10:30+00:00[Europe/London]";
    FX_FORWARD_DATA_SOURCE = beanData(
        "name", "TODO",
        "externalIdBundle", "",
        "type", "FXForwardSecurity",
        "payCurrency", "USD",
        "payAmount", "150",
        "receiveCurrency", "GBP",
        "receiveAmount", "100",
        "forwardDate", forwardDateStr, // maybe need an explicit converter for this
        "regionId", "Reg~123",
        "attributes", attributes);

    ZonedDateTime forwardDate = ZonedDateTime.parse(forwardDateStr);
    ExternalId regionId = ExternalId.of("Reg", "123");
    FX_FORWARD = new FXForwardSecurity(Currency.USD, 150, Currency.GBP, 100, forwardDate, regionId);
    FX_FORWARD.setName("TODO");
    FX_FORWARD.setAttributes(attributes);

    //-------------------------------------

    String tradeDateStr = "2012-12-21T10:30+00:00[Europe/London]";
    String effectiveDateStr = "2012-12-23T10:30+00:00[Europe/London]";
    String maturityDateStr = "2013-12-21T10:30+00:00[Europe/London]";
    SWAP_DATA_SOURCE = beanData(
        "externalIdBundle", "",
        "name", "TODO",
        "type", "SwapSecurity",
        "attributes", attributes,
        "counterparty", "Cpty",
        "tradeDate", tradeDateStr,
        "effectiveDate", effectiveDateStr,
        "maturityDate", maturityDateStr,
        "payLeg", beanData(
          "type", "FixedInterestRateLeg",
          "rate", "1.234",
          "businessDayConvention", "Modified Following",
          "dayCount", "Act/360",
          "frequency", "3m",
          "regionId", "Reg~123",
          "eom", "true",
          "notional", "USD 222.33"),
        "receiveLeg", beanData(
          "type", "FloatingInterestRateLeg",
          "floatingReferenceRateId", "Rate~123",
          "initialFloatingRate", "321.9",
          "floatingRateType", "IBOR",
          "settlementDays", "5",
          "offsetFixing", "1m",
          "businessDayConvention", "Following",
          "dayCount", "Act/Act",
          "frequency", "6m",
          "regionId", "Reg~234",
          "eom", "true",
          "notional", "GBP 123.45"));

    ZonedDateTime tradeDate = ZonedDateTime.parse(tradeDateStr);
    ZonedDateTime effectiveDate = ZonedDateTime.parse(effectiveDateStr);
    ZonedDateTime maturityDate = ZonedDateTime.parse(maturityDateStr);

    SwapLeg payLeg = new FixedInterestRateLeg(
        DayCountFactory.INSTANCE.getDayCount("Act/360"),
        SimpleFrequencyFactory.INSTANCE.getFrequency("3m"),
        ExternalId.of("Reg", "123"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"),
        new InterestRateNotional(Currency.USD, 222.33),
        true,
        1.234);
    FloatingInterestRateLeg receiveLeg = new FloatingInterestRateLeg(
        DayCountFactory.INSTANCE.getDayCount("Act/Act"),
        SimpleFrequencyFactory.INSTANCE.getFrequency("6m"),
        ExternalId.of("Reg", "234"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"),
        new InterestRateNotional(Currency.GBP, 123.45),
        true,
        ExternalId.of("Rate", "123"),
        FloatingRateType.IBOR);
    receiveLeg.setInitialFloatingRate(321.9);
    receiveLeg.setSettlementDays(5);
    receiveLeg.setOffsetFixing(SimpleFrequencyFactory.INSTANCE.getFrequency("1m"));
    SWAP = new SwapSecurity(tradeDate, effectiveDate, maturityDate, "Cpty", payLeg, receiveLeg);
    SWAP.setName("TODO");
    SWAP.setAttributes(attributes);

    //-------------------------------------

    String firstObservationDateStr = "2012-12-21T10:30+00:00[Europe/London]";
    String lastObservationDateStr = "2013-12-21T10:30+00:00[Europe/London]";
    String settlementDateStr = "2013-12-25T10:30+00:00[Europe/London]";
    EQUITY_VARIANCE_SWAP_DATA_SOURCE = beanData(
        "externalIdBundle", "",
        "name", "TODO",
        "type", "EquityVarianceSwapSecurity",
        "spotUnderlyingId", "BLOOMBERG_TICKER~AAPL US Equity",
        "currency", "GBP",
        "strike", "0.1",
        "notional", "1234",
        "parameterizedAsVariance", "false",
        "annualizationFactor", "15",
        "firstObservationDate", firstObservationDateStr,
        "lastObservationDate", lastObservationDateStr,
        "settlementDate", settlementDateStr,
        "regionId", "Reg~123",
        "observationFrequency", "Weekly",
        "attributes", attributes
    );
    EQUITY_VARIANCE_SWAP =
        new EquityVarianceSwapSecurity(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "AAPL US Equity"),
                                       Currency.GBP,
                                       0.1,
                                       1234,
                                       false,
                                       15,
                                       ZonedDateTime.parse(firstObservationDateStr),
                                       ZonedDateTime.parse(lastObservationDateStr),
                                       ZonedDateTime.parse(settlementDateStr),
                                       ExternalId.of("Reg", "123"),
                                       SimpleFrequencyFactory.INSTANCE.getFrequency("Weekly"));
    EQUITY_VARIANCE_SWAP.setName("TODO");
    EQUITY_VARIANCE_SWAP.setAttributes(attributes);
  }

  /* package */ static BeanDataSource beanData(Object... pairs) {
    final Map<Object, Object> map = Maps.newHashMap();
    for (int i = 0; i < pairs.length / 2; i++) {
      map.put(pairs[i * 2], pairs[(i * 2) + 1]);
    }
    return new MapBeanDataSource(map);
  }

  @SuppressWarnings("unchecked")
  private static class MapBeanDataSource implements BeanDataSource {

    private final Map<Object, Object> _map;

    public MapBeanDataSource(Map<Object, Object> map) {
      _map = map;
    }

    @Override
    public String getValue(String propertyName) {
      return (String) _map.get(propertyName);
    }

    @Override
    public List<String> getCollectionValues(String propertyName) {
      return (List<String>) _map.get(propertyName);
    }

    @Override
    public Map<String, String> getMapValues(String propertyName) {
      return (Map<String, String>) _map.get(propertyName);
    }

    @Override
    public BeanDataSource getBeanData(String propertyName) {
      return (BeanDataSource) _map.get(propertyName);
    }

    @Override
    public String getBeanTypeName() {
      String type = getValue("type");
      if (type == null) {
        throw new OpenGammaRuntimeException("No type found in " + _map);
      }
      return type;
    }
  }
}
