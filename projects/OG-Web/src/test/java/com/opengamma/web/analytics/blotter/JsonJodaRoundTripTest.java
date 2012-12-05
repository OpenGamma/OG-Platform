/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.joda.beans.MetaBean;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

public class JsonJodaRoundTripTest {

  static {
    JodaBeanConverters.getInstance();
  }

  @Test
  public void fxForwardRoundTrip() throws JSONException {
    ZonedDateTime forwardDate = ZonedDateTime.of(2012, 12, 21, 10, 0, 0, 0, TimeZone.UTC);
    ExternalId regionId = ExternalId.of("Reg", "123");
    FXForwardSecurity fxForward = new FXForwardSecurity(Currency.USD, 150, Currency.GBP, 100, forwardDate, regionId);
    fxForward.setName("GBP/USD forward");

    JsonDataSink sink = new JsonDataSink();
    BeanVisitor<JSONObject> writingVisitor = new BuildingBeanVisitor<JSONObject>(fxForward, sink);
    BeanTraverser traverser = new BeanTraverser();
    JSONObject json = traverser.traverse(FXForwardSecurity.meta(), writingVisitor);
    assertNotNull(json);
    System.out.println(json);

    JsonBeanDataSource dataSource = new JsonBeanDataSource(new JSONObject(json.toString()));
    MetaBeanFactory metaBeanFactory = new MapMetaBeanFactory(ImmutableList.<MetaBean>of(FXForwardSecurity.meta()));
    BeanVisitor<FXForwardSecurity> readingVisitor = new BeanBuildingVisitor<FXForwardSecurity>(dataSource, metaBeanFactory);
    FXForwardSecurity fxForward2 = traverser.traverse(FXForwardSecurity.meta(), readingVisitor);
    assertEquals(fxForward, fxForward2);
  }

  @Test
  public void swapRoundTrip() throws JSONException {
    ZonedDateTime tradeDate = ZonedDateTime.of(2012, 12, 21, 10, 0, 0, 0, TimeZone.UTC);
    ZonedDateTime effectiveDate = ZonedDateTime.of(2013, 1, 21, 10, 0, 0, 0, TimeZone.UTC);
    ZonedDateTime maturityDate = ZonedDateTime.of(2013, 12, 21, 10, 0, 0, 0, TimeZone.UTC);
    SwapLeg payLeg = new FixedInterestRateLeg(
        DayCountFactory.INSTANCE.getDayCount("Act/360"),
        SimpleFrequency.MONTHLY,
        ExternalId.of("Reg", "123"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"),
        new InterestRateNotional(Currency.GBP, 123),
        false,
        0.01);
    SwapLeg receiveLeg = new FloatingInterestRateLeg(
        DayCountFactory.INSTANCE.getDayCount("Act/Act"),
        SimpleFrequency.QUARTERLY,
        ExternalId.of("Reg", "123"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"),
        new InterestRateNotional(Currency.GBP, 234),
        false,
        ExternalId.of("Rate", "asdf"),
        FloatingRateType.IBOR);
    SwapSecurity security = new SwapSecurity(tradeDate, effectiveDate, maturityDate, "cpty", payLeg, receiveLeg);
    security.setName("Test swap");

    JsonDataSink sink = new JsonDataSink();
    BeanTraverser traverser = new BeanTraverser();
    BeanVisitor<JSONObject> writingVisitor = new BuildingBeanVisitor<JSONObject>(security, sink);
    JSONObject json = traverser.traverse(SwapSecurity.meta(), writingVisitor);
    assertNotNull(json);
    System.out.println(json);

    JsonBeanDataSource dataSource = new JsonBeanDataSource(new JSONObject(json.toString()));
    MetaBeanFactory metaBeanFactory = new MapMetaBeanFactory(ImmutableList.<MetaBean>of(
        SwapSecurity.meta(),
        FixedInterestRateLeg.meta(),
        FloatingInterestRateLeg.meta(),
        InterestRateNotional.meta()));
    BeanVisitor<SwapSecurity> readingVisitor = new BeanBuildingVisitor<SwapSecurity>(dataSource, metaBeanFactory);
    SwapSecurity security2 = traverser.traverse(SwapSecurity.meta(), readingVisitor);
    assertEquals(security, security2);
  }
}
