/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isdanew;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ArrayUtils;
import org.threeten.bp.Clock;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.StandardCDSQuotingConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FastCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ParSpread;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;
import com.opengamma.analytics.math.curve.NodalTenorDoubleCurve;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.WeekendHolidaySource;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.financial.analytics.model.credit.CreditSecurityToRecoveryRateVisitor;
import com.opengamma.financial.analytics.model.credit.IMMDateGenerator;
import com.opengamma.financial.analytics.model.credit.SpreadCurveFunctions;
import com.opengamma.financial.credit.CdsRecoveryRateIdentifier;
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

/** Function to return spreads modified for a given security */
public class ISDACompliantCreditCurveFunction extends AbstractFunction.NonCompiledInvoker {

  /** String representation of fixed pillars used for non IMM */
  public static final String NON_IMM_PILLAR_TENORS = "P6M,P1Y,P2Y,P3Y,P4Y,P5Y,P7Y,P10Y";
  private static final FastCreditCurveBuilder CREDIT_CURVE_BUILDER = new FastCreditCurveBuilder();
  private static final MarketQuoteConverter POINTS_UP_FRONT_CONVERTER = new MarketQuoteConverter();
  private HolidaySource _holidaySource;
  private RegionSource _regionSource;

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
  private static CreditCurveIdentifier getCreditCurveIdentifier(final CreditDefaultSwapSecurity security,
                                                                final String name) {
    final CreditCurveIdentifier curveIdentifier = CreditCurveIdentifier.of(name + security.getReferenceEntity().getValue(),
                                                                           security.getNotional().getCurrency(),
                                                                           security.getDebtSeniority().toString(),
                                                                           security.getRestructuringClause().toString());
    return curveIdentifier;
  }

