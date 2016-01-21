/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isdanew;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ArrayUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.StandardCDSQuotingConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FiniteDifferenceSpreadSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ParSpread;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.analytics.math.curve.NodalTenorDoubleCurve;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.holiday.HolidaySource;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.TenorLabelledMatrix1D;
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToRecoveryRateVisitor;
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

//TODO Check bucketed cs01 in desired outputs before calcing as expensive

/**
 * Abstract class for cds functions that require ISDA and spread curves.
 */
public class ISDACompliantCDSFunction extends NonCompiledInvoker {

  private final String[] _valueRequirements = new String[] {ValueRequirementNames.ACCRUED_DAYS, ValueRequirementNames.ACCRUED_PREMIUM,
    ValueRequirementNames.POINTS_UPFRONT, ValueRequirementNames.CLEAN_PRESENT_VALUE, ValueRequirementNames.DIRTY_PRESENT_VALUE, ValueRequirementNames.CLEAN_PRICE,
    ValueRequirementNames.QUOTED_SPREAD, ValueRequirementNames.UPFRONT_AMOUNT, ValueRequirementNames.BUCKETED_CS01, ValueRequirementNames.PARALLEL_CS01, ValueRequirementNames.PRINCIPAL};
  public static double ONE_BPS = 1e-4; // fractional 1 BPS
  private HolidaySource _holidaySource; //OpenGammaCompilationContext.getHolidaySource(context);
  private RegionSource _regionSource;
  private static final MarketQuoteConverter POINTS_UP_FRONT_CONVERTER = new MarketQuoteConverter();
  protected static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();
  public static final FiniteDifferenceSpreadSensitivityCalculator CALCULATOR = new FiniteDifferenceSpreadSensitivityCalculator();

