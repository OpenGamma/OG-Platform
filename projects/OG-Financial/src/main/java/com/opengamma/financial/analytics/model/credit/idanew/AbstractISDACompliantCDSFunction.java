/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.idanew;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.WeekendHolidaySource;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Abstract class for cds functions.
 */
public abstract class AbstractISDACompliantCDSFunction extends NonCompiledInvoker {

  private final String _valueRequirement;
  private static double s_tenminus4 = 1e-4; // fractional 1 BPS
  private HolidaySource _holidaySource;
  private RegionSource _regionSource;

  public AbstractISDACompliantCDSFunction(final String valueRequirement) {
    _valueRequirement = valueRequirement;
  }

  protected static double getTenminus4() {
    return s_tenminus4;
  }

  protected String getValueRequirement() {
    return _valueRequirement;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    // using hardcoded region and calendar for now
    _holidaySource = new WeekendHolidaySource(); //OpenGammaCompilationContext.getHolidaySource(context);
    _regionSource = new TestRegionSource(getTestRegion()); //OpenGammaCompilationContext.getRegionSource(context);
    //_converter = new CreditDefaultSwapSecurityConverterDeprecated(holidaySource, regionSource);
  }

  protected abstract Object compute(final double parSpread, final double notional, final CDSAnalytic analytic);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
    throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());

    final LegacyVanillaCDSSecurity security = (LegacyVanillaCDSSecurity) target.getSecurity();
    //LegacyVanillaCreditDefaultSwapDefinition cds = _converter.visitLegacyVanillaCDSSecurity(security);
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);

    final CDSAnalyticVisitor converter = new CDSAnalyticVisitor(now.toLocalDate(), _holidaySource, _regionSource);
    final CDSAnalytic analytic = security.accept(converter);

    final Object result = compute(security.getParSpread(), security.getNotional().getAmount(), analytic);

    final ValueProperties properties = desiredValue.getConstraints().copy().get();
    final ValueSpecification spec = new ValueSpecification(getValueRequirement(), target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, result));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .get();
    return Collections.singleton(new ValueSpecification(getValueRequirement(), target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.EMPTY_SET;
  }

  public static CreditCurveIdentifier getSpreadCurveIdentifier(final CreditDefaultSwapSecurity cds) {
    return getCreditCurveIdentifier(cds, "");
  }

  public static CreditCurveIdentifier getISDACurveIdentifier(final CreditDefaultSwapSecurity cds) {
    return getCreditCurveIdentifier(cds, "ISDA_");
  }

  /**
   * Get the CreditCurveIdentifier with name appended
   *
   * @param security
   */
  private static CreditCurveIdentifier getCreditCurveIdentifier(final CreditDefaultSwapSecurity security, final String name) {
    final CreditCurveIdentifier curveIdentifier = CreditCurveIdentifier.of(name + security.getReferenceEntity().getValue(), security.getNotional().getCurrency(),
        security.getDebtSeniority().toString(), security.getRestructuringClause().toString());
    return curveIdentifier;
  }

  public static ManageableRegion getTestRegion() {
    final ManageableRegion region = new ManageableRegion();
    region.setUniqueId(UniqueId.parse("Dummy~region"));
    region.setName("United States");
    region.setCurrency(Currency.USD);
    region.setCountry(Country.US);
    region.setTimeZone(ZoneId.of("America/New_York"));
    region.setExternalIdBundle(ExternalIdBundle.of(ExternalId.parse("dummy~region")));
    return region;
  }

  /** Test region */
  public final class TestRegionSource extends AbstractSourceWithExternalBundle<Region> implements RegionSource {

    private final AtomicLong _count = new AtomicLong(0);
    private final Region _testRegion;

    private TestRegionSource(final Region testRegion) {
      _testRegion = testRegion;
    }

    private TestRegionSource() {
      _testRegion = getTestRegion();
    }

    @Override
    public Collection<Region> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
      _count.getAndIncrement();
      Collection<Region> result = Collections.emptyList();
      if (_testRegion.getExternalIdBundle().equals(bundle) && versionCorrection.equals(VersionCorrection.LATEST)) {
        result = Collections.singleton((Region) getTestRegion());
      }
      return result;
    }

    @Override
    public Region get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      _count.getAndIncrement();
      Region result = null;
      if (_testRegion.getUniqueId().getObjectId().equals(objectId) && versionCorrection.equals(VersionCorrection.LATEST)) {
        result = _testRegion;
      }
      return result;
    }

    @Override
    public Region get(final UniqueId uniqueId) {
      _count.getAndIncrement();
      Region result = null;
      if (_testRegion.getUniqueId().equals(uniqueId)) {
        result = _testRegion;
      }
      return result;
    }

    @Override
    public Region getHighestLevelRegion(final ExternalIdBundle bundle) {
      _count.getAndIncrement();
      Region result = null;
      if (_testRegion.getExternalIdBundle().equals(bundle)) {
        result = _testRegion;
      }
      return result;
    }

    @Override
    public Region getHighestLevelRegion(final ExternalId externalId) {
      _count.getAndIncrement();
      Region result = null;
      if (_testRegion.getExternalIdBundle().contains(externalId)) {
        result = _testRegion;
      }
      return result;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public AtomicLong getCount() {
      return _count;
    }

    @Override
    public ChangeManager changeManager() {
      return DummyChangeManager.INSTANCE;
    }

  }
}
