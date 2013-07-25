/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.idanew;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ArrayUtils;
import org.threeten.bp.Clock;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.creditdefaultswap.StandardCDSQuotingConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.FastCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.math.curve.NodalTenorDoubleCurve;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.WeekendHolidaySource;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
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
import com.opengamma.financial.analytics.model.credit.SpreadCurveFunctions;
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
import com.opengamma.util.time.Tenor;

/**
 * Function to return spreads modified for a given security
 */
public class ISDACompliantCreditCurveFunction extends AbstractFunction.NonCompiledInvoker {

  private CreditDefaultSwapSecurityConverterDeprecated _converter;
  private HolidaySource _holidaySource;
  private RegionSource _regionSource;

  @Override
  public void init(final FunctionCompilationContext context) {
      // using hardcoded region and calendar for now
      _holidaySource = new WeekendHolidaySource(); //OpenGammaCompilationContext.getHolidaySource(context);
      _regionSource = new TestRegionSource(); //OpenGammaCompilationContext.getRegionSource(context);
      _converter = new CreditDefaultSwapSecurityConverterDeprecated(_holidaySource, _regionSource);
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      ValueRequirement creditCurveReq = null;
      ValueRequirement spreadsRequriement = null;
      for (final ValueRequirement entry : desiredValues) {
        if (entry.getValueName().equals(ValueRequirementNames.HAZARD_RATE_CURVE)) {
          creditCurveReq = entry;
        } else if (entry.getValueName().equals(ValueRequirementNames.BUCKETED_SPREADS)) {
          spreadsRequriement = entry;
        }
      }
      final Clock snapshotClock = executionContext.getValuationClock();
      final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
      final LegacyVanillaCDSSecurity security = (LegacyVanillaCDSSecurity) target.getSecurity();
      LegacyVanillaCreditDefaultSwapDefinition cds = _converter.visitLegacyVanillaCDSSecurity(security);
      final StandardCDSQuotingConvention quoteConvention = StandardCDSQuotingConvention.parse(spreadsRequriement.getConstraint(ISDAFunctionConstants.CDS_QUOTE_CONVENTION));
      final NodalTenorDoubleCurve spreadCurve = (NodalTenorDoubleCurve) inputs.getValue(ValueRequirementNames.BUCKETED_SPREADS);
      if (spreadCurve == null) {
        throw new OpenGammaRuntimeException("Bucketed spreads not available for " +  AbstractISDACompliantWithCreditCurveCDSFunction.getSpreadCurveIdentifier(
            security));
      }

      // get the isda curve
      final ISDACompliantYieldCurve yieldCurve = (ISDACompliantYieldCurve) inputs.getValue(ValueRequirementNames.YIELD_CURVE);
      if (yieldCurve == null) {
        throw new OpenGammaRuntimeException("Couldn't get isda curve");
      }

      // modify curve as needed
      final ZonedDateTime[] bucketDates = SpreadCurveFunctions.getIMMDates(now, spreadsRequriement.getConstraint(ISDAFunctionConstants.ISDA_BUCKET_TENORS));
      //double[] spreads = SpreadCurveFunctions.getSpreadCurve(cds, spreadCurve, bucketDates, quoteConvention, now, yieldCurve, cds.getStartDate());
      double[] spreads = SpreadCurveFunctions.getSpreadCurveNew(spreadCurve, bucketDates, security.getStartDate(), quoteConvention);
      Tenor[] tenors = SpreadCurveFunctions.getBuckets(spreadsRequriement.getConstraint(ISDAFunctionConstants.ISDA_BUCKET_TENORS));
      final NodalTenorDoubleCurve modifiedSpreadCurve = new NodalTenorDoubleCurve(tenors, ArrayUtils.toObject(spreads), true);
      final CDSQuoteConvention[] quotes = SpreadCurveFunctions.getQuotes(security.getMaturityDate(), bucketDates, spreads, security.getParSpread(), quoteConvention);

      // CDS analytics for credit curve
      //final LegacyVanillaCreditDefaultSwapDefinition curveCDS = cds.withStartDate(now);
      final CDSAnalytic[] creditAnalytics = new CDSAnalytic[spreads.length];
      for (int i = 0; i < creditAnalytics.length; i++) {
        final CDSAnalyticVisitor visitor = new CDSAnalyticVisitor(now.toLocalDate(), _holidaySource, _regionSource, security.getStartDate().toLocalDate(), bucketDates[i].toLocalDate());
        creditAnalytics[i] = security.accept(visitor);
        //creditAnalytics[i] = CDSAnalyticConverter.create(curveCDS, now.toLocalDate(), bucketDates[i].toLocalDate());
      }

      final FastCreditCurveBuilder creditCurveBuilder = new FastCreditCurveBuilder();
      final ISDACompliantCreditCurve creditCurve = creditCurveBuilder.calibrateCreditCurve(creditAnalytics, quotes, yieldCurve);
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), creditCurveReq.getConstraints());

      // spreads
      final ValueSpecification spreadSpec = new ValueSpecification(ValueRequirementNames.BUCKETED_SPREADS, target.toSpecification(), spreadsRequriement.getConstraints());

      return Sets.newHashSet(new ComputedValue(spec, creditCurve),
                             new ComputedValue(spreadSpec, modifiedSpreadCurve));
    }

    @Override
    public ComputationTargetType getTargetType() {
      return FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      @SuppressWarnings("synthetic-access")
      final ValueProperties properties = createValueProperties()
          .withAny(ISDAFunctionConstants.CDS_QUOTE_CONVENTION)
          .withAny(ISDAFunctionConstants.ISDA_BUCKET_TENORS)
          .withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
          .withAny(ISDAFunctionConstants.ISDA_CURVE_DATE)
          .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
          .get();
      final ValueSpecification creditCurveSpec = new ValueSpecification(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), properties);
      final ValueProperties spreadProperties = createValueProperties()
          .withAny(ISDAFunctionConstants.CDS_QUOTE_CONVENTION)
          .withAny(ISDAFunctionConstants.ISDA_BUCKET_TENORS)
          .withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
          .withAny(ISDAFunctionConstants.ISDA_CURVE_DATE)
          .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
          .get();
      final ValueSpecification spreadSpec = new ValueSpecification(ValueRequirementNames.BUCKETED_SPREADS, target.toSpecification(), spreadProperties);
      return Sets.newHashSet(creditCurveSpec, spreadSpec);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final CreditDefaultSwapSecurity cds = (CreditDefaultSwapSecurity) target.getSecurity();
      final CreditCurveIdentifier spreadIdentifier = AbstractISDACompliantWithCreditCurveCDSFunction.getSpreadCurveIdentifier(
          cds);

      final Currency ccy = cds.getNotional().getCurrency();
      final CreditCurveIdentifier isdaIdentifier = AbstractISDACompliantWithCreditCurveCDSFunction.getISDACurveIdentifier(
          cds);

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

      if (desiredValue.getConstraint(ISDAFunctionConstants.CDS_QUOTE_CONVENTION) == null) {
        return null;
      }

      // isda curve
      final ValueProperties isdaProperties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, isdaIdentifier.toString())
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
          .with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, isdaOffset)
          .with(ISDAFunctionConstants.ISDA_CURVE_DATE, isdaCurveDate)
          .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, isdaCurveMethod)
          .get();
      final ValueRequirement isdaRequirment = new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.CURRENCY, ccy.getUniqueId(), isdaProperties);

      final ValueRequirement spreadRequirment = new ValueRequirement(ValueRequirementNames.BUCKETED_SPREADS, ComputationTargetType.PRIMITIVE, spreadIdentifier.getUniqueId());
      return Sets.newHashSet(spreadRequirment, isdaRequirment);
    }

    //@Override
    //public boolean canHandleMissingRequirements() {
    //  // time series may not be available
    //  return true;
    //}
    //
    //@Override
    //public boolean canHandleMissingInputs() {
    //  return true;
    //}


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

public class TestRegionSource extends AbstractSourceWithExternalBundle<Region> implements RegionSource {

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

};
