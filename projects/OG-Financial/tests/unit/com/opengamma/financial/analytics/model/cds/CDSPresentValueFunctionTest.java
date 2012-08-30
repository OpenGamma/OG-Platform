package com.opengamma.financial.analytics.model.cds;

import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

public class CDSPresentValueFunctionTest {

  private static final Security SECURITY = new SimpleSecurity(UniqueId.of("Test", "SEC"), ExternalIdBundle.EMPTY, "Test security", "EQUITY");
  private static ExternalId underlying = ExternalSchemes.bloombergBuidSecurityId("dummy");
  private static GovernmentBondSecurity target;
  private static MockSecuritySource securitySource;
  private static FunctionCompilationContext functionCompilationContext;
  private static final Security CDS_SECURITY = new CDSSecurity(1.0, 0.6, 0.0025, Currency.GBP, ZonedDateTime.of(2020, 12, 20, 0, 0, 0, 0, TimeZone.UTC), ZonedDateTime.now(), SimpleFrequency.ANNUAL,
    DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), StubType.SHORT_START, 3,
    "US Treasury", Currency.USD, "Senior", "No Restructuring");
  private CDSApproxISDAPresentValueFunction testItem;

  @BeforeClass
  public static void initBeforeClass()
  {
    target = new GovernmentBondSecurity("US TREASURY N/B", "Government", "US", "Treasury", Currency.USD,
      YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(ZonedDateTime.of(2011, 2, 1, 12, 0, 0, 0, TimeZone.UTC)), "", 200,
      SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME), DayCountFactory.INSTANCE.getDayCount("Actual/Actual"),
      ZonedDateTime.of(2011, 2, 1, 12, 0, 0, 0, TimeZone.UTC), ZonedDateTime.of(2011, 2, 1, 12, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2011, 2, 1, 12, 0, 0, 0, TimeZone.UTC), 100d, 100000000, 5000, 1000, 100, 100);
    target.setExternalIdBundle(ExternalIdBundle.of(underlying));
    securitySource = new MockSecuritySource();
    securitySource.addSecurity(target);
    functionCompilationContext = new FunctionCompilationContext();
    functionCompilationContext.setFunctionInitId(123);
    functionCompilationContext.setSecuritySource(securitySource);
    final MapComputationTargetResolver targetResolver = new MapComputationTargetResolver();
    functionCompilationContext.setComputationTargetResolver(targetResolver);
  }

  @BeforeMethod
  public void beforeEachMethod()
  {
    testItem = new CDSApproxISDAPresentValueFunction();
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
    Set<ValueRequirement> result = testItem.getRequirements(functionCompilationContext, new ComputationTarget(CDS_SECURITY), null);
    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 3);

    TreeSet<String> r = new TreeSet<String>();
    for (ValueRequirement valueRequirement : result)
    {
      r.add(valueRequirement.toString());
    }

    Assert
      .assertEquals(
        r.toString(),
        "[ValueReq[YieldCurve, CTSpec[PRIMITIVE, CurrencyISO~GBP], {Curve=[CDS_US TREASURY N/B]}], ValueReq[YieldCurve, CTSpec[PRIMITIVE, CurrencyISO~GBP], {CurveCalculationMethod=[ParRate],Curve=[SECONDARY],FundingCurve=[SECONDARY],ForwardCurve=[SECONDARY]}], ValueReq[YieldCurve, CTSpec[PRIMITIVE, CurrencyISO~USD], {CurveCalculationMethod=[ParRate],Curve=[SECONDARY],FundingCurve=[SECONDARY],ForwardCurve=[SECONDARY]}]]");
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
