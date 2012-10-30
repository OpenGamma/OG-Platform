package com.opengamma.financial.analytics.model.cds;

import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.core.security.impl.test.MockSecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

public class ISDAApproxCDSPriceFlatSpreadFunctionTest {

  private static final Security SECURITY = new SimpleSecurity(UniqueId.of("Test", "SEC"), ExternalIdBundle.EMPTY, "Test security", "EQUITY");
  private static MockSecuritySource securitySource;
  private static FunctionCompilationContext functionCompilationContext;
  private static final CDSSecurity CDS_SECURITY = new CDSSecurity(1.0, 0.6, 0.0025, Currency.GBP, ZonedDateTime.of(2020, 12, 20, 0, 0, 0, 0, TimeZone.UTC), ZonedDateTime.now(), SimpleFrequency.ANNUAL,
    DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), StubType.SHORT_START, 3,
    "US Treasury", Currency.USD, "Senior", "No Restructuring");
  private ISDAApproxCDSPriceFlatSpreadFunction testItem;

  @BeforeClass
  public static void initBeforeClass() {
    securitySource = new MockSecuritySource();
    functionCompilationContext = new FunctionCompilationContext();
    functionCompilationContext.setFunctionInitId(123);
    functionCompilationContext.setSecuritySource(securitySource);
    final MapComputationTargetResolver targetResolver = new MapComputationTargetResolver();
    functionCompilationContext.setComputationTargetResolver(targetResolver);

    CDS_SECURITY.setUniqueId(UniqueId.of("dummy_scheme", "dummy_value"));
  }

  @BeforeMethod
  public void beforeEachMethod() {
    testItem = new ISDAApproxCDSPriceFlatSpreadFunction();
  }

  @Test
  public void canApplyTo() {
    boolean result = testItem.canApplyTo(null, new ComputationTarget("test"));
    Assert.assertFalse(result);
  }

  @Test
  public void canApplyTo1() {
    boolean result = testItem.canApplyTo(null, new ComputationTarget(SECURITY));
    Assert.assertFalse(result);
  }

  @Test
  public void canApplyTo2() {
    boolean result = testItem.canApplyTo(null, new ComputationTarget(CDS_SECURITY));
    Assert.assertTrue(result);
  }

  @Test
  public void execute() {
    //TODO
  }

  @Test
  public void getRequirements() {

    ValueRequirement requirement = new ValueRequirement(ValueRequirementNames.CLEAN_PRICE, ComputationTargetType.SECURITY, CDS_SECURITY.getUniqueId(),
      ValueProperties
        .with(ValuePropertyNames.CURRENCY, Currency.USD.getCode())
        .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX)
        .with(ISDAFunctionConstants.ISDA_HAZARD_RATE_STRUCTURE, ISDAFunctionConstants.ISDA_HAZARD_RATE_FLAT)
        .get());

    Set<ValueRequirement> result = testItem.getRequirements(functionCompilationContext, new ComputationTarget(CDS_SECURITY), requirement);
    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 2);

    TreeSet<String> r = new TreeSet<String>();
    for (ValueRequirement valueRequirement : result) {
      r.add(valueRequirement.toString());
    }

    Assert
      .assertEquals(
        r.toString(),
        "[ValueReq[SpotRate, CTSpec[SECURITY, dummy_scheme~dummy_value], EMPTY], ValueReq[YieldCurve, CTSpec[PRIMITIVE, CurrencyISO~GBP], {CalculationMethod=[ISDA]}]]");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getResults1() {
    testItem.getResults(null, new ComputationTarget(CDS_SECURITY));
  }

  @Test
  public void getResults2() {
    //TODO
  }

  @Test
  public void getTargetType() {
    Assert.assertEquals(testItem.getTargetType(), ComputationTargetType.SECURITY);
  }
}
