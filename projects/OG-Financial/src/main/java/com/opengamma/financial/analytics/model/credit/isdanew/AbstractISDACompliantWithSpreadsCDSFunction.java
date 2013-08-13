/**
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
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.StandardCDSQuotingConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ParSpread;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFront;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFrontConverter;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.QuotedSpread;
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
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.analytics.model.credit.IMMDateGenerator;
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

/**
 * Abstract class for cds functions that require ISDA and spread curves.
 */
public abstract class AbstractISDACompliantWithSpreadsCDSFunction extends NonCompiledInvoker {

  private final String _valueRequirement;
  private static double s_tenminus4 = 1e-4; // fractional 1 BPS
  private HolidaySource _holidaySource; //OpenGammaCompilationContext.getHolidaySource(context);
  private RegionSource _regionSource;
  private static final PointsUpFrontConverter POINTS_UP_FRONT_CONVERTER = new PointsUpFrontConverter();
  protected static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();

  public AbstractISDACompliantWithSpreadsCDSFunction(final String valueRequirement) {
    _valueRequirement = valueRequirement;
  }

  protected static double getTenminus4() {
    return s_tenminus4;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    // using hardcoded region and calendar for now
    _holidaySource = new WeekendHolidaySource(); //OpenGammaCompilationContext.getHolidaySource(context);
    _regionSource = new TestRegionSource(getTestRegion()); //OpenGammaCompilationContext.getRegionSource(context);
    //_converter = new CreditDefaultSwapSecurityConverterDeprecated(holidaySource, regionSource);
  }

  protected abstract Object compute(final ZonedDateTime maturity,
                                    final PointsUpFront puf,
                                    final CDSQuoteConvention quote,
                                    final double notional,
                                    final
                                    BuySellProtection buySellProtection,
                                    final ISDACompliantYieldCurve yieldCurve,
                                    final CDSAnalytic analytic,
                                    final CDSAnalytic[] curveAnalytics,
                                    final CDSQuoteConvention[] quotes,
                                    final ZonedDateTime[] bucketDates,
                                    ISDACompliantCreditCurve creditCurve);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
    throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    ValueRequirement requirement = desiredValues.iterator().next();