  @Override
  public void init(final FunctionCompilationContext context) {
    // using hardcoded region and calendar for now
    _holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    _regionSource = new TestRegionSource(getTestRegion()); //OpenGammaCompilationContext.getRegionSource(context);
    //_converter = new CreditDefaultSwapSecurityConverterDeprecated(holidaySource, regionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
    throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final ValueRequirement requirement = desiredValues.iterator().next();
    final ValueProperties properties = requirement.getConstraints().copy().get();

    final LegacyVanillaCDSSecurity security = (LegacyVanillaCDSSecurity) target.getSecurity();
    //LegacyVanillaCreditDefaultSwapDefinition cds = _converter.visitLegacyVanillaCDSSecurity(security);
    final ValueRequirement desiredValue = desiredValues.iterator().next(); // all same constraints

    final String quoteConventionString = desiredValue.getConstraint(ISDAFunctionConstants.CDS_QUOTE_CONVENTION);
    final StandardCDSQuotingConvention quoteConvention = StandardCDSQuotingConvention.parse(quoteConventionString);

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
    boolean useBucketStart = security.getAttributes().containsKey("bucketStartDate");
    //final String pillarString = IMMDateGenerator.isIMMDate(security.getMaturityDate()) ? requirement.getConstraint(ISDAFunctionConstants.ISDA_BUCKET_TENORS) : ISDACompliantCreditCurveFunction.NON_IMM_PILLAR_TENORS;
    final ZonedDateTime[] bucketDates;
    if (useBucketStart) {
      ZonedDateTime bucketStart = LocalDate.parse(security.getAttributes().get("bucketStartDate")).atStartOfDay(now.getZone());
      bucketDates = SpreadCurveFunctions.getPillarDatesFromBucketStart(bucketStart, spreadObject.getXData()); 
    } else {
      bucketDates = SpreadCurveFunctions.getPillarDates(now, spreadObject.getXData());
    }
    final CDSQuoteConvention[] quotes = SpreadCurveFunctions.getQuotes(security.getMaturityDate(), spreads, security.getParSpread(), quoteConvention, false);

    // spreads
    NodalTenorDoubleCurve pillarObject = (NodalTenorDoubleCurve) inputs.getValue(ValueRequirementNames.PILLAR_SPREADS);
    if (pillarObject == null) {
      throw new OpenGammaRuntimeException("Unable to get pillars");
    }

    // CDS analytics for credit curve (possible performance improvement if earlier result obtained)
    //final LegacyVanillaCreditDefaultSwapDefinition curveCDS = cds.withStartDate(now);
    //security.setStartDate(now); // needed for curve instruments
    final CDSAnalytic[] bucketCDSs = new CDSAnalytic[bucketDates.length];
    for (int i = 0; i < bucketCDSs.length; i++) {
      //security.setMaturityDate(bucketDates[i]);
      final CDSAnalyticVisitor visitor = new CDSAnalyticVisitor(now.toLocalDate(), _holidaySource, _regionSource, security.getStartDate().toLocalDate(), bucketDates[i].toLocalDate(), recoveryRate);
      bucketCDSs[i] = security.accept(visitor);
    }

    final ZonedDateTime[] pillarDates;
    if (useBucketStart) {
      ZonedDateTime bucketStart = LocalDate.parse(security.getAttributes().get("bucketStartDate")).atStartOfDay(now.getZone());
      pillarDates = SpreadCurveFunctions.getPillarDatesFromBucketStart(bucketStart, pillarObject.getXData());
    } else {
      pillarDates = SpreadCurveFunctions.getPillarDates(now, pillarObject.getXData());
    }
        
    final CDSAnalytic[] pillarCDSs = new CDSAnalytic[pillarDates.length];
    for (int i = 0; i < pillarCDSs.length; i++) {
      //security.setMaturityDate(bucketDates[i]);
      final CDSAnalyticVisitor visitor = new CDSAnalyticVisitor(now.toLocalDate(), _holidaySource, _regionSource, security.getStartDate().toLocalDate(), pillarDates[i].toLocalDate(), recoveryRate);
      pillarCDSs[i] = security.accept(visitor);
    }

    final ISDACompliantCreditCurve creditCurve = (ISDACompliantCreditCurve) inputs.getValue(ValueRequirementNames.HAZARD_RATE_CURVE);
    if (creditCurve == null) {
      throw new OpenGammaRuntimeException("Couldnt get credit curve");
    }

    //final CDSAnalytic analytic = CDSAnalyticConverter.create(cds, now.toLocalDate());
    final CDSAnalyticVisitor visitor = new CDSAnalyticVisitor(now.toLocalDate(), _holidaySource, _regionSource, recoveryRate);
    final BuySellProtection buySellProtection = security.isBuy() ? BuySellProtection.BUY : BuySellProtection.SELL;
//    final String term = new Tenor(Period.between(security.getStartDate().toLocalDate(), security.getMaturityDate().toLocalDate())).getPeriod().toString();
//    final Double cdsQuoteDouble = (Double) inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE,
//        ComputationTargetType.PRIMITIVE, ExternalId.of("Tenor", term)));
    final Double cdsQuoteDouble = (Double) inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (cdsQuoteDouble == null) {
      throw new OpenGammaRuntimeException("Couldn't get spread for " + security);
    }
    final CDSQuoteConvention quote = SpreadCurveFunctions.getQuotes(security.getMaturityDate(), new double[] {cdsQuoteDouble}, security.getParSpread(), quoteConvention, true)[0];

    boolean isNonIMMFAndFromPUF = !IMMDateLogic.isIMMDate(security.getMaturityDate().toLocalDate()) && quote instanceof PointsUpFront;
    boolean isNonIMMAndFromSpread = !IMMDateLogic.isIMMDate(security.getMaturityDate().toLocalDate()) && (quote instanceof QuotedSpread || quote instanceof ParSpread);
    int buySellPremiumFactor = security.isBuy() ? -1 : 1;

    final CDSAnalytic analytic = (isNonIMMAndFromSpread || isNonIMMFAndFromPUF) ? 
        ((CDSAnalyticVisitor) visitor).visitLegacyVanillaCDSSecurityNonIMMIrrSch(security) : security.accept(visitor);
    
    final double notional = security.getNotional().getAmount();
    final double coupon = security.getParSpread() * ONE_BPS;
    final PointsUpFront puf = getPointsUpfront(quote, buySellProtection, yieldCurve, analytic, creditCurve);
    final double accruedPremiumPrim =   isNonIMMAndFromSpread || isNonIMMFAndFromPUF ? 0 : analytic.getAccruedPremium(coupon);
//    final double accruedPremium = isNonIMMAndFromSpread || isNonIMMFAndFromPUF ? 0 : analytic.getAccruedPremium(coupon) * notional * buySellPremiumFactor;
    final double accruedPremium = isNonIMMAndFromSpread || isNonIMMFAndFromPUF ? 0 : accruedPremiumPrim * notional * buySellPremiumFactor;
    final int accruedDays = isNonIMMAndFromSpread || isNonIMMFAndFromPUF ? 0 : analytic.getAccuredDays();
    final double quotedSpread = getQuotedSpread(quote, puf, buySellProtection, yieldCurve, analytic).getQuotedSpread();
    final double upfrontAmount = isNonIMMAndFromSpread ? 0 : getUpfrontAmount(analytic, puf, notional, accruedPremiumPrim, buySellProtection);
    final double cleanPV = puf.getPointsUpFront() * notional;
    final double principal = isNonIMMAndFromSpread ? 0 : cleanPV;
    final double cleanPrice = getCleanPrice(puf);
    final TenorLabelledMatrix1D bucketedCS01 = getBucketedCS01(analytic, bucketCDSs, spreadObject.getXData(), quote, notional, yieldCurve, creditCurve);

    double factor = isNonIMMAndFromSpread ? yieldCurve.getDiscountFactor(analytic.getValuationTime()) : 1.0;
    final double parallelCS01 = factor * getParallelCS01(quote, analytic, yieldCurve, notional, pillarCDSs, ArrayUtils.toPrimitive(pillarObject.getYData()));

    final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(_valueRequirements.length);
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.ACCRUED_PREMIUM, target.toSpecification(), properties), accruedPremium));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.ACCRUED_DAYS, target.toSpecification(), properties), accruedDays));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.QUOTED_SPREAD, target.toSpecification(), properties), quotedSpread / ONE_BPS));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.UPFRONT_AMOUNT, target.toSpecification(), properties), upfrontAmount));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.DIRTY_PRESENT_VALUE, target.toSpecification(), properties), upfrontAmount));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.CLEAN_PRESENT_VALUE, target.toSpecification(), properties), cleanPV));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.PRINCIPAL, target.toSpecification(), properties), principal));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.CLEAN_PRICE, target.toSpecification(), properties), cleanPrice));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.BUCKETED_CS01, target.toSpecification(), properties), bucketedCS01));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.PARALLEL_CS01, target.toSpecification(), properties), parallelCS01));
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.POINTS_UPFRONT, target.toSpecification(), properties), puf.getPointsUpFront()));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> specs = new HashSet<>();
    final ValueProperties properties = createValueProperties()
        .withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
        .withAny(ISDAFunctionConstants.ISDA_CURVE_DATE)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
        .withAny(ISDAFunctionConstants.CDS_QUOTE_CONVENTION)
        .withAny(ISDAFunctionConstants.ISDA_BUCKET_TENORS)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .get();
    for (final String value : _valueRequirements) {
      specs.add(new ValueSpecification(value, target.toSpecification(), properties));
    }
    return specs;
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
    final ValueRequirement pillarRequirment = new ValueRequirement(ValueRequirementNames.PILLAR_SPREADS, target.toSpecification(), spreadProperties);
    final ValueRequirement creditCurveRequirement = new ValueRequirement(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), spreadProperties);

    // get individual spread for this cds (ignore business day adjustment on either)
    final Period period = Period.between(cds.getStartDate().toLocalDate().withDayOfMonth(20), cds.getMaturityDate().toLocalDate().withDayOfMonth(20));
    final ValueRequirement cdsSpreadRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalId.of("Tenor", period.toString()));

    final CdsRecoveryRateIdentifier recoveryRateIdentifier = cds.accept(new CreditSecurityToRecoveryRateVisitor(context.getSecuritySource()));
    final ValueRequirement recoveryRateRequirement = new ValueRequirement("PX_LAST", ComputationTargetType.PRIMITIVE, recoveryRateIdentifier.getExternalId());

    return Sets.newHashSet(isdaRequirment, spreadRequirment, cdsSpreadRequirement, creditCurveRequirement, pillarRequirment, recoveryRateRequirement);
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
    } else if (quote instanceof ParSpread) {
      puf = PRICER.pv(analytic, yieldCurve, creditCurve, ((ParSpread) quote).getCoupon());
    } else {
      throw new OpenGammaRuntimeException("Unknown quote type " + quote);
    }
    // SELL protection reverses directions of legs
    puf = (buySellProtection == BuySellProtection.SELL) ? -puf : puf;
    return new PointsUpFront(quote.getCoupon(), puf);
  }

  public static  QuotedSpread getQuotedSpread(CDSQuoteConvention quote, PointsUpFront puf, BuySellProtection buySellProtection, ISDACompliantYieldCurve yieldCurve, CDSAnalytic analytic) {
    double quotedSpread;
    if (quote instanceof QuotedSpread) {
      return (QuotedSpread) quote;
    } else {
      quotedSpread = POINTS_UP_FRONT_CONVERTER.pufToQuotedSpread(analytic, puf.getCoupon(), yieldCurve, puf.getPointsUpFront());
    }
    // SELL protection reverses directions of legs
    quotedSpread = (buySellProtection == BuySellProtection.SELL) ? -quotedSpread : quotedSpread;
    return new QuotedSpread(quote.getCoupon(), quotedSpread);
  }

  public double getUpfrontAmount(final CDSAnalytic analytic, final PointsUpFront puf, final double notional, final double accPremiumPrim, final BuySellProtection buySellProtection) {
    // upfront amount is defined as dirty PV
    double cash = (puf.getPointsUpFront() - accPremiumPrim) * notional;
    // SELL protection reverses directions of legs
    return (buySellProtection == BuySellProtection.SELL) ? -cash : cash;
  }

  public double getCleanPrice(final PointsUpFront puf) {
    return 100.0 * (1 - puf.getPointsUpFront());
  }

  public TenorLabelledMatrix1D getBucketedCS01(CDSAnalytic analytic, CDSAnalytic[] buckets, Tenor[] tenors, CDSQuoteConvention quote, double notional, ISDACompliantYieldCurve yieldCurve, ISDACompliantCreditCurve creditCurve) {
    
    int nBuckets = buckets.length;
    int nShortBuckets = 0; 
    for (int i = 0; i < nBuckets; ++i) { 
      if (analytic.getProtectionEnd() >= buckets[i].getProtectionEnd()) { 
        nShortBuckets = i; 
      } 
    }
    nShortBuckets = (nShortBuckets == nBuckets - 1) ? nShortBuckets + 1 : nShortBuckets + 2; 
    CDSAnalytic[] shortBuckets = Arrays.copyOf(buckets, nShortBuckets);
    
    //TODO: Check quote.getCoupon() is spread value for IMM & 0.01 (or 0.05) for non IMM
//    double[] cs01Values;
    double[] shortCs01Values;
    if (quote instanceof ParSpread) {
      shortCs01Values = CALCULATOR.bucketedCS01FromCreditCurve(analytic, quote.getCoupon(), shortBuckets, yieldCurve, creditCurve, ONE_BPS);
    } else if (quote instanceof PointsUpFront) {
      shortCs01Values = CALCULATOR.bucketedCS01FromPUF(analytic, (PointsUpFront) quote, yieldCurve, shortBuckets, ONE_BPS);
    } else {
      shortCs01Values = CALCULATOR.bucketedCS01FromCreditCurve(analytic, quote.getCoupon()/*coupon * ONE_BPS*/, shortBuckets, yieldCurve, creditCurve, ONE_BPS);
    }
    for (int i = 0; i < shortCs01Values.length; i++) {
      shortCs01Values[i] *= notional * ONE_BPS;
    }
    
    double[] cs01Values = new double [nBuckets];
    System.arraycopy(shortCs01Values, 0, cs01Values, 0, nShortBuckets);
    return new TenorLabelledMatrix1D(tenors, cs01Values);
  }

  public double getParallelCS01(CDSQuoteConvention quote, CDSAnalytic analytic, ISDACompliantYieldCurve yieldCurve, double notional, CDSAnalytic[] pillars, double[] pillarSpreads) {
    double cs01;
    if (quote instanceof ParSpread) {
      cs01 = CALCULATOR.parallelCS01FromParSpreads(analytic,
                                               quote.getCoupon(), // ParSpread
                                               yieldCurve,
                                               pillars,
                                               pillarSpreads,
                                               ONE_BPS,
                                               BumpType.ADDITIVE);
    } else {
      cs01 = CALCULATOR.parallelCS01(analytic, quote, yieldCurve, ONE_BPS);
    }
    return Double.valueOf(cs01 * notional * ONE_BPS);
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
