package com.opengamma.financial.analytics.model.cds;

import java.util.Set;
import java.util.TreeSet;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.security.impl.test.MockSecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ISDAApproxCDSPriceFlatSpreadFunctionTest {

  private static MockSecuritySource securitySource;
  private static FunctionCompilationContext functionCompilationContext;
  private static final CDSSecurity CDS_SECURITY = new CDSSecurity(1.0, 0.6, 0.0025, Currency.GBP, zdt(2020, 12, 20, 0, 0, 0, 0, ZoneOffset.UTC), ZonedDateTime.now(), SimpleFrequency.ANNUAL,
    DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, StubType.SHORT_START, 3,
    "US Treasury", Currency.USD, "Senior", "No Restructuring");
  private ISDAApproxCDSPriceFlatSpreadFunction testItem;

  @BeforeClass
  public static void initBeforeClass() {
    securitySource = new MockSecuritySource();
    functionCompilationContext = new FunctionCompilationContext();
    functionCompilationContext.setFunctionInitId(123);
    functionCompilationContext.setSecuritySource(securitySource);
    final MapComputationTargetResolver targetResolver = new MapComputationTargetResolver();
    functionCompilationContext.setRawComputationTargetResolver(targetResolver);
    CDS_SECURITY.setUniqueId(UniqueId.of("dummy_scheme", "dummy_value"));
  }

  @BeforeMethod
  public void beforeEachMethod() {
    testItem = new ISDAApproxCDSPriceFlatSpreadFunction();
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
    
    Set<ValueRequirement> result = testItem.getRequirements(functionCompilationContext, new ComputationTarget(ComputationTargetType.SECURITY, CDS_SECURITY), requirement);

    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 2);

    TreeSet<String> r = new TreeSet<String>();
    for (ValueRequirement valueRequirement : result) {
      r.add(valueRequirement.toString());
    }

    Assert
      .assertEquals(
        r.toString(),
            "[ValueReq[SpotRate, CTSpec[SECURITY, dummy_scheme~dummy_value], EMPTY], ValueReq[YieldCurve, CTSpec[CURRENCY, CurrencyISO~GBP], {CalculationMethod=[ISDA]}]]");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getResults1() {
    testItem.getResults(null, new ComputationTarget(ComputationTargetType.SECURITY, CDS_SECURITY));
  }

  @Test
  public void getResults2() {
    //TODO
  }

  @Test
  public void getTargetType() {
    Assert.assertEquals(testItem.getTargetType(), FinancialSecurityTypes.CDS_SECURITY);
  }

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(int y, int m, int d, int hr, int min, int sec, int nanos, ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}