    final LegacyVanillaCDSSecurity security = (LegacyVanillaCDSSecurity) target.getSecurity();
    //LegacyVanillaCreditDefaultSwapDefinition cds = _converter.visitLegacyVanillaCDSSecurity(security);
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);

    final String quoteConventionString = desiredValue.getConstraint(ISDAFunctionConstants.CDS_QUOTE_CONVENTION);
    final StandardCDSQuotingConvention quoteConvention = StandardCDSQuotingConvention.parse(quoteConventionString);

    // get the isda curve
    final Object isdaObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (isdaObject == null) {
      throw new OpenGammaRuntimeException("Couldn't get isda curve");
    }
    final ISDACompliantYieldCurve yieldCurve = (ISDACompliantYieldCurve) isdaObject;

    // spreads
    NodalTenorDoubleCurve spreadObject = (NodalTenorDoubleCurve) inputs.getValue(ValueRequirementNames.BUCKETED_SPREADS);
    if (spreadObject == null) {
      throw new OpenGammaRuntimeException("Unable to get spreads");
    }
    final double[] spreads = ArrayUtils.toPrimitive(spreadObject.getYData());
    final String pillarString = IMMDateGenerator.isIMMDate(security.getMaturityDate()) ? requirement.getConstraint(ISDAFunctionConstants.ISDA_BUCKET_TENORS) : ISDACompliantCreditCurveFunction.NON_IMM_PILLAR_TENORS;
    final ZonedDateTime[] bucketDates = SpreadCurveFunctions.getPillarDates(now, pillarString);
    final CDSQuoteConvention[] quotes = SpreadCurveFunctions.getQuotes(security.getMaturityDate(), spreads, security.getParSpread(), quoteConvention);

    // CDS analytics for credit curve (possible performance improvement if earlier result obtained)
    //final LegacyVanillaCreditDefaultSwapDefinition curveCDS = cds.withStartDate(now);
    //security.setStartDate(now); // needed for curve instruments
    final CDSAnalytic[] creditAnalytics = new CDSAnalytic[bucketDates.length];
    for (int i = 0; i < creditAnalytics.length; i++) {
      //security.setMaturityDate(bucketDates[i]);
      final CDSAnalyticVisitor visitor = new CDSAnalyticVisitor(now.toLocalDate(), _holidaySource, _regionSource, security.getStartDate().toLocalDate(), bucketDates[i].toLocalDate());
      //creditAnalytics[i] = CDSAnalyticConverter.create(curveCDS, now.toLocalDate(), bucketDates[i].toLocalDate());
      creditAnalytics[i] = security.accept(visitor);
    }

    final ISDACompliantCreditCurve creditCurve = (ISDACompliantCreditCurve) inputs.getValue(ValueRequirementNames.HAZARD_RATE_CURVE);
    if (creditCurve == null) {
      throw new OpenGammaRuntimeException("Couldnt get credit curve");
    }

    //final CDSAnalytic analytic = CDSAnalyticConverter.create(cds, now.toLocalDate());
    final CDSAnalyticVisitor visitor = new CDSAnalyticVisitor(now.toLocalDate(), _holidaySource, _regionSource);
    final CDSAnalytic analytic = security.accept(visitor);
    final BuySellProtection buySellProtection = security.isBuy() ? BuySellProtection.BUY : BuySellProtection.SELL;
    final Double cdsQuoteDouble = (Double) inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (cdsQuoteDouble == null) {
      throw new OpenGammaRuntimeException("Couldn't get spread for " + security);
    }
    final CDSQuoteConvention quote = SpreadCurveFunctions.getQuotes(security.getMaturityDate(), new double[] {cdsQuoteDouble}, security.getParSpread(), quoteConvention)[0];
    // TODO: Only do this once
    final PointsUpFront puf = getPointsUpfront(quote, buySellProtection, yieldCurve, analytic, creditCurve);

    final Object result = compute(security.getMaturityDate(), puf, quote, security.getNotional().getAmount(),
        buySellProtection, yieldCurve, analytic, creditAnalytics, quotes, bucketDates, creditCurve);

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
        .withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
        .withAny(ISDAFunctionConstants.ISDA_CURVE_DATE)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
        .withAny(ISDAFunctionConstants.CDS_QUOTE_CONVENTION)
        .withAny(ISDAFunctionConstants.ISDA_BUCKET_TENORS)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .get();
    return Collections.singleton(new ValueSpecification(getValueRequirement(), target.toSpecification(), properties));
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

    //market  spreads
    final ValueProperties spreadProperties = ValueProperties.builder()
        .with(ISDAFunctionConstants.CDS_QUOTE_CONVENTION, quoteConvention)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, isdaOffset)
        .with(ISDAFunctionConstants.ISDA_CURVE_DATE, isdaCurveDate)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, isdaCurveMethod)
        .with(ISDAFunctionConstants.ISDA_BUCKET_TENORS, bucketTenors)
        .get();
    final ValueRequirement spreadRequirment = new ValueRequirement(ValueRequirementNames.BUCKETED_SPREADS, target.toSpecification(), spreadProperties);
    final ValueRequirement creditCurveRequirement = new ValueRequirement(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), spreadProperties);

    // get individual spread for this cds (ignore business day adjustment on either)
    final Period period = Period.between(cds.getStartDate().toLocalDate().withDayOfMonth(20), cds.getMaturityDate().toLocalDate().withDayOfMonth(20));
    final ValueRequirement cdsSpreadRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalId.of("Tenor", period.toString()));

    return Sets.newHashSet(isdaRequirment, spreadRequirment, cdsSpreadRequirement, creditCurveRequirement);
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
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

  public PointsUpFront getPointsUpfront(CDSQuoteConvention quote, BuySellProtection buySellProtection, ISDACompliantYieldCurve yieldCurve, CDSAnalytic analytic, ISDACompliantCreditCurve creditCurve) {
    double puf = 0.0;
    if (quote instanceof PointsUpFront) {
      return (PointsUpFront) quote;
    } else if (quote instanceof QuotedSpread) {
      puf = POINTS_UP_FRONT_CONVERTER.quotedSpreadToPUF(analytic,
                                           quote.getCoupon(),
                                           yieldCurve,
                                           ((QuotedSpread) quote).getQuotedSpread());
      // SELL protection reverses directions of legs
      puf = Double.valueOf(buySellProtection == BuySellProtection.SELL ? -puf : puf);
    } else if (quote instanceof ParSpread) {
      puf = PRICER.pv(analytic, yieldCurve, creditCurve, ((ParSpread) quote).getCoupon());
      // SELL protection reverses directions of legs
      puf = Double.valueOf(buySellProtection == BuySellProtection.SELL ? -puf : puf);
    } else {
      throw new OpenGammaRuntimeException("Unknown quote type " + quote);
    }
    return new PointsUpFront(quote.getCoupon(), puf);
  }

  public static  QuotedSpread getQuotedSpread(CDSQuoteConvention quote, BuySellProtection buySellProtection, ISDACompliantYieldCurve yieldCurve, CDSAnalytic analytic, double premium) {
    double quotedSpread;
    if (quote instanceof PointsUpFront) {
      quotedSpread = POINTS_UP_FRONT_CONVERTER.pufToQuotedSpread(analytic, quote.getCoupon(), yieldCurve, ((PointsUpFront) quote).getPointsUpFront());
    } else if (quote instanceof QuotedSpread) {
      return (QuotedSpread) quote;
    } else if (quote instanceof ParSpread) {
      quotedSpread = POINTS_UP_FRONT_CONVERTER.parSpreadsToQuotedSpreads(new CDSAnalytic[] {analytic}, premium * getTenminus4(), yieldCurve, new double[] {quote.getCoupon()})[0];
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

  protected String getValueRequirement() {
    return _valueRequirement;
  }

  /** test region */
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