  public static QuotedSpread getQuotedSpread(CDSQuoteConvention quote,
                                             BuySellProtection buySellProtection,
                                             ISDACompliantYieldCurve yieldCurve,
                                             CDSAnalytic analytic,
                                             double premium) {
    double quotedSpread;
    if (quote instanceof PointsUpFront) {
      quotedSpread = POINTS_UP_FRONT_CONVERTER.pufToQuotedSpread(analytic,
                                                                 quote.getCoupon(),
                                                                 yieldCurve,
                                                                 ((PointsUpFront) quote).getPointsUpFront());
    } else if (quote instanceof QuotedSpread) {
      return (QuotedSpread) quote;
    } else if (quote instanceof ParSpread) {
      quotedSpread = POINTS_UP_FRONT_CONVERTER.parSpreadsToQuotedSpreads(new CDSAnalytic[]{analytic},
                                                                         premium * 1e-4,
                                                                         yieldCurve,
                                                                         new double[]{quote.getCoupon()})[0];
    } else {
      throw new OpenGammaRuntimeException("Unknown quote type " + quote);
    }
    // SELL protection reverses directions of legs
    quotedSpread = Double.valueOf(buySellProtection == BuySellProtection.SELL ? -quotedSpread : quotedSpread);
    return new QuotedSpread(quote.getCoupon(), quotedSpread);
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

  @Override
  public void init(final FunctionCompilationContext context) {
    // using hardcoded region and calendar for now
    _holidaySource = new WeekendHolidaySource(); //OpenGammaCompilationContext.getHolidaySource(context);
    _regionSource = new TestRegionSource(); //OpenGammaCompilationContext.getRegionSource(context);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext,
                                    final FunctionInputs inputs,
                                    final ComputationTarget target,
                                    final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    ValueRequirement requirement = desiredValues.iterator().next();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final LegacyVanillaCDSSecurity security = (LegacyVanillaCDSSecurity) target.getSecurity();

    final CdsRecoveryRateIdentifier recoveryRateIdentifier = security.accept(new CreditSecurityToRecoveryRateVisitor(
        executionContext.getSecuritySource()));
    Object recoveryRateObject = inputs.getValue(new ValueRequirement("PX_LAST",
                                                                     ComputationTargetType.PRIMITIVE,
                                                                     recoveryRateIdentifier.getExternalId()));
    if (recoveryRateObject == null) {
      throw new OpenGammaRuntimeException("Could not get recovery rate");
      //s_logger.warn("Could not get recovery rate, defaulting to 0.4: " + recoveryRateIdentifier);
      //recoveryRateObject = 0.4;
    }
    final double recoveryRate = (Double) recoveryRateObject;

    CreditDefaultSwapSecurityConverterDeprecated converter = new CreditDefaultSwapSecurityConverterDeprecated(
        _holidaySource,
        _regionSource,
        recoveryRate);
    LegacyVanillaCreditDefaultSwapDefinition cds = converter.visitLegacyVanillaCDSSecurity(security);
    final StandardCDSQuotingConvention quoteConvention = StandardCDSQuotingConvention.parse(requirement.getConstraint(
        ISDAFunctionConstants.CDS_QUOTE_CONVENTION));
    final NodalTenorDoubleCurve spreadCurve = (NodalTenorDoubleCurve) inputs.getValue(ValueRequirementNames.BUCKETED_SPREADS);
    if (spreadCurve == null) {
      throw new OpenGammaRuntimeException("Bucketed spreads not available for " + getSpreadCurveIdentifier(security));
    }

    // get the isda curve
    final ISDACompliantYieldCurve yieldCurve = (ISDACompliantYieldCurve) inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (yieldCurve == null) {
      throw new OpenGammaRuntimeException("Couldn't get isda curve");
    }

    final Double cdsQuoteDouble = (Double) inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (cdsQuoteDouble == null) {
      throw new OpenGammaRuntimeException("Couldn't get spread for " + security);
    }
    final CDSAnalyticVisitor pricingVisitor = new CDSAnalyticVisitor(now.toLocalDate(),
                                                                     _holidaySource,
                                                                     _regionSource,
                                                                     recoveryRate);
    final CDSAnalytic pricingCDS = security.accept(pricingVisitor);
    final CDSQuoteConvention quote = SpreadCurveFunctions.getQuotes(security.getMaturityDate(),
                                                                    new double[]{cdsQuoteDouble},
                                                                    security.getParSpread(),
                                                                    quoteConvention,
                                                                    true)[0];
    final QuotedSpread quotedSpread = getQuotedSpread(quote,
                                                      security.isBuy() ? BuySellProtection.BUY : BuySellProtection.SELL,
                                                      yieldCurve,
                                                      pricingCDS,
                                                      security.getParSpread());

    ISDACompliantCreditCurve creditCurve;
    NodalTenorDoubleCurve modifiedSpreadCurve;
    NodalTenorDoubleCurve modifiedPillarCurve;

    if (IMMDateGenerator.isIMMDate(security.getMaturityDate())) {
      final String pillarString = requirement.getConstraint(ISDAFunctionConstants.ISDA_BUCKET_TENORS);
      final ZonedDateTime[] bucketDates = SpreadCurveFunctions.getPillarDates(now, pillarString);
      final ZonedDateTime[] pillarDates = bucketDates;
      double[] spreads = SpreadCurveFunctions.getSpreadCurveNew(spreadCurve,
                                                                bucketDates,
                                                                security.getStartDate(),
                                                                quoteConvention);
      Tenor[] tenors = SpreadCurveFunctions.getBuckets(pillarString);
      modifiedSpreadCurve = new NodalTenorDoubleCurve(tenors, ArrayUtils.toObject(spreads), true);
      modifiedPillarCurve = modifiedSpreadCurve; // for IMM buckets and spreads are the same
      //final CDSQuoteConvention[] quotes = SpreadCurveFunctions.getQuotes(security.getMaturityDate(), spreads, security.getParSpread(), quoteConvention, false);

      // CDS analytics for credit curve
      final CDSAnalytic[] creditAnalytics = new CDSAnalytic[pillarDates.length];
      for (int i = 0; i < creditAnalytics.length; i++) {
        final CDSAnalyticVisitor curveVisitor = new CDSAnalyticVisitor(now.toLocalDate(),
                                                                       _holidaySource,
                                                                       _regionSource,
                                                                       security.getStartDate().toLocalDate(),
                                                                       pillarDates[i].toLocalDate(),
                                                                       recoveryRate);
        creditAnalytics[i] = security.accept(curveVisitor);
      }
      creditCurve = CREDIT_CURVE_BUILDER.calibrateCreditCurve(pricingCDS, quotedSpread, yieldCurve);

    } else {
      // non IMM date - pillars set to fixed set
      final String pillarString = NON_IMM_PILLAR_TENORS;
      final String bucketString = requirement.getConstraint(ISDAFunctionConstants.ISDA_BUCKET_TENORS);
      final ZonedDateTime[] bucketDates = SpreadCurveFunctions.getPillarDatesNoAdjustment(now, bucketString);
      final ZonedDateTime[] pillarDates = SpreadCurveFunctions.getPillarDatesNoAdjustment(now, pillarString);
      double[] bucketSpreads = SpreadCurveFunctions.getSpreadCurveNew(spreadCurve,
                                                                      bucketDates,
                                                                      security.getStartDate(),
                                                                      quoteConvention);
      double[] pillarSpreads = SpreadCurveFunctions.getSpreadCurveNew(spreadCurve,
                                                                      pillarDates,
                                                                      security.getStartDate(),
                                                                      quoteConvention);
      Tenor[] bucketTenors = SpreadCurveFunctions.getBuckets(bucketString);
      Tenor[] pillarTenors = SpreadCurveFunctions.getBuckets(pillarString);
      modifiedSpreadCurve = new NodalTenorDoubleCurve(bucketTenors, ArrayUtils.toObject(bucketSpreads), true);
      modifiedPillarCurve = new NodalTenorDoubleCurve(pillarTenors, ArrayUtils.toObject(pillarSpreads), true);
      final CDSQuoteConvention[] quotes = SpreadCurveFunctions.getQuotes(security.getMaturityDate(),
                                                                         pillarSpreads,
                                                                         security.getParSpread(),
                                                                         quoteConvention,
                                                                         false);

      // CDS analytics for credit curve
      final CDSAnalytic[] creditAnalytics = new CDSAnalytic[pillarDates.length];
      for (int i = 0; i < creditAnalytics.length; i++) {
        final CDSAnalyticVisitor curveVisitor = new CDSAnalyticVisitor(now.toLocalDate(),
                                                                       _holidaySource,
                                                                       _regionSource,
                                                                       security.getStartDate().toLocalDate(),
                                                                       pillarDates[i].toLocalDate(), recoveryRate);
        creditAnalytics[i] = security.accept(curveVisitor);
      }
      creditCurve = CREDIT_CURVE_BUILDER.calibrateCreditCurve(creditAnalytics, quotes, yieldCurve);
    }


    //if (IMMDateGenerator.isIMMDate(security.getMaturityDate())) {
    //  creditCurve = CREDIT_CURVE_BUILDER.calibrateCreditCurve(pricingCDS, quotedSpread, yieldCurve);
    //} else {
    //  creditCurve = CREDIT_CURVE_BUILDER.calibrateCreditCurve(creditAnalytics, quotes, yieldCurve);
    //}
    //if (IMMDateGenerator.isIMMDate(security.getMaturityDate())) {
    //  // form from single point instead of all
    //  final int index = Arrays.binarySearch(spreadCurve, security.getMaturityDate());
    //  ArgumentChecker.isTrue(index > 0, "cds maturity " + security + " not in pillar dates");
    //  creditCurve = creditCurveBuilder.calibrateCreditCurve(new CDSAnalytic[] { creditAnalytics[index] },
    //                                                        new CDSQuoteConvention[] { quotes[index] }, yieldCurve);
    //} else {
    //  creditCurve = creditCurveBuilder.calibrateCreditCurve(creditAnalytics, quotes, yieldCurve);
    //}
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.HAZARD_RATE_CURVE,
                                                           target.toSpecification(),
                                                           requirement.getConstraints());

    // spreads
    final ValueSpecification spreadSpec = new ValueSpecification(ValueRequirementNames.BUCKETED_SPREADS,
                                                                 target.toSpecification(),
                                                                 requirement.getConstraints());
    final ValueSpecification pillarSpec = new ValueSpecification(ValueRequirementNames.PILLAR_SPREADS,
                                                                 target.toSpecification(),
                                                                 requirement.getConstraints());

    return Sets.newHashSet(new ComputedValue(spec, creditCurve),
                           new ComputedValue(spreadSpec, modifiedSpreadCurve),
                           new ComputedValue(pillarSpec, modifiedPillarCurve));
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
    final ValueSpecification creditCurveSpec = new ValueSpecification(ValueRequirementNames.HAZARD_RATE_CURVE,
                                                                      target.toSpecification(),
                                                                      properties);
    final ValueProperties spreadProperties = createValueProperties()
        .withAny(ISDAFunctionConstants.CDS_QUOTE_CONVENTION)
        .withAny(ISDAFunctionConstants.ISDA_BUCKET_TENORS)
        .withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
        .withAny(ISDAFunctionConstants.ISDA_CURVE_DATE)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .get();
    final ValueSpecification spreadSpec = new ValueSpecification(ValueRequirementNames.BUCKETED_SPREADS,
                                                                 target.toSpecification(),
                                                                 spreadProperties);
    final ValueSpecification pillarSpec = new ValueSpecification(ValueRequirementNames.PILLAR_SPREADS,
                                                                 target.toSpecification(),
                                                                 spreadProperties);
    return Sets.newHashSet(creditCurveSpec, spreadSpec, pillarSpec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context,
                                               final ComputationTarget target,
                                               final ValueRequirement desiredValue) {
    final CreditDefaultSwapSecurity cds = (CreditDefaultSwapSecurity) target.getSecurity();
    final CreditCurveIdentifier spreadIdentifier = getSpreadCurveIdentifier(cds);

    final Currency ccy = cds.getNotional().getCurrency();
    final CreditCurveIdentifier isdaIdentifier = getISDACurveIdentifier(cds);

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
    final ValueRequirement isdaRequirment = new ValueRequirement(ValueRequirementNames.YIELD_CURVE,
                                                                 ComputationTargetType.CURRENCY,
                                                                 ccy.getUniqueId(),
                                                                 isdaProperties);

    final ValueRequirement spreadRequirment = new ValueRequirement(ValueRequirementNames.BUCKETED_SPREADS,
                                                                   ComputationTargetType.PRIMITIVE,
                                                                   spreadIdentifier.getUniqueId());

    // get individual spread for this cds (ignore business day adjustment on either)
    final Period period = Period.between(cds.getStartDate().toLocalDate().withDayOfMonth(20),
                                         cds.getMaturityDate().toLocalDate().withDayOfMonth(20));
    final ValueRequirement cdsSpreadRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE,
                                                                       ComputationTargetType.PRIMITIVE,
                                                                       ExternalId.of("Tenor", period.toString()));

    final CdsRecoveryRateIdentifier recoveryRateIdentifier = cds.accept(new CreditSecurityToRecoveryRateVisitor(context.getSecuritySource()));
    final ValueRequirement recoveryRateRequirement = new ValueRequirement("PX_LAST",
                                                                          ComputationTargetType.PRIMITIVE,
                                                                          recoveryRateIdentifier.getExternalId());

    return Sets.newHashSet(spreadRequirment, isdaRequirment, cdsSpreadRequirement, recoveryRateRequirement);
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
