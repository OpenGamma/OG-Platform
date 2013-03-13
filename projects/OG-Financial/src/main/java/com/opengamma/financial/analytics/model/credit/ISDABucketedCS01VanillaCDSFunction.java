/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.CS01CreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.math.curve.NodalTenorDoubleCurve;
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
import com.opengamma.financial.analytics.conversion.CreditDefaultSwapSecurityConverter;
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
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

//TODO: This is a temporary version which looks for data to assemble curves and spreads in a very particular fashion
// this will be made more generic i.e. pulling isda curves via yield curve data bundle asap.

/**
 * Function to calculate the bucketed CS01 for a given CDS instrument.
 */
public class ISDABucketedCS01VanillaCDSFunction extends AbstractFunction.NonCompiledInvoker {

  private CreditDefaultSwapSecurityConverter _converter;
  private static final CS01CreditDefaultSwap CALCULATOR = new CS01CreditDefaultSwap(); // the calculator
  private static final Logger s_logger = LoggerFactory.getLogger(ISDABucketedCS01VanillaCDSFunction.class);

  // should this be pulled in from somewhere else?
  private static final Collection<ZonedDateTime> BUCKET_DATES = new ArrayList<>();

  //FIXME: Derive these instead of hardcoding
  static {
    BUCKET_DATES.add(DateUtils.getUTCDate(2013, 9, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2014, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2015, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2016, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2017, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2018, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2019, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2020, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2021, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2022, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2023, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2028, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2033, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2043, 3, 20));
  }

  private final ZonedDateTime[] BUCKET_DATES_ARRAY = BUCKET_DATES.toArray(new ZonedDateTime[BUCKET_DATES.size()]);

  public ISDABucketedCS01VanillaCDSFunction() {
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    // using hardcoded region and calendar for now
    final HolidaySource holidaySource = new WeekendHolidaySource(); //OpenGammaCompilationContext.getHolidaySource(context);
    @SuppressWarnings("synthetic-access")
    final RegionSource regionSource = new TestRegionSource(getTestRegion()); //OpenGammaCompilationContext.getRegionSource(context);
    _converter = new CreditDefaultSwapSecurityConverter(holidaySource, regionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());

    final LegacyVanillaCDSSecurity security = (LegacyVanillaCDSSecurity) target.getSecurity();
    final LegacyVanillaCreditDefaultSwapDefinition cds = _converter.visitLegacyVanillaCDSSecurity(security);
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);

    // get the market spread bump to apply to all tenors
    final Object spreadObject = inputs.getValue(ValueRequirementNames.BUCKETED_SPREADS);
    if (spreadObject == null) {
      throw new OpenGammaRuntimeException("Couldn't get input spreads");
    } else if (!(spreadObject instanceof NodalTenorDoubleCurve)) {
      throw new OpenGammaRuntimeException("Unexpected spread curve object type " + spreadObject.getClass().getName());
    }
    final NodalTenorDoubleCurve spreadCurve = (NodalTenorDoubleCurve) spreadObject;
    if (spreadCurve.size() == 0) {
      throw new OpenGammaRuntimeException("No bucket spreads");
    }
    // find index of bucket this cds maturity is in - should really implement a custom comparator and do a binary search
    Double spreadRate = Double.valueOf(0.0);
    for (final Tenor tenor : spreadCurve.getXData()) {
      final ZonedDateTime bucketDate = cds.getStartDate().plus(tenor.getPeriod());
      if (!bucketDate.isAfter(security.getMaturityDate())) {
        spreadRate = spreadCurve.getYValue(tenor);
      } else {
        break; // stop when we find desired bucket
      }
    }

    // set all spreads to desired spread
    final double[] spreads = new double[BUCKET_DATES.size()];
    Arrays.fill(spreads, spreadRate.doubleValue());

    // get the isda curve
    final Object isdaObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (isdaObject == null) {
      throw new OpenGammaRuntimeException("Couldn't get isda curve");
    }
    final ISDADateCurve isdaCurve = (ISDADateCurve) isdaObject;

