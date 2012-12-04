/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class SecurityBuilderTest {

  private static final double DELTA = 0.0001;

  /**
   * test building a nice simple security
   */
  @Test(enabled = false)
  public void buildFXForwardSecurity() {
    ImmutableMap<String, String> attributes = ImmutableMap.of("attr1", "attrVal1", "attr2", "attrVal2");
    BeanDataSource dataSource = data(
        "type", "FXForwardSecurity",
        "name", "FX Forward GBP/USD",
        "externalIdBundle", "Ext~123, Ext~234",
        "payCurrency", "USD",
        "payAmount", "150",
        "receiveCurrency", "GBP",
        "receiveAmount", "100",
        "forwardDate", "2012-12-21T10:30+00:00[Europe/London]", // maybe need an explicit converter for this
        "regionId", "Reg~123",
        "attributes", attributes);
    ManageableSecurity security = SecurityBuilder.buildBean(dataSource);
    assertNotNull(security);
    assertTrue(security instanceof FXForwardSecurity);
    FXForwardSecurity fxSecurity = (FXForwardSecurity) security;
    assertEquals(Currency.USD, fxSecurity.getPayCurrency());
    assertEquals(150d, fxSecurity.getPayAmount(), DELTA);
    assertEquals(Currency.GBP, fxSecurity.getReceiveCurrency());
    assertEquals(100d, fxSecurity.getReceiveAmount(), DELTA);
    assertEquals(ZonedDateTime.parse("2012-12-21T10:30+00:00[Europe/London]"), fxSecurity.getForwardDate());
    assertEquals(ExternalId.of("Reg", "123"), fxSecurity.getRegionId());
    assertEquals("FX Forward GBP/USD", fxSecurity.getName());
    assertEquals(attributes, fxSecurity.getAttributes());
    assertEquals(fxSecurity.getExternalIdBundle(), ExternalIdBundle.of(ExternalId.of("Ext", "123"),
                                                                       ExternalId.of("Ext", "234")));
    assertEquals(FXForwardSecurity.SECURITY_TYPE, fxSecurity.getSecurityType());
  }

  @Test(enabled = false)
  public void buildSwapSecurity() {
    Map<String, String> attributes = ImmutableMap.of("attr1", "attrVal1", "attr2", "attrVal2");
    BeanDataSource dataSource = data(
        "type", "SwapSecurity",
        "name", "Swap Security",
        "externalIdBundle", "Ext~123, Ext~234",
        "attributes", attributes,
        "counterparty", "Cpty",
        "tradeDate", "2012-12-21T10:30+00:00[Europe/London]",
        "effectiveDate", "2012-12-23T10:30+00:00[Europe/London]",
        "maturityDate", "2013-12-21T10:30+00:00[Europe/London]",
        "payLeg", data(
          "type", "FixedInterestRateLeg",
          "rate", "1.234",
          "businessDayConvention", "Modified Following",
          "dayCount", "Act/360",
          "frequency", "3m",
          "regionId", "Reg~123",
          "eom", "true",
          "notional", data(
            "type", "InterestRateNotional",
            "currency", "USD",
            "amount", "222.33")),
        "receiveLeg", data(
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
          "notional", data(
            "type", "InterestRateNotional",
            "currency", "GBP",
            "currency", "GBP",
            "amount", "123.45")));
    ManageableSecurity security = SecurityBuilder.buildBean(dataSource);
    assertNotNull(security);
    assertTrue(security instanceof SwapSecurity);
    SwapSecurity swapSecurity = (SwapSecurity) security;
    assertEquals("Swap Security", swapSecurity.getName());
    assertEquals(SwapSecurity.SECURITY_TYPE, swapSecurity.getSecurityType());
    assertEquals(ExternalIdBundle.of(ExternalId.of("Ext", "123"), ExternalId.of("Ext", "234")),
                 swapSecurity.getExternalIdBundle());
    assertEquals(attributes, swapSecurity.getAttributes());
    assertEquals("Cpty", swapSecurity.getCounterparty());
    assertEquals(ZonedDateTime.parse("2012-12-21T10:30+00:00[Europe/London]"), swapSecurity.getTradeDate());
    assertEquals(ZonedDateTime.parse("2012-12-23T10:30+00:00[Europe/London]"), swapSecurity.getEffectiveDate());
    assertEquals(ZonedDateTime.parse("2013-12-21T10:30+00:00[Europe/London]"), swapSecurity.getMaturityDate());
  }

  private static BeanDataSource data(Object... pairs) {
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
