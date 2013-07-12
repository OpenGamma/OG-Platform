/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.idanew;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.creditdefaultswap.StandardCDSQuotingConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.FastCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.math.curve.NodalTenorDoubleCurve;
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
import com.opengamma.financial.analytics.model.credit.IMMDateGenerator;
import com.opengamma.financial.analytics.model.credit.SpreadCurveFunctions;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Abstract class for cds functions that require ISDA and spread curves.
 */
public abstract class ISDACompliantCDSFunction extends NonCompiledInvoker {

  private CreditDefaultSwapSecurityConverterDeprecated _converter;
  private static final Logger s_logger = LoggerFactory.getLogger(ISDACompliantCDSFunction.class);
  protected final String _valueRequirement;
  private static Calendar _cal = new MondayToFridayCalendar("weekday");
  protected static double s_tenminus4 = 1e-4; // fractional 1 BPS

  public ISDACompliantCDSFunction(final String valueRequirement) {
    _valueRequirement = valueRequirement;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    // using hardcoded region and calendar for now
    final HolidaySource holidaySource = new WeekendHolidaySource(); //OpenGammaCompilationContext.getHolidaySource(context);
    @SuppressWarnings("synthetic-access")
    final RegionSource regionSource = new TestRegionSource(getTestRegion()); //OpenGammaCompilationContext.getRegionSource(context);
    _converter = new CreditDefaultSwapSecurityConverterDeprecated(holidaySource, regionSource);
  }

  protected abstract Object compute(final ZonedDateTime valuationDate, final LegacyVanillaCreditDefaultSwapDefinition cds, final ISDACompliantCreditCurve creditCurve,
                                    final ISDACompliantYieldCurve yieldCurve, final CDSAnalytic analytic, final CDSAnalytic[] curveAnalytics, final double[] spreads);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
      throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());

    final LegacyVanillaCDSSecurity security = (LegacyVanillaCDSSecurity) target.getSecurity();
    LegacyVanillaCreditDefaultSwapDefinition cds = _converter.visitLegacyVanillaCDSSecurity(security);
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);

    final String quoteConventionString = desiredValue.getConstraint(ISDAFunctionConstants.CDS_QUOTE_CONVENTION);
    final StandardCDSQuotingConvention quoteConvention = StandardCDSQuotingConvention.parse(quoteConventionString);

    // get the isda curve
    final Object isdaObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (isdaObject == null) {
      throw new OpenGammaRuntimeException("Couldn't get isda curve");
    }
    final ISDACompliantYieldCurve yieldCurve = (ISDACompliantYieldCurve) isdaObject;

    //TODO: The credit curve logic needs to be improved possibly moving to a new function - though it needs to be targetted at a cds (needed for some cases)
    // also the cds analytics need to be passed down to the calculation - for now do all here

    final ISDACompliantCreditCurve creditCurve = (ISDACompliantCreditCurve) inputs.getValue(ValueRequirementNames.HAZARD_RATE_CURVE);
    if (creditCurve == null) {
      throw new OpenGammaRuntimeException("Unable to get credit curve");
    }

    NodalTenorDoubleCurve spreadObject = (NodalTenorDoubleCurve) inputs.getValue(ValueRequirementNames.BUCKETED_SPREADS);
    if (spreadObject == null) {
      throw new OpenGammaRuntimeException("Unable to get spreads");
    }
    final double[] spreads = ArrayUtils.toPrimitive(spreadObject.getYData());

    final ZonedDateTime[] bucketDates = SpreadCurveFunctions.getIMMDates(now, desiredValue.getConstraint(ISDAFunctionConstants.ISDA_BUCKET_TENORS));

    // CDS analytics for credit curve  - regenerated (possible performance improvement if earlier result obtained)
    final LegacyVanillaCreditDefaultSwapDefinition curveCDS = cds.withStartDate(now);
    final CDSAnalytic[] creditAnalytics = new CDSAnalytic[spreads.length];
    for (int i = 0; i < creditAnalytics.length; i++) {
      creditAnalytics[i] = CDSAnalyticConverter.create(curveCDS, now.toLocalDate(), bucketDates[i].toLocalDate());
    }

    final CDSAnalytic analytic = CDSAnalyticConverter.create(cds, now.toLocalDate());

    final Object result = compute(now, cds, creditCurve, yieldCurve, analytic, creditAnalytics, spreads);

    final ValueProperties properties = desiredValue.getConstraints().copy().get();
    final ValueSpecification spec = new ValueSpecification(_valueRequirement, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, result));
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
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
        .withAny(ISDAFunctionConstants.CDS_QUOTE_CONVENTION)
        .withAny(ISDAFunctionConstants.ISDA_BUCKET_TENORS)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .get();
    return Collections.singleton(new ValueSpecification(_valueRequirement, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final LegacyVanillaCDSSecurity cds = (LegacyVanillaCDSSecurity) target.getSecurity();
    final Currency ccy = cds.getNotional().getCurrency();
    final CreditCurveIdentifier isdaIdentifier = getISDACurveIdentifier(cds);
    final CreditCurveIdentifier spreadIdentifier = getSpreadCurveIdentifier(cds);

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

    // isda curve
    final ValueProperties isdaProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, isdaIdentifier.toString())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, isdaOffset)
        .with(ISDAFunctionConstants.ISDA_CURVE_DATE, isdaCurveDate)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, isdaCurveMethod)
        .get();
    final ValueRequirement isdaRequirment = new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.CURRENCY, ccy.getUniqueId(), isdaProperties);

    final String quoteConvention = desiredValue.getConstraint(ISDAFunctionConstants.CDS_QUOTE_CONVENTION);
    if (quoteConvention == null) {
      return null;
    }

    final String bucketTenors = desiredValue.getConstraint(ISDAFunctionConstants.ISDA_BUCKET_TENORS);
    if (bucketTenors == null) {
      return null;
    }

    // hazard curve
    final ValueProperties creditCurveProperties = ValueProperties.builder()
        .with(ISDAFunctionConstants.CDS_QUOTE_CONVENTION, quoteConvention)
        //.with(ValuePropertyNames.CURVE, isdaIdentifier.toString())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, isdaOffset)
        .with(ISDAFunctionConstants.ISDA_CURVE_DATE, isdaCurveDate)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, isdaCurveMethod)
        .with(ISDAFunctionConstants.ISDA_BUCKET_TENORS, bucketTenors)
        .get();
    final ValueRequirement creditRequirment = new ValueRequirement(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), creditCurveProperties);

    //market  spreads
    final ValueProperties spreadProperties = ValueProperties.builder()
        .with(ISDAFunctionConstants.CDS_QUOTE_CONVENTION, quoteConvention)
            //.with(ValuePropertyNames.CURVE, isdaIdentifier.toString())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, isdaOffset)
        .with(ISDAFunctionConstants.ISDA_CURVE_DATE, isdaCurveDate)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, isdaCurveMethod)
        .with(ISDAFunctionConstants.ISDA_BUCKET_TENORS, bucketTenors)
        .get();
    final ValueRequirement spreadRequirment = new ValueRequirement(ValueRequirementNames.BUCKETED_SPREADS, target.toSpecification(), spreadProperties);

    return Sets.newHashSet(isdaRequirment, spreadRequirment, creditRequirment);
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
}
