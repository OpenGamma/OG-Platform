/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.conversion.CreditDefaultSwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Abstract class for specific CS01 functions to derive from.
 */
public class ISDARiskMetricsVanillaCDSFunction extends NonCompiledInvoker {

  private CreditDefaultSwapSecurityConverterDeprecated _converter;
  protected static final String[] s_requirements = {ValueRequirementNames.ACCRUED_DAYS, ValueRequirementNames.ACCRUED_PREMIUM, ValueRequirementNames.CLEAN_PRICE, ValueRequirementNames.PRINCIPAL };

  @Override
  public void init(final FunctionCompilationContext context) {
    // using hardcoded region and calendar for now
    final HolidaySource holidaySource = new WeekendHolidaySource(); //OpenGammaCompilationContext.getHolidaySource(context);
    @SuppressWarnings("synthetic-access")
    final RegionSource regionSource = new TestRegionSource(getTestRegion()); //OpenGammaCompilationContext.getRegionSource(context);
    _converter = new CreditDefaultSwapSecurityConverterDeprecated(holidaySource, regionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
      throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());

    final LegacyVanillaCDSSecurity security = (LegacyVanillaCDSSecurity) target.getSecurity();
    LegacyVanillaCreditDefaultSwapDefinition cds = _converter.visitLegacyVanillaCDSSecurity(security);
    final ValueProperties properties = Iterables.getFirst(desiredValues, null).getConstraints().copy().get(); // all share same properties

    final Object cleanPVObject = inputs.getValue(ValueRequirementNames.CLEAN_PRESENT_VALUE);
    final double cleanPresentValue = ((Double) cleanPVObject).doubleValue();
    final Object dirtyPVObject = inputs.getValue(ValueRequirementNames.DIRTY_PRESENT_VALUE);
    final double dirtyPresentValue = ((Double) dirtyPVObject).doubleValue();
    final Object upfrontObject = inputs.getValue(ValueRequirementNames.UPFRONT_AMOUNT);
    final double upfrontAmount = ((Double) upfrontObject).doubleValue();

    cds = IMMDateGenerator.cdsModifiedForIMM(now, cds);

    final double accruedPremium = cleanPresentValue - dirtyPresentValue;
    final double accruedDays = Math.abs((accruedPremium * 360 / cds.getParSpread()) * (10000 / cds.getNotional()));
    final double cleanPrice = 100.0 * ((cds.getNotional() - dirtyPresentValue - accruedPremium) / cds.getNotional());
    final double principal = accruedPremium + upfrontAmount;

    final ComputedValue accruedDaysSpec = new ComputedValue(new ValueSpecification(ValueRequirementNames.ACCRUED_DAYS, target.toSpecification(), properties), accruedDays);
    final ComputedValue accruedPremiumSpec = new ComputedValue(new ValueSpecification(ValueRequirementNames.ACCRUED_PREMIUM, target.toSpecification(), properties), accruedPremium);
    final ComputedValue cleanPriceSpec = new ComputedValue(new ValueSpecification(ValueRequirementNames.CLEAN_PRICE, target.toSpecification(), properties), cleanPrice);
    final ComputedValue principalSpec = new ComputedValue(new ValueSpecification(ValueRequirementNames.PRINCIPAL, target.toSpecification(), properties), principal);

    return Sets.newHashSet(accruedDaysSpec, accruedPremiumSpec, cleanPriceSpec, principalSpec);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
        .withAny(ISDAFunctionConstants.ISDA_CURVE_DATE)
        .withAny(ISDAFunctionConstants.ISDA_IMPLEMENTATION)
        .withAny(ISDAFunctionConstants.CDS_QUOTE_CONVENTION)
        .withAny(ISDAFunctionConstants.ISDA_BUCKET_TENORS)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .get();
    final Set<ValueSpecification> specs = new HashSet<>();
    for (final String name : s_requirements) {
      specs.add(new ValueSpecification(name, target.toSpecification(), properties));
    }
    return specs;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final LegacyVanillaCDSSecurity cds = (LegacyVanillaCDSSecurity) target.getSecurity();

    final String isdaOffset = desiredValue.getConstraint(ISDAFunctionConstants.ISDA_CURVE_OFFSET);
    if (isdaOffset == null) {
      return null;
    }

    final String isdaCurveDate = desiredValue.getConstraint(ISDAFunctionConstants.ISDA_CURVE_DATE);
    if (isdaCurveDate == null) {
      return null;
    }

    final String isdaCurveMethod = desiredValue.getConstraint(ISDAFunctionConstants.ISDA_IMPLEMENTATION);
    if (isdaCurveMethod == null) {
      return null;
    }

    final String quoteConvention = desiredValue.getConstraint(ISDAFunctionConstants.CDS_QUOTE_CONVENTION);
    if (quoteConvention == null) {
      return null;
    }

    final String bucketTenors = desiredValue.getConstraint(ISDAFunctionConstants.ISDA_BUCKET_TENORS);
    if (bucketTenors == null) {
      return null;
    }

    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, isdaOffset)
        .with(ISDAFunctionConstants.ISDA_CURVE_DATE, isdaCurveDate)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, isdaCurveMethod)
        .with(ISDAFunctionConstants.CDS_QUOTE_CONVENTION, quoteConvention)
        .with(ISDAFunctionConstants.ISDA_BUCKET_TENORS, bucketTenors)
        .get();
    final ValueRequirement cleanPVRequirment = new ValueRequirement(ValueRequirementNames.CLEAN_PRESENT_VALUE, target.toSpecification(), properties);
    final ValueRequirement dirtyPVRequirment = new ValueRequirement(ValueRequirementNames.DIRTY_PRESENT_VALUE, target.toSpecification(), properties);
    final ValueRequirement upfrontRequirment = new ValueRequirement(ValueRequirementNames.UPFRONT_AMOUNT, target.toSpecification(), properties);

    return Sets.newHashSet(cleanPVRequirment, dirtyPVRequirment, upfrontRequirment);
  }

  protected static ManageableRegion getTestRegion() {
    final ManageableRegion region = new ManageableRegion();
    region.setUniqueId(UniqueId.parse("Dummy~region"));
    region.setName("United States");
    region.setCurrency(Currency.USD);
    region.setCountry(Country.US);
    region.setTimeZone(ZoneId.of("America/New_York"));
    region.setExternalIdBundle(ExternalIdBundle.of(ExternalId.parse("dummy~region")));
    return region;
  }

  class TestRegionSource extends AbstractSourceWithExternalBundle<Region> implements RegionSource {

    private final AtomicLong _count = new AtomicLong(0);
    private final Region _testRegion;

    private TestRegionSource(final Region testRegion) {
      _testRegion = testRegion;
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