    // Compute the bucketed CS01 for this CDS
    double[] bucketedCS01;
    try {
      bucketedCS01 = CALCULATOR.getCS01BucketedCreditDefaultSwap(now, cds, isdaCurve, BUCKET_DATES_ARRAY, spreads, 1.0, SpreadBumpType.ADDITIVE_BUCKETED,
          PriceType.CLEAN); // take values from requirements
    } catch (Exception ex) {
      s_logger.error("Exception thrown during calculation: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
      throw ex;
    }

    final ValueProperties properties = desiredValue.getConstraints().copy().get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BUCKETED_CS01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, bucketedCS01));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        //.withAny(ValuePropertyNames.CURVE)
        .withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
        .withAny(ISDAFunctionConstants.ISDA_CURVE_DATE)
        .withAny(ISDAFunctionConstants.ISDA_IMPLEMENTATION)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.BUCKETED_CS01, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context,
                                               final ComputationTarget target,
                                               final ValueRequirement desiredValue) {
    final LegacyVanillaCDSSecurity cds = (LegacyVanillaCDSSecurity) target.getSecurity();
    final Currency ccy = cds.getNotional().getCurrency();
    final CreditCurveIdentifier curveIdentifier = getCreditCurveIdentifier(cds);

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
        .with(ValuePropertyNames.CURVE, "ISDA_" + curveIdentifier.toString())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, isdaOffset)
        .with(ISDAFunctionConstants.ISDA_CURVE_DATE, isdaCurveDate)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, isdaCurveMethod)
        .get();
    final ValueRequirement isdaRequirment = new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.CURRENCY, ccy.getUniqueId(), isdaProperties);

    // market spreads
    final ValueRequirement spreadRequirment = new ValueRequirement(ValueRequirementNames.BUCKETED_SPREADS, ComputationTargetType.PRIMITIVE, curveIdentifier.getUniqueId());

    return Sets.newHashSet(isdaRequirment, spreadRequirment);
  }


  /**
   * Get the CreditCurveIdentifier
   *
   * @param security
   */
  private static CreditCurveIdentifier getCreditCurveIdentifier(final LegacyVanillaCDSSecurity security) {
    final CreditCurveIdentifier curveIdentifier = CreditCurveIdentifier.of(security.getReferenceEntity(), security.getNotional().getCurrency(),
        security.getDebtSeniority().toString(), security.getRestructuringClause().toString());
    return curveIdentifier;
  }

  private class TestRegionSource implements RegionSource {

    private final AtomicLong _count = new AtomicLong(0);
    private final Region _testRegion;

    private TestRegionSource(Region testRegion) {
      _testRegion = testRegion;
    }

    @Override
    public Collection<? extends Region> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
      _count.getAndIncrement();
      Collection<? extends Region> result = Collections.emptyList();
      if (_testRegion.getExternalIdBundle().equals(bundle) && versionCorrection.equals(VersionCorrection.LATEST)) {
        result = Collections.singleton(getTestRegion());
      }
      return result;
    }

    @Override
    public Region get(ObjectId objectId, VersionCorrection versionCorrection) {
      _count.getAndIncrement();
      Region result = null;
      if (_testRegion.getUniqueId().getObjectId().equals(objectId) && versionCorrection.equals(VersionCorrection.LATEST)) {
        result = _testRegion;
      }
      return result;
    }

    @Override
    public Region get(UniqueId uniqueId) {
      _count.getAndIncrement();
      Region result = null;
      if (_testRegion.getUniqueId().equals(uniqueId)) {
        result = _testRegion;
      }
      return result;
    }

    @Override
    public Region getHighestLevelRegion(ExternalIdBundle bundle) {
      _count.getAndIncrement();
      Region result = null;
      if (_testRegion.getExternalIdBundle().equals(bundle)) {
        result = _testRegion;
      }
      return result;
    }

    @Override
    public Region getHighestLevelRegion(ExternalId externalId) {
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
    public Map<UniqueId, Region> get(Collection<UniqueId> uniqueIds) {
      Map<UniqueId, Region> result = Maps.newHashMap();
      for (UniqueId uniqueId : uniqueIds) {
        try {
          Region security = get(uniqueId);
          result.put(uniqueId, security);
        } catch (DataNotFoundException ex) {
          // do nothing
        }
      }
      return result;
    }
  }

  private static ManageableRegion getTestRegion() {
    ManageableRegion region = new ManageableRegion();
    region.setUniqueId(UniqueId.parse("Dummy~region"));
    region.setName("United States");
    region.setCurrency(Currency.USD);
    region.setCountry(Country.US);
    region.setTimeZone(ZoneId.of("America/New_York"));
    region.setExternalIdBundle(ExternalIdBundle.of(ExternalId.parse("dummy~region")));
    return region;
  }

}
