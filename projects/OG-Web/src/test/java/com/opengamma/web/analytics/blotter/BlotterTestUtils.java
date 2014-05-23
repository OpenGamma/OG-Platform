/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/* package */ class BlotterTestUtils {

  /* package */ static final FXForwardSecurity FX_FORWARD;
  /* package */ static final MapBeanDataSource FX_FORWARD_DATA_SOURCE;
  /* package */ static final SwapSecurity SWAP;
  /* package */ static final MapBeanDataSource SWAP_DATA_SOURCE;
  /* package */ static final SwaptionSecurity SWAPTION;
  /* package */ static final MapBeanDataSource SWAPTION_DATA_SOURCE;
  /* package */ static final EquityVarianceSwapSecurity EQUITY_VARIANCE_SWAP;
  /* package */ static final MapBeanDataSource EQUITY_VARIANCE_SWAP_DATA_SOURCE;

  static {
    Map<String, String> attributes = ImmutableMap.of("attr1", "attrVal1", "attr2", "attrVal2");
    List<String> permissions = Lists.newArrayList("perm1", "perm2");
    String forwardDateStr = "2012-12-21";
    FX_FORWARD_DATA_SOURCE = beanData(
        "name", "TODO",
        "externalIdBundle", "",
        "type", "FXForwardSecurity",
        "payCurrency", "USD",
        "payAmount", "150",
        "receiveCurrency", "GBP",
        "receiveAmount", "100",
        "forwardDate", forwardDateStr,
        "attributes", attributes,
        "requiredPermissions", permissions);

    ZonedDateTime forwardDate = parseDate(forwardDateStr);
    ExternalId regionId = ExternalId.of(ExternalSchemes.FINANCIAL, "GB");
    FX_FORWARD = new FXForwardSecurity(Currency.USD, 150, Currency.GBP, 100, forwardDate, regionId);
    FX_FORWARD.setName("TODO");
    FX_FORWARD.setAttributes(attributes);
    FX_FORWARD.setRequiredPermissions(Sets.newHashSet(permissions));

    //-------------------------------------

    String tradeDateStr = "2012-12-21";
    String effectiveDateStr = "2012-12-23";
    String maturityDateStr = "2013-12-21";
    SWAP_DATA_SOURCE = beanData(
        "externalIdBundle", "",
        "name", "TODO",
        "type", "SwapSecurity",
        "attributes", attributes,
        "counterparty", "Cpty",
        "tradeDate", tradeDateStr,
        "effectiveDate", effectiveDateStr,
        "maturityDate", maturityDateStr,
        "exchangeInitialNotional", "false",
        "exchangeFinalNotional", "false",
        "payLeg", beanData(
          "type", "FixedInterestRateLeg",
          "rate", "1.234",
          "businessDayConvention", "Modified Following",
          "dayCount", "Act/360",
          "frequency", "3m",
          "regionId", "123",
          "eom", "true",
          "notional", beanData(
            "type", "InterestRateNotional",
            "currency", "USD",
            "amount", "222.33")),
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
          "regionId", "234",
          "eom", "true",
          "notional", beanData(
            "type", "InterestRateNotional",
            "currency", "GBP",
            "amount", "123.45")));

    ZonedDateTime tradeDate = parseDate(tradeDateStr);
    ZonedDateTime effectiveDate = parseDate(effectiveDateStr);
    ZonedDateTime maturityDate = parseDate(maturityDateStr);

    SwapLeg payLeg = new FixedInterestRateLeg(
        DayCounts.ACT_360,
        SimpleFrequencyFactory.of("3m"),
        ExternalId.of(ExternalSchemes.FINANCIAL, "123"),
        BusinessDayConventions.MODIFIED_FOLLOWING,
        new InterestRateNotional(Currency.USD, 222.33),
        true,
        1.234);
    FloatingInterestRateLeg receiveLeg = new FloatingInterestRateLeg(
        DayCounts.ACT_ACT_ISDA,
        SimpleFrequencyFactory.of("6m"),
        ExternalId.of(ExternalSchemes.FINANCIAL, "234"),
        BusinessDayConventions.FOLLOWING,
        new InterestRateNotional(Currency.GBP, 123.45),
        true,
        ExternalId.of("Rate", "123"),
        FloatingRateType.IBOR);
    receiveLeg.setInitialFloatingRate(321.9);
    receiveLeg.setSettlementDays(5);
    receiveLeg.setOffsetFixing(SimpleFrequencyFactory.of("1m"));
    SWAP = new SwapSecurity(tradeDate, effectiveDate, maturityDate, "Cpty", payLeg, receiveLeg);
    SWAP.setName("TODO");
    SWAP.setAttributes(attributes);

    //-------------------------------------

    String firstObservationDateStr = "2012-12-21";
    String lastObservationDateStr = "2013-12-21";
    String settlementDateStr = "2013-12-25";
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
        "regionId", "123",
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
                                       parseDate(firstObservationDateStr),
                                       parseDate(lastObservationDateStr),
                                       parseDate(settlementDateStr),
                                       ExternalId.of(ExternalSchemes.FINANCIAL, "123"),
                                       SimpleFrequencyFactory.of("Weekly"));
    EQUITY_VARIANCE_SWAP.setName("TODO");
    EQUITY_VARIANCE_SWAP.setAttributes(attributes);

    //-------------------------------------

    SWAPTION_DATA_SOURCE = beanData(
        "type", "SwaptionSecurity",
        "name", "TODO",
        "attributes", attributes,
        "payer", "true",
        "longShort", "Short",
        "expiry", "2013-03-08",
        "cashSettled", "false",
        "currency", "GBP",
        "notional", "100000",
        "exerciseType", "European",
        "settlementDate", "2013-03-10");
    // need to provide a value for the underlying ID but it isn't used
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(2013, 3, 8, 0, 0), ZoneOffset.UTC));
    ZonedDateTime settlementDate = ZonedDateTime.of(LocalDateTime.of(2013, 3, 10, 11, 0), ZoneOffset.UTC);
    SWAPTION = new SwaptionSecurity(true, ExternalId.of("ABC", "123"), false, expiry, false, Currency.GBP, 100000d,
                                    new EuropeanExerciseType(), settlementDate);
    SWAPTION.setName("TODO");
    SWAPTION.setAttributes(attributes);
  }

  private static ZonedDateTime parseDate(String dateStr) {
    return LocalDate.parse(dateStr).atTime(11, 0).atZone(ZoneOffset.UTC);
  }

  /* package */ static MapBeanDataSource overrideBeanData(MapBeanDataSource delegate, Object... pairs) {
    final Map<Object, Object> map = Maps.newHashMap();
    for (int i = 0; i < pairs.length / 2; i++) {
      map.put(pairs[i * 2], pairs[(i * 2) + 1]);
    }
    return new MapBeanDataSource(delegate, map);
  }

  /* package */ static MapBeanDataSource beanData(Object... pairs) {
    final Map<Object, Object> map = Maps.newHashMap();
    for (int i = 0; i < pairs.length / 2; i++) {
      map.put(pairs[i * 2], pairs[(i * 2) + 1]);
    }
    return new MapBeanDataSource(map);
  }

  @SuppressWarnings("unchecked")
  private static class MapBeanDataSource implements BeanDataSource {

    private final Map<Object, Object> _map;

    private MapBeanDataSource(MapBeanDataSource delegate, Map<Object, Object> map) {
      _map = Maps.newHashMap(delegate._map);
      _map.putAll(map);
    }

    private MapBeanDataSource(Map<Object, Object> map) {
      _map = map;
    }

    @Override
    public Object getValue(String propertyName) {
      return _map.get(propertyName);
    }

    @Override
    public List<Object> getCollectionValues(String propertyName) {
      return (List<Object>) _map.get(propertyName);
    }

    @Override
    public Map<?, ?> getMapValues(String propertyName) {
      return (Map<?, ?>) _map.get(propertyName);
    }

    @Override
    public String getBeanTypeName() {
      String type = (String) getValue("type");
      if (type == null) {
        throw new OpenGammaRuntimeException("No type found in " + _map);
      }
      return type;
    }
  }
}
