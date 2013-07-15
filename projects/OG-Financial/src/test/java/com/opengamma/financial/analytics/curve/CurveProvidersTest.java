/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CurveProvidersTest {
  private static final ExternalId DE_CASH_SECURITY_TARGET = ExternalId.of("Security", "DE Cash");
  private static final DiscountingCurveConfiguration EUR_CASH_SECURITY_DISCOUNTING = new DiscountingCurveConfiguration("EUR Discounting 1", "EUR Config 1");
  private static final ForwardIborCurveConfiguration EUR_CASH_SECURITY_FORWARDING = new ForwardIborCurveConfiguration("EUR Forward3M 1", "EUR Config 1");
  private static final ExternalId EUR_3M_LIBOR_TARGET = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER.getName(), "EU0003M Index");
  private static final ForwardIborCurveConfiguration EUR_3M_LIBOR_FORWARDING = new ForwardIborCurveConfiguration("EUR Forward3M 2", "EUR Config 2");
  private static final ExternalId DE_TARGET = ExternalId.of("Region", "DE");
  private static final DiscountingCurveConfiguration DE_DISCOUNTING = new DiscountingCurveConfiguration("EUR Discounting 3", "EUR Config 3");
  private static final ForwardIborCurveConfiguration DE_FORWARDING = new ForwardIborCurveConfiguration("EUR Forward3M 3", "EUR Config 3");
  private static final ExternalId FR_TARGET = ExternalId.of("Region", "FR");
  private static final DiscountingCurveConfiguration FR_DISCOUNTING = new DiscountingCurveConfiguration("EUR Discounting 4", "EUR Config 4");
  private static final ForwardIborCurveConfiguration FR_FORWARDING = new ForwardIborCurveConfiguration("EUR Forward3M 4", "EUR Config 4");
  private static final ExternalId EUR_TARGET = ExternalId.of(Currency.OBJECT_SCHEME, "EUR");
  private static final DiscountingCurveConfiguration EUR_DISCOUNTING = new DiscountingCurveConfiguration("EUR Discounting 5", "EUR Config 5");
  private static final ForwardIborCurveConfiguration EUR_FORWARDING = new ForwardIborCurveConfiguration("EUR Forward3M 5", "EUR Config 5");
  private static final ExternalId DE_CASH_TARGET = ExternalId.of(CurveConfigurationForSecurityVisitor.SECURITY_IDENTIFIER, CashSecurity.SECURITY_TYPE + "_" + "DE");
  private static final DiscountingCurveConfiguration DE_CASH_DISCOUNTING = new DiscountingCurveConfiguration("EUR Discounting 6", "EUR Config 6");
  private static final ForwardIborCurveConfiguration DE_CASH_FORWARDING = new ForwardIborCurveConfiguration("EUR Forward3M 6", "EUR Config 6");
  private static final ExternalId EUR_CASH_TARGET = ExternalId.of(CurveConfigurationForSecurityVisitor.SECURITY_IDENTIFIER, CashSecurity.SECURITY_TYPE + "_" + "EUR");
  private static final DiscountingCurveConfiguration EUR_CASH_DISCOUNTING = new DiscountingCurveConfiguration("EUR Discounting 7", "EUR Config 7");
  private static final ForwardIborCurveConfiguration EUR_CASH_FORWARDING = new ForwardIborCurveConfiguration("EUR Forward3M 7", "EUR Config 7");
  private static final ExternalId CASH_TARGET = ExternalId.of(CurveConfigurationForSecurityVisitor.SECURITY_IDENTIFIER, CashSecurity.SECURITY_TYPE);
  private static final DiscountingCurveConfiguration CASH_DISCOUNTING = new DiscountingCurveConfiguration("Discounting 8", "Config 8");
  private static final CurveConfigurationSpecification DE_CONFIG = new CurveConfigurationSpecification(DE_TARGET, 2);
  private static final CurveConfigurationSpecification FR_CONFIG = new CurveConfigurationSpecification(FR_TARGET, 2);
  private static final CurveConfigurationSpecification DE_CASH_CONFIG = new CurveConfigurationSpecification(DE_CASH_TARGET, 4);
  private static final CurveConfigurationSpecification EUR_CASH_CONFIG = new CurveConfigurationSpecification(EUR_CASH_TARGET, 5);
  private static final CurveConfigurationSpecification EUR_CONFIG = new CurveConfigurationSpecification(EUR_TARGET, 6);
  private static final CurveConfigurationSpecification CASH_CONFIG = new CurveConfigurationSpecification(CASH_TARGET, 7);
  private static final CurveConfigurationSpecification EUR_3M_LIBOR_CONFIG = new CurveConfigurationSpecification(EUR_3M_LIBOR_TARGET, 1);
  private static final CurveConfigurationSpecification DE_CASH_SECURITY_CONFIG = new CurveConfigurationSpecification(DE_CASH_SECURITY_TARGET, 0);
  private static final InstrumentExposureConfiguration EXPOSURE_CONFIG;
  private static final SecurityCurveProvider PROVIDER = new SecurityCurveProvider();

  static {
    final List<CurveConfiguration> deCashSecurityConfigs = Arrays.asList(EUR_CASH_SECURITY_DISCOUNTING, EUR_CASH_SECURITY_FORWARDING);
    final List<CurveConfiguration> eur3MLiborConfigs = Arrays.asList((CurveConfiguration) EUR_3M_LIBOR_FORWARDING);
    final List<CurveConfiguration> deConfigs = Arrays.asList(DE_DISCOUNTING, DE_FORWARDING);
    final List<CurveConfiguration> frConfigs = Arrays.asList(FR_DISCOUNTING, FR_FORWARDING);
    final List<CurveConfiguration> eurConfigs = Arrays.asList(EUR_DISCOUNTING, EUR_FORWARDING);
    final List<CurveConfiguration> eurCashConfigs = Arrays.asList(EUR_CASH_DISCOUNTING, EUR_CASH_FORWARDING);
    final List<CurveConfiguration> deCashConfigs = Arrays.asList(DE_CASH_DISCOUNTING, DE_CASH_FORWARDING);
    final List<CurveConfiguration> cashConfigs = Arrays.asList((CurveConfiguration) CASH_DISCOUNTING);
    final Map<CurveConfigurationSpecification, Collection<CurveConfiguration>> specifications = new HashMap<>();
    specifications.put(DE_CASH_SECURITY_CONFIG, deCashSecurityConfigs);
    specifications.put(EUR_3M_LIBOR_CONFIG, eur3MLiborConfigs);
    specifications.put(EUR_CONFIG, eurConfigs);
    specifications.put(FR_CONFIG, frConfigs);
    specifications.put(DE_CONFIG, deConfigs);
    specifications.put(DE_CASH_CONFIG, deCashConfigs);
    specifications.put(EUR_CASH_CONFIG, eurCashConfigs);
    specifications.put(CASH_CONFIG, cashConfigs);
    EXPOSURE_CONFIG = new InstrumentExposureConfiguration(specifications);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoConfigForSecurity() {
    final EquitySecurity equity = new EquitySecurity("A", "B", "C", Currency.USD);
    equity.setUniqueId(UniqueId.of("Security", "Equity"));
    PROVIDER.getValueRequirements(equity, EXPOSURE_CONFIG);
  }

  @Test
  public void testSecurityLevelRequirements() {
    final CashSecurity cash = new CashSecurity(Currency.EUR, ExternalId.of("Region", "DE"),
        DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2014, 1, 1), DayCountFactory.INSTANCE.getDayCount("30/360"), 0.01, 10000);
    cash.setUniqueId(UniqueId.of(DE_CASH_SECURITY_TARGET.getScheme().getName(), DE_CASH_SECURITY_TARGET.getValue()));

    final Set<ValueRequirement> expected = new HashSet<>();
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Discounting 1").with(CURVE_CALCULATION_CONFIG, "EUR Config 1").get()));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Forward3M 1").with(CURVE_CALCULATION_CONFIG, "EUR Config 1").get()));
    assertEquals(expected, PROVIDER.getValueRequirements(cash, EXPOSURE_CONFIG));
  }

  @Test
  public void testTickerLevelRequirements() {
    final Set<ValueRequirement> expected = new HashSet<>();
    final FRASecurity fra = new FRASecurity(Currency.EUR, ExternalId.of("Region", "DE"), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 4, 1), 0.01,
        10000, ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER.getName(), "EU0003M Index"), DateUtils.getUTCDate(2013, 4, 1));
    fra.setUniqueId(UniqueId.of("Security", "DE FRA"));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Forward3M 2").with(CURVE_CALCULATION_CONFIG, "EUR Config 2").get()));
    assertEquals(expected, PROVIDER.getValueRequirements(fra, EXPOSURE_CONFIG));
  }

  @Test
  public void testSecurityTypeRegionLevelRequirements() {
    final Set<ValueRequirement> expected = new HashSet<>();
    final CashSecurity frCash = new CashSecurity(Currency.EUR, ExternalId.of("Region", "DE"),
        DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2014, 1, 1), DayCountFactory.INSTANCE.getDayCount("30/360"), 0.01, 10000);
    frCash.setUniqueId(UniqueId.of("Security", "DE Cash 2"));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Discounting 6").with(CURVE_CALCULATION_CONFIG, "EUR Config 6").get()));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Forward3M 6").with(CURVE_CALCULATION_CONFIG, "EUR Config 6").get()));
    assertEquals(expected, PROVIDER.getValueRequirements(frCash, EXPOSURE_CONFIG));
  }

  @Test
  public void testSecurityTypeCurrencyLevelRequirements() {
    final Set<ValueRequirement> expected = new HashSet<>();
    final CashSecurity cash = new CashSecurity(Currency.EUR, ExternalId.of("Region", "FR"),
        DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2014, 1, 1), DayCountFactory.INSTANCE.getDayCount("30/360"), 0.01, 10000);
    cash.setUniqueId(UniqueId.of("Security", "FR Cash"));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Discounting 7").with(CURVE_CALCULATION_CONFIG, "EUR Config 7").get()));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Forward3M 7").with(CURVE_CALCULATION_CONFIG, "EUR Config 7").get()));
    assertEquals(expected, PROVIDER.getValueRequirements(cash, EXPOSURE_CONFIG));
  }

  @Test
  public void testRegionLevelRequirements() {
    Set<ValueRequirement> expected = new HashSet<>();
    FRASecurity fra = new FRASecurity(Currency.EUR, ExternalId.of("Region", "DE"), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 7, 1), 0.01,
        10000, ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER.getName(), "EU0006M Index"), DateUtils.getUTCDate(2013, 7, 1));
    fra.setUniqueId(UniqueId.of("Security", "DE FRA"));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Discounting 3").with(CURVE_CALCULATION_CONFIG, "EUR Config 3").get()));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Forward3M 3").with(CURVE_CALCULATION_CONFIG, "EUR Config 3").get()));
    assertEquals(expected, PROVIDER.getValueRequirements(fra, EXPOSURE_CONFIG));

    expected = new HashSet<>();
    fra = new FRASecurity(Currency.EUR, ExternalId.of("Region", "FR"), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 7, 1), 0.01,
        10000, ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER.getName(), "EU0006M Index"), DateUtils.getUTCDate(2013, 7, 1));
    fra.setUniqueId(UniqueId.of("Security", "FR FRA"));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Discounting 4").with(CURVE_CALCULATION_CONFIG, "EUR Config 4").get()));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Forward3M 4").with(CURVE_CALCULATION_CONFIG, "EUR Config 4").get()));
    assertEquals(expected, PROVIDER.getValueRequirements(fra, EXPOSURE_CONFIG));
  }

  @Test
  public void testCurrencyLevelRequirements() {
    final Set<ValueRequirement> expected = new HashSet<>();
    final FRASecurity fra = new FRASecurity(Currency.EUR, ExternalId.of("Region", "IT"), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 4, 1), 0.01,
        10000, ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER.getName(), "EU0006M Index"), DateUtils.getUTCDate(2013, 4, 1));
    fra.setUniqueId(UniqueId.of("Security", "IT FRA"));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Discounting 5").with(CURVE_CALCULATION_CONFIG, "EUR Config 5").get()));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "EUR Forward3M 5").with(CURVE_CALCULATION_CONFIG, "EUR Config 5").get()));
    assertEquals(expected, PROVIDER.getValueRequirements(fra, EXPOSURE_CONFIG));
  }

  @Test
  public void testSecurityTypeLevelRequirements() {
    final Set<ValueRequirement> expected = new HashSet<>();
    final CashSecurity cash = new CashSecurity(Currency.USD, ExternalId.of("Region", "US"),
        DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2014, 1, 1), DayCountFactory.INSTANCE.getDayCount("30/360"), 0.01, 10000);
    cash.setUniqueId(UniqueId.of("Security", "US Cash"));
    expected.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(CURVE, "Discounting 8").with(CURVE_CALCULATION_CONFIG, "Config 8").get()));
    assertEquals(expected, PROVIDER.getValueRequirements(cash, EXPOSURE_CONFIG));
  }
}
