/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.trade.fpml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.ByteSource;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.irs.NotionalExchange;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.irs.RateAveragingMethod;
import com.opengamma.financial.security.irs.StubCalculationMethod;
import com.opengamma.financial.security.irs.StubCalculationMethod.Builder;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.trade.ForwardRateAgreementTrade;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.sesame.trade.TradeWrapper;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Loader of trade data in FpML v5.8 format.
 * <p>
 * This handles the subset of FpML necessary to populate the trade model.
 */
public final class FpmlTradeParser {
  // Notes: Streaming trades directly from the file is difficult due to the
  // need to parse the party element at the root, which is after the trades

  /**
   * The 'href' attribute key.
   */
  private static final String HREF = "href";

  /**
   * The parsed file.
   */
  private final XmlElement _fpmlRoot;
  /**
   * The map of references.
   */
  private final ImmutableMap<String, XmlElement> _refs;
  /**
   * Map of reference id to partyId.
   */
  private final ListMultimap<String, String> _parties;
  /**
   * The party reference id.
   */
  private String _ourPartyHrefId;

  /**
   * Creates an instance, parsing the specified source.
   * 
   * @param source  the source of the FpML XML document
   * @param ourParty  our party identifier, as stored in {@code <partyId>}
   */
  public FpmlTradeParser(ByteSource source, String ourParty) {
    ArgumentChecker.notNull(source, "source");
    ArgumentChecker.notNull(ourParty, "ourParty");
    XmlFile xmlFile = XmlFile.of(source, "id");
    _fpmlRoot = xmlFile.getRoot();
    _refs = xmlFile.getReferences();
    _parties = parseParties(xmlFile.getRoot());
    _ourPartyHrefId = findOurParty(ourParty);
  }

  /**
   * Creates an instance, based on the specified element.
   * 
   * @param fpmlRootEl  the source of the FpML XML document
   * @param refs  the map of id/href to referenced element
   * @param ourParty  our party identifier, as stored in {@code <partyId>}
   */
  public FpmlTradeParser(XmlElement fpmlRootEl, Map<String, XmlElement> refs, String ourParty) {
    ArgumentChecker.notNull(fpmlRootEl, "fpmlRootEl");
    ArgumentChecker.notNull(refs, "refs");
    ArgumentChecker.notNull(ourParty, "ourParty");
    _fpmlRoot = fpmlRootEl;
    _refs = ImmutableMap.copyOf(refs);
    _parties = parseParties(fpmlRootEl);
    _ourPartyHrefId = findOurParty(ourParty);
  }

  // parse all the root-level party elements
  private static ListMultimap<String, String> parseParties(XmlElement root) {
    ListMultimap<String, String> parties = ArrayListMultimap.create();
    for (XmlElement child : root.getChildren("party")) {
      parties.putAll(child.getAttribute("id"), findPartyIds(child));
    }
    return ImmutableListMultimap.copyOf(parties);
  }

  // find the party identifiers
  private static List<String> findPartyIds(XmlElement party) {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    for (XmlElement child : party.getChildren("partyId")) {
      if (child.hasContent()) {
        builder.add(child.getContent());
      }
    }
    return builder.build();
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the FpML root elements into a list of trades.
   * 
   * @return the trades
   * @throws FpmlParseException if a parse error occurred
   */
  public List<TradeWrapper<?>> parseTrades() {
    try {
      List<XmlElement> tradeEls = _fpmlRoot.getChildren("trade");
      ImmutableList.Builder<TradeWrapper<?>> builder = ImmutableList.builder();
      for (XmlElement tradeEl : tradeEls) {
        builder.add(parseTrade(tradeEl));
      }
      return builder.build();
      
    } catch (FpmlParseException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      throw new FpmlParseException(ex);
    }
  }

  private TradeWrapper<?> parseTrade(XmlElement tradeEl) {
    XmlElement tradeHeaderEl = tradeEl.getChildSingle("tradeHeader");
    LocalDate tradeDate = parseDate(tradeHeaderEl.getChildSingle("tradeDate"));

    Optional<XmlElement> fra = tradeEl.getChildOptional("fra");
    if (fra.isPresent()) {
      return parseFraTrade(fra.get(), tradeDate);
    }
    Optional<XmlElement> swap = tradeEl.getChildOptional("swap");
    if (swap.isPresent()) {
      return parseSwapTrade(swap.get(), tradeDate);
    }
    throw new FpmlParseException("Unknown product type, not fra or swap");
  }

  //-------------------------------------------------------------------------
  // FRA
  //-------------------------------------------------------------------------
  private ForwardRateAgreementTrade parseFraTrade(XmlElement fraEl, LocalDate tradeDate) {
    // payment date must be same as start date
    // FRAs with an interpolated index are not supported
    // payment business day convention is not used
    // additional payments are not used
    // only ISDA discounting is supported
    // fixing offset of calendar days or months/years is not supported

    // buy/sell and counterparty
    String buyerPartyReference = fraEl.getChildSingle("buyerPartyReference").getAttribute(HREF);
    String sellerPartyReference = fraEl.getChildSingle("sellerPartyReference").getAttribute(HREF);
    int sign;
    ExternalId counterparty;
    if (buyerPartyReference.equals(_ourPartyHrefId)) {
      sign = 1;
      counterparty = ExternalId.of(Counterparty.DEFAULT_SCHEME, _parties.get(sellerPartyReference).get(0));
    } else if (sellerPartyReference.equals(_ourPartyHrefId)) {
      sign = -1;
      counterparty = ExternalId.of(Counterparty.DEFAULT_SCHEME, _parties.get(buyerPartyReference).get(0));
    } else {
      throw new FpmlParseException(
          "Neither buyerPartyReference nor sellerPartyReference contain our party ID: " + _ourPartyHrefId);
    }
    // start date
    LocalDate startDate = parseDate(fraEl.getChildSingle("adjustedEffectiveDate"));
    // end date
    LocalDate endDate = parseDate(fraEl.getChildSingle("adjustedTerminationDate"));
    // payment date
    XmlElement paymentDateEl = fraEl.getChildSingle("paymentDate");
    LocalDate paymentDate = parseDate(paymentDateEl.getChildSingle("unadjustedDate"));
    if (!startDate.equals(paymentDate)) {
      throw new FpmlParseException("Only startDate = paymentDate is supported");
    }
    XmlElement paymentDateAdjEl = paymentDateEl.getChildSingle("dateAdjustments");
    Set<ExternalId> paymentCalendars = parseBusinessDayAdjustments(paymentDateAdjEl).getSecond();
    // fixing offset
    int fixingLag = -parseRelativeDateOffsetDays(fraEl.getChildSingle("fixingDateOffset"));
    Pair<BusinessDayConvention, Set<ExternalId>> fixingAdjustments =
        parseRelativeDateOffsetDaysAdjustment(fraEl.getChildSingle("fixingDateOffset"));
    // dateRelativeTo required to refer to adjustedEffectiveDate, so ignored here
    // day count
    DayCount dayCount = parseDayCountFraction(fraEl.getChildSingle("dayCountFraction"));
    // notional
    CurrencyAmount notional = parseCurrencyAmount(fraEl.getChildSingle("notional"));
    // fixed rate
    double fixedRate = parseDecimal(fraEl.getChildSingle("fixedRate"));
    // index
    Frequency indexTenor = parseIndexFrequency(fraEl);
    ExternalId indexId = parseIndexId(fraEl);
    // discounting
    String fraDiscounting = fraEl.getChildSingle("fraDiscounting").getContent();
    if (!"ISDA".equals(fraDiscounting)) {
      throw new FpmlParseException("Only fraDiscounting = ISDA is supported");
    }

    ForwardRateAgreementSecurity fraSecurity = new ForwardRateAgreementSecurity(
        notional.getCurrency(),
        indexId,
        indexTenor,
        startDate,
        endDate,
        fixedRate,
        notional.getAmount() * sign,
        null,  // derived fixingDate
        dayCount,
        fixingAdjustments.getFirst(),
        fixingAdjustments.getSecond(),
        paymentCalendars,
        fixingLag);
    Trade trade = new SimpleTrade(
        fraSecurity,
        BigDecimal.ONE,
        new SimpleCounterparty(counterparty),
        tradeDate,
        OffsetTime.of(LocalTime.MIDNIGHT, ZoneOffset.UTC));
    return new ForwardRateAgreementTrade(trade);
  }

  //-------------------------------------------------------------------------
  // Swap
  //-------------------------------------------------------------------------
  private InterestRateSwapTrade parseSwapTrade(XmlElement swapEl, LocalDate tradeDate) {
    // effective/termination date must be the same on all legs
    // relativeEffectiveDate/relativeTerminationDate not supported
    // firstPeriodStartDate not supported
    // effectiveDate adjustments not supported
    // cap/floor/rateTretment/discounting not supported
    // fixed leg interest rate must not vary over time
    // floating leg gearing and spred must not vary over time
    // negativeInterestRateTreatment is not supported (floor rate in model is not used)
    
    ImmutableList<XmlElement> legEls = swapEl.getChildren("swapStream");
    ExternalId counterparty = null;
    LocalDate effectiveDate = null;
    LocalDate terminationDate = null;
    NotionalExchange notionalExchange = NotionalExchange.NO_EXCHANGE;
    List<InterestRateSwapLeg> legs = new ArrayList<>();
    for (XmlElement legEl : legEls) {
      // pay/receive
      String payerPartyReference = legEl.getChildSingle("payerPartyReference").getAttribute(HREF);
      String receiverPartyReference = legEl.getChildSingle("receiverPartyReference").getAttribute(HREF);
      PayReceiveType payReceive;
      // determine direction and setup counterparty
      if (payerPartyReference.equals(_ourPartyHrefId)) {
        ExternalId proposedCounterparty = ExternalId.of(Counterparty.DEFAULT_SCHEME, _parties.get(receiverPartyReference).get(0));
        if (counterparty == null) {
          counterparty = proposedCounterparty;
        } else if (!counterparty.equals(proposedCounterparty)) {
          throw new FpmlParseException(
              "Two different counterparties found: " + counterparty + " and " + proposedCounterparty);
        }
        payReceive = PayReceiveType.PAY;

      } else if (receiverPartyReference.equals(_ourPartyHrefId)) {
        ExternalId proposedCounterparty = ExternalId.of(Counterparty.DEFAULT_SCHEME, _parties.get(payerPartyReference).get(0));
        if (counterparty == null) {
          counterparty = proposedCounterparty;
        } else if (!counterparty.equals(proposedCounterparty)) {
          throw new FpmlParseException(
              "Two different counterparties found: " + counterparty + " and " + proposedCounterparty);
        }
        payReceive = PayReceiveType.RECEIVE;

      } else {
        throw new FpmlParseException(
            "Neither payerPartyReference nor receiverPartyReference contain our party ID: " + _ourPartyHrefId);
      }
      
      // calculation
      XmlElement calcPeriodAmountEl = legEl.getChildSingle("calculationPeriodAmount");
      validateNotPresent(calcPeriodAmountEl, "knownAmountSchedule");
      XmlElement calcEl = calcPeriodAmountEl.getChildSingle("calculation");
      validateNotPresent(calcEl, "fxLinkedNotionalSchedule");
      validateNotPresent(calcEl, "futureValueNotional");
      
      // calculation dates
      XmlElement calcPeriodDatesEl = legEl.getChildSingle("calculationPeriodDates");
      // business day adjustments
      Pair<BusinessDayConvention, Set<ExternalId>> calcBda = parseBusinessDayAdjustments(
          calcPeriodDatesEl.getChildSingle("calculationPeriodDatesAdjustments"));
      // start date
      Optional<XmlElement> effectiveDateEl = calcPeriodDatesEl.getChildOptional("effectiveDate");
      if (effectiveDateEl.isPresent()) {
        Pair<LocalDate, Pair<BusinessDayConvention, Set<ExternalId>>> adj = parseAdjustableDate(effectiveDateEl.get());
        if (effectiveDate != null && !effectiveDate.equals(adj.getFirst())) {
          throw new FpmlParseException("Two different effective dates found");
        }
        effectiveDate = adj.getFirst();
      } else {
        throw new FpmlParseException("Missing 'effectiveDate' elememt");
      }
      // end date
      Optional<XmlElement> terminationDateEl = calcPeriodDatesEl.getChildOptional("terminationDate");
      Pair<BusinessDayConvention, Set<ExternalId>> terminationDateAdj;
      if (terminationDateEl.isPresent()) {
        Pair<LocalDate, Pair<BusinessDayConvention, Set<ExternalId>>> adj = parseAdjustableDate(terminationDateEl.get());
        if (terminationDate != null && !terminationDate.equals(adj.getFirst())) {
          throw new FpmlParseException("Two different termination dates found");
        }
        terminationDate = adj.getFirst();
        terminationDateAdj = adj.getSecond();
      } else {
        throw new FpmlParseException("Missing 'terminationDate' elememt");
      }
      // first regular date
      Optional<XmlElement> firstRegOptEl = calcPeriodDatesEl.getChildOptional("firstRegularPeriodStartDate");
      LocalDate firstRegularStartDate = null;
      if (firstRegOptEl.isPresent()) {
        firstRegularStartDate = parseDate(firstRegOptEl.get());
      }
      // last regular date
      Optional<XmlElement> lastRegOptEl = calcPeriodDatesEl.getChildOptional("lastRegularPeriodEndDate");
      LocalDate lastRegularEndDate = null;
      if (lastRegOptEl.isPresent()) {
        lastRegularEndDate = parseDate(lastRegOptEl.get());
      }
      // stub type
      Optional<XmlElement> stubOptEl = calcPeriodDatesEl.getChildOptional("stubPeriodType");
      StubType stubConvention = StubType.NONE;
      if (stubOptEl.isPresent()) {
        stubConvention = parseStubConvention(stubOptEl.get());
      }
      // frequency
      XmlElement freqEl = calcPeriodDatesEl.getChildSingle("calculationPeriodFrequency");
      Frequency accrualFreq = parseFrequency(freqEl);
      // roll convention
      RollConvention rollConvention = FpmlConversions.rollConvention(freqEl.getChildSingle("rollConvention").getContent());

      // payment dates
      XmlElement paymentDatesEl = legEl.getChildSingle("paymentDates");
      // frequency
      PeriodFrequency paymentFreq = parseFrequency(paymentDatesEl.getChildSingle("paymentFrequency"));
      if (!paymentDatesEl.getChildSingle("payRelativeTo").getContent().equals("CalculationPeriodEndDate")) {
        throw new FpmlParseException("Only 'CalculationPeriodEndDate' is supported for 'payRelativeTo'");
      }
      DateRelativeTo payRelativeTo = parseDateRelativeTo(paymentDatesEl.getChildSingle("payRelativeTo"));
      // offset
      Pair<BusinessDayConvention, Set<ExternalId>> payAdj = parseBusinessDayAdjustments(
          paymentDatesEl.getChildSingle("paymentDatesAdjustments"));
      Optional<XmlElement> paymentOffsetEl = paymentDatesEl.getChildOptional("paymentDaysOffset");
      int payOffsetDays = 0;
      if (paymentOffsetEl.isPresent()) {
        payOffsetDays = parseRelativeDateOffsetDays(paymentOffsetEl.get());
      }
      // compounding
      Optional<XmlElement> cpdOptEl = calcEl.getChildOptional("compoundingMethod");
      CompoundingMethod compoundingMethod = CompoundingMethod.NONE;
      if (cpdOptEl.isPresent()) {
        compoundingMethod = parseCompoundingMethod(cpdOptEl.get());
      }

      // notional exchanges
      Optional<XmlElement> exchangeOptEl = legEl.getChildOptional("principalExchanges");
      if (exchangeOptEl.isPresent()) {
        NotionalExchange.Builder notionalExchangeBuilder = NotionalExchange.builder();
        notionalExchangeBuilder.exchangeInitialNotional(
            Boolean.parseBoolean(exchangeOptEl.get().getChildSingle("initialExchange").getContent()));
        notionalExchangeBuilder.exchangeInterimNotional(
            Boolean.parseBoolean(exchangeOptEl.get().getChildSingle("intermediateExchange").getContent()));
        notionalExchangeBuilder.exchangeFinalNotional(
            Boolean.parseBoolean(exchangeOptEl.get().getChildSingle("finalExchange").getContent()));
        notionalExchange = notionalExchangeBuilder.build();
      }
      // notional schedule
      XmlElement notionalEl = calcEl.getChildSingle("notionalSchedule");
      validateNotPresent(notionalEl, "notionalStepParameters");
      XmlElement notionalScheduleEl = notionalEl.getChildSingle("notionalStepSchedule");
      Pair<ImmutableList<LocalDate>, ImmutableList<Double>> notionalSchedule =
          parseSchedule(notionalScheduleEl, effectiveDate);
      Currency currency = parseCurrency(notionalScheduleEl.getChildSingle("currency"));

      // day count
      DayCount dayCount = parseDayCountFraction(calcEl.getChildSingle("dayCountFraction"));

      // stubs
      StubCalculationMethod stub = null;
      if (firstRegularStartDate != null && lastRegularEndDate != null) {
        Builder stubBuilder = StubCalculationMethod.builder();
        parseStubCalculations(legEl, stubBuilder);
        stub = stubBuilder
            .type(StubType.BOTH)
            .firstStubEndDate(firstRegularStartDate)
            .lastStubEndDate(lastRegularEndDate)
            .build();
      } else if (firstRegularStartDate != null) {
        Builder stubBuilder = StubCalculationMethod.builder();
        parseStubCalculations(legEl, stubBuilder);
        stub = stubBuilder
            .type(stubConvention != StubType.NONE ? stubConvention : StubType.SHORT_START)
            .firstStubEndDate(firstRegularStartDate)
            .build();
      } else if (lastRegularEndDate != null) {
        Builder stubBuilder = StubCalculationMethod.builder();
        parseStubCalculations(legEl, stubBuilder);
        stub = stubBuilder
            .type(stubConvention != StubType.NONE ? stubConvention : StubType.SHORT_END)
            .lastStubEndDate(lastRegularEndDate)
            .build();
      }

      // build
      FixedInterestRateSwapLeg leg = new FixedInterestRateSwapLeg();
      leg.setNotional(InterestRateSwapNotional.of(currency, notionalSchedule.getFirst(), notionalSchedule.getSecond()));
      leg.setPayReceiveType(payReceive);
      if (stub != null) {
        leg.setStubCalculationMethod(stub);
      }
      leg.setDayCountConvention(dayCount);
      leg.setRollConvention(rollConvention);
      leg.setMaturityDateCalendars(terminationDateAdj.getSecond());
      leg.setMaturityDateBusinessDayConvention(terminationDateAdj.getFirst());
      leg.setPaymentDateCalendars(payAdj.getSecond());
      leg.setPaymentDateBusinessDayConvention(payAdj.getFirst());
      leg.setPaymentDateFrequency(paymentFreq);
      leg.setPaymentDateRelativeTo(payRelativeTo);
      leg.setPaymentOffset(payOffsetDays);
      leg.setAccrualPeriodCalendars(calcBda.getSecond());
      leg.setAccrualPeriodBusinessDayConvention(calcBda.getFirst());
      leg.setAccrualPeriodFrequency(accrualFreq);
      leg.setCompoundingMethod(compoundingMethod);
      legs.add(parseFixedFloat(legEl, calcEl, effectiveDate, leg));
    }
    InterestRateSwapSecurity swapSecurity = new InterestRateSwapSecurity(
        ExternalIdBundle.EMPTY, "Parsed from FpML", effectiveDate, terminationDate, legs);
    swapSecurity.setNotionalExchange(notionalExchange);
    Trade trade = new SimpleTrade(
        swapSecurity,
        BigDecimal.ONE,
        new SimpleCounterparty(counterparty),
        tradeDate,
        OffsetTime.of(LocalTime.MIDNIGHT, ZoneOffset.UTC));
    return new InterestRateSwapTrade(trade);
  }

  // interpret based on fixed or float
  private InterestRateSwapLeg parseFixedFloat(
      XmlElement legEl, XmlElement calcEl, LocalDate effectiveDate, FixedInterestRateSwapLeg leg) {
    Optional<XmlElement> fixedOptEl = calcEl.getChildOptional("fixedRateSchedule");
    Optional<XmlElement> floatingOptEl = calcEl.getChildOptional("floatingRateCalculation");

    if (fixedOptEl.isPresent()) {
      // fixed
      Pair<ImmutableList<LocalDate>, ImmutableList<Double>> fixedRates = parseSchedule(fixedOptEl.get(), effectiveDate);
      if (fixedRates.getFirst().size() != 1) {
        throw new FpmlParseException("Fixed leg must have a single interest rate");
      }
      leg.setRate(new Rate(fixedRates.getSecond().get(0)));
      return leg;
    }
    
    if (!floatingOptEl.isPresent()) {
      throw new FpmlParseException("Invalid 'calculation' type, not fixedRateSchedule or floatingRateCalculation");
    }
    XmlElement floatingEl = floatingOptEl.get();
    validateNotPresent(floatingEl, "rateTreatment");
    validateNotPresent(floatingEl, "capRateSchedule");
    validateNotPresent(floatingEl, "floorRateSchedule");
    
    FloatingInterestRateSwapLeg floating = new FloatingInterestRateSwapLeg();
    floating.setNotional(leg.getNotional());
    floating.setPayReceiveType(leg.getPayReceiveType());
    floating.setStubCalculationMethod(leg.getStubCalculationMethod());
    floating.setDayCountConvention(leg.getDayCountConvention());
    floating.setRollConvention(leg.getRollConvention());
    floating.setMaturityDateCalendars(leg.getMaturityDateCalendars());
    floating.setMaturityDateBusinessDayConvention(leg.getMaturityDateBusinessDayConvention());
    floating.setPaymentDateCalendars(leg.getPaymentDateCalendars());
    floating.setPaymentDateBusinessDayConvention(leg.getPaymentDateBusinessDayConvention());
    floating.setPaymentDateFrequency(leg.getPaymentDateFrequency());
    floating.setPaymentDateRelativeTo(leg.getPaymentDateRelativeTo());
    floating.setPaymentOffset(leg.getPaymentOffset());
    floating.setAccrualPeriodCalendars(leg.getAccrualPeriodCalendars());
    floating.setAccrualPeriodBusinessDayConvention(leg.getAccrualPeriodBusinessDayConvention());
    floating.setAccrualPeriodFrequency(leg.getAccrualPeriodFrequency());
    floating.setCompoundingMethod(leg.getCompoundingMethod());

    // index
    ExternalId indexId = parseIndexId(floatingEl);
    floating.setFloatingReferenceRateId(indexId);
    if (indexId.getValue().contains("OIS") || indexId.getValue().contains("COMPOUND")) {
      floating.setFloatingRateType(FloatingRateType.OIS);
    } else if (indexId.getValue().startsWith("USD-Federal Funds-H.15")) {
      floating.setFloatingRateType(FloatingRateType.OVERNIGHT_ARITHMETIC_AVERAGE);
    } else {
      floating.setFloatingRateType(FloatingRateType.IBOR);
    }
    // gearing
    Optional<XmlElement> gearingOptEl = floatingEl.getChildOptional("floatingRateMultiplierSchedule");
    if (gearingOptEl.isPresent()) {
      Pair<ImmutableList<LocalDate>, ImmutableList<Double>> gearings = parseSchedule(gearingOptEl.get(), effectiveDate);
      if (gearings.getFirst().size() != 1) {
        throw new FpmlParseException("Floating leg must have a single gearing");
      }
      floating.setGearing(gearings.getSecond().get(0));
    }
    // spread
    if (floatingEl.getChildren("spreadSchedule").size() > 1) {
      throw new FpmlParseException("Only one 'spreadSchedule' is supported");
    }
    Optional<XmlElement> spreadOptEl = floatingEl.getChildOptional("spreadSchedule");
    if (spreadOptEl.isPresent()) {
      validateNotPresent(spreadOptEl.get(), "type");
      Pair<ImmutableList<LocalDate>, ImmutableList<Double>> spreads = parseSchedule(spreadOptEl.get(), effectiveDate);
      if (spreads.getFirst().size() != 1) {
        // TODO: could try to create period index from dates
        throw new FpmlParseException("Floating leg must have a single spread");
      }
      floating.setSpreadSchedule(new Rate(spreads.getSecond().get(0)));
    }
    // initial fixed rate
    Optional<XmlElement> initialRateOptEl = floatingEl.getChildOptional("initialRate");
    if (initialRateOptEl.isPresent()) {
      floating.setCustomRates(new Rate(parseDecimal(initialRateOptEl.get())));
    }
    // averaging method
    Optional<XmlElement> avgMethodOptEl = floatingEl.getChildOptional("averagingMethod");
    if (avgMethodOptEl.isPresent()) {
      floating.setRateAveragingMethod(parseAveragingMethod(avgMethodOptEl.get()));
    }
    // resets
    XmlElement resetDatesEl = legEl.getChildSingle("resetDates");
    validateNotPresent(resetDatesEl, "initialFixingDate");
    // fixing date offset
    XmlElement fixingDatesEl = resetDatesEl.getChildSingle("fixingDates");
    floating.setFixingDateOffset(parseRelativeDateOffsetDays(fixingDatesEl));
    floating.setFixingDateCalendars(parseBusinessCenters(fixingDatesEl));
    floating.setFixingDateBusinessDayConvention(FpmlConversions.businessDayConvention(
        fixingDatesEl.getChildSingle("businessDayConvention").getContent()));
    Optional<XmlElement> dayTypeEl = fixingDatesEl.getChildOptional("dayType");
    boolean calendarDays = floating.getFixingDateOffset() == 0 ||
        (dayTypeEl.isPresent() && dayTypeEl.get().getContent().equals("Calendar"));
    floating.setFixingDateOffsetType(calendarDays ? OffsetType.CALENDAR : OffsetType.BUSINESS);
    // reset frequency and adjustment
    Optional<XmlElement> resetRelativeOptEl = resetDatesEl.getChildOptional("resetRelativeTo");
    if (resetRelativeOptEl.isPresent()) {
      floating.setResetDateRelativeTo(parseDateRelativeTo(resetRelativeOptEl.get()));
    }
    Frequency resetFreq = parseFrequency(resetDatesEl.getChildSingle("resetFrequency"));
    floating.setResetPeriodFrequency(resetFreq);
    Pair<BusinessDayConvention, Set<ExternalId>> resetAdj =
        parseBusinessDayAdjustments(resetDatesEl.getChildSingle("resetDatesAdjustments"));
    floating.setResetPeriodBusinessDayConvention(resetAdj.getFirst());
    floating.setResetPeriodCalendars(resetAdj.getSecond());
    
    // rate cut off
    validateNotPresent(resetDatesEl, "initialFixingDate");
    Optional<XmlElement> cutOffOptEl = resetDatesEl.getChildOptional("rateCutOffDaysOffset");
    if (cutOffOptEl.isPresent()) {
      Period cutOff = parsePeriod(cutOffOptEl.get());
      if (cutOff.toTotalMonths() != 0) {
        throw new FpmlParseException("Invalid 'rateCutOffDaysOffset' value, expected days-based period: " + cutOff);
      }
      floating.setRateCutoffDaysOffset(-cutOff.getDays());
    }
    return floating;
  }

  //-------------------------------------------------------------------------
  // Converts an FpML 'StubValue' to a {@code StubCalculation}.
  private void parseStubCalculations(XmlElement legEl, StubCalculationMethod.Builder builder) {
    Optional<XmlElement> stubCalcOptEl = legEl.getChildOptional("stubCalculationPeriodAmount");
    if (stubCalcOptEl.isPresent()) {
      Optional<XmlElement> initialStubOptEl = stubCalcOptEl.get().getChildOptional("initialStub");
      if (initialStubOptEl.isPresent()) {
        XmlElement initialStubEl = initialStubOptEl.get();
        validateNotPresent(initialStubEl, "stubAmount");
        Optional<XmlElement> rateOptEl = initialStubEl.getChildOptional("stubRate");
        if (rateOptEl.isPresent()) {
          builder.firstStubRate(parseDecimal(rateOptEl.get()));
        } else {
          List<XmlElement> indicesEls = initialStubEl.getChildren("floatingRate");
          if (indicesEls.size() == 0 || indicesEls.size() > 2) {
            throw new FpmlParseException("Unknown stub structure: " + initialStubEl);
          }
          XmlElement index1El = indicesEls.get(0);
          validateNotPresent(index1El, "floatingRateMultiplierSchedule");
          validateNotPresent(index1El, "spreadSchedule");
          validateNotPresent(index1El, "rateTreatment");
          validateNotPresent(index1El, "capRateSchedule");
          validateNotPresent(index1El, "floorRateSchedule");
          builder.firstStubStartReferenceRateId(parseIndexId(index1El));
          if (indicesEls.size() == 2) {
            XmlElement index2El = indicesEls.get(1);
            validateNotPresent(index2El, "floatingRateMultiplierSchedule");
            validateNotPresent(index2El, "spreadSchedule");
            validateNotPresent(index2El, "rateTreatment");
            validateNotPresent(index2El, "capRateSchedule");
            validateNotPresent(index2El, "floorRateSchedule");
            builder.firstStubEndReferenceRateId(parseIndexId(index2El));
          }
        }
      }
      Optional<XmlElement> finalStubOptEl = stubCalcOptEl.get().getChildOptional("finalStub");
      if (finalStubOptEl.isPresent()) {
        XmlElement finalStubEl = finalStubOptEl.get();
        validateNotPresent(finalStubEl, "stubAmount");
        Optional<XmlElement> rateOptEl = finalStubEl.getChildOptional("stubRate");
        if (rateOptEl.isPresent()) {
          builder.lastStubRate(parseDecimal(rateOptEl.get()));
        } else {
          List<XmlElement> indicesEls = finalStubEl.getChildren("floatingRate");
          if (indicesEls.size() == 0 || indicesEls.size() > 2) {
            throw new FpmlParseException("Unknown stub structure: " + finalStubEl);
          }
          XmlElement index1El = indicesEls.get(0);
          validateNotPresent(index1El, "floatingRateMultiplierSchedule");
          validateNotPresent(index1El, "spreadSchedule");
          validateNotPresent(index1El, "rateTreatment");
          validateNotPresent(index1El, "capRateSchedule");
          validateNotPresent(index1El, "floorRateSchedule");
          builder.lastStubStartReferenceRateId(parseIndexId(index1El));
          if (indicesEls.size() == 2) {
            XmlElement index2El = indicesEls.get(1);
            validateNotPresent(index2El, "floatingRateMultiplierSchedule");
            validateNotPresent(index2El, "spreadSchedule");
            validateNotPresent(index2El, "rateTreatment");
            validateNotPresent(index2El, "capRateSchedule");
            validateNotPresent(index2El, "floorRateSchedule");
            builder.lastStubEndReferenceRateId(parseIndexId(index2El));
          }
        }
      }
    }
  }

  // Converts an FpML 'StubPeriodTypeEnum' to a {@code StubConvention}.
  private StubType parseStubConvention(XmlElement baseEl) {
    if (baseEl.getContent().equals("ShortInitial")) {
      return StubType.SHORT_START;
    } else if (baseEl.getContent().equals("ShortFinal")) {
      return StubType.SHORT_END;
    } else if (baseEl.getContent().equals("LongInitial")) {
      return StubType.LONG_START;
    } else if (baseEl.getContent().equals("LongFinal")) {
      return StubType.LONG_END;
    } else {
      throw new FpmlParseException("Unknown 'stubPeriodType': " + baseEl.getContent());
    }
  }

  // Converts an FpML 'CompoundingMethodEnum' to a {@code CompoundingMethod}.
  private CompoundingMethod parseCompoundingMethod(XmlElement baseEl) {
    if (baseEl.getContent().equals("None")) {
      return CompoundingMethod.NONE;
    } else if (baseEl.getContent().equals("Flat")) {
      return CompoundingMethod.FLAT;
    } else if (baseEl.getContent().equals("Straight")) {
      return CompoundingMethod.STRAIGHT;
    } else if (baseEl.getContent().equals("SpreadExclusive")) {
      return CompoundingMethod.SPREAD_EXCLUSIVE;
    } else {
      throw new FpmlParseException("Unknown 'compoundingMethod': " + baseEl.getContent());
    }
  }

  // Converts an FpML 'PayRelativeToEnum' or 'ResetRelativeToEnum' to a {@code PaymentRelativeTo}.
  private DateRelativeTo parseDateRelativeTo(XmlElement baseEl) {
    if (baseEl.getContent().equals("CalculationPeriodStartDate")) {
      return DateRelativeTo.START;
    } else if (baseEl.getContent().equals("CalculationPeriodEndDate")) {
      return DateRelativeTo.END;
    } else {
      throw new FpmlParseException("Unknown 'payRelativeTo': " + baseEl.getContent());
    }
  }

  // Converts an FpML 'AveragingMethodEnum' to a {@code IborRateAveragingMethod}.
  private RateAveragingMethod parseAveragingMethod(XmlElement baseEl) {
    if (baseEl.getContent().equals("Unweighted")) {
      return RateAveragingMethod.UNWEIGHTED;
    } else if (baseEl.getContent().equals("Weighted")) {
      return RateAveragingMethod.WEIGHTED;
    } else {
      throw new FpmlParseException("Unknown 'averagingMethod': " + baseEl.getContent());
    }
  }

  //-------------------------------------------------------------------------
  // helper methods for FpML types
  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'Schedule' to a {@code ValueSchedule}.
   * 
   * @param baseEl  the FpML schedule element
   * @param effectiveDate  the effective date
   * @return the schedule
   */
  private Pair<ImmutableList<LocalDate>, ImmutableList<Double>> parseSchedule(
      XmlElement notionalScheduleEl, LocalDate effectiveDate) {
    // FpML content: ('initialValue', 'step*')
    // FpML 'step' content: ('stepDate', 'stepValue')
    double initialValue = parseDecimal(notionalScheduleEl.getChildSingle("initialValue"));
    List<XmlElement> stepEls = notionalScheduleEl.getChildren("step");
    ImmutableList.Builder<LocalDate> dateBuilder = ImmutableList.builder();
    ImmutableList.Builder<Double> valueBuilder = ImmutableList.builder();
    dateBuilder.add(effectiveDate);
    valueBuilder.add(initialValue);
    for (XmlElement stepEl : stepEls) {
      dateBuilder.add(parseDate(stepEl.getChildSingle("stepDate")));
      valueBuilder.add(parseDecimal(stepEl.getChildSingle("stepValue")));
    }
    return Pairs.of(dateBuilder.build(), valueBuilder.build());
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'RelativeDateOffset' to an {@code int}.
   * 
   * @param baseEl  the FpML adjustable date element
   * @return the days
   * @throws FpmlParseException if the holiday calendar is not known
   */
  private int parseRelativeDateOffsetDays(XmlElement baseEl) {
    // FpML content: ('periodMultiplier', 'period', 'dayType?',
    //                'businessDayConvention', 'BusinessCentersOrReference.model?'
    //                'dateRelativeTo', 'adjustedDate')
    // The 'dateRelativeTo' element is not used here
    // The 'adjustedDate' element is ignored
    Period period = parsePeriod(baseEl);
    if (period.toTotalMonths() != 0) {
      throw new FpmlParseException("Expected days-based period but found " + period);
    }
    Optional<XmlElement> dayTypeEl = baseEl.getChildOptional("dayType");
    if (dayTypeEl.isPresent() && dayTypeEl.get().getContent().equals("Calendar")) {
      throw new FpmlParseException("Only business day adjustments are supported");
    }
    return period.getDays();
  }

  /**
   * Converts an FpML 'RelativeDateOffset' to a {@code DaysAdjustment}.
   * 
   * @param baseEl  the FpML adjustable date element
   * @return the days adjustment
   * @throws FpmlParseException if the holiday calendar is not known
   */
  private Pair<BusinessDayConvention, Set<ExternalId>> parseRelativeDateOffsetDaysAdjustment(XmlElement baseEl) {
    // FpML content: ('periodMultiplier', 'period', 'dayType?',
    //                'businessDayConvention', 'BusinessCentersOrReference.model?'
    //                'dateRelativeTo', 'adjustedDate')
    // The 'dateRelativeTo' element is not used here
    // The 'adjustedDate' element is ignored
    Period period = parsePeriod(baseEl);
    if (period.toTotalMonths() != 0) {
      throw new FpmlParseException("Expected days-based period but found " + period);
    }
    Optional<XmlElement> dayTypeEl = baseEl.getChildOptional("dayType");
    if (dayTypeEl.isPresent() && dayTypeEl.get().getContent().equals("Calendar")) {
      throw new FpmlParseException("Only business day adjustments are supported");
    }
    BusinessDayConvention bdc = FpmlConversions.businessDayConvention(
        baseEl.getChildSingle("businessDayConvention").getContent());
    Set<ExternalId> calendar = parseBusinessCenters(baseEl);
    return Pairs.of(bdc, calendar);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'AdjustableDate' to an {@code AdjustableDate}.
   * 
   * @param baseEl  the FpML adjustable date element
   * @return the adjustable date
   * @throws FpmlParseException if the holiday calendar is not known
   */
  private Pair<LocalDate, Pair<BusinessDayConvention, Set<ExternalId>>> parseAdjustableDate(XmlElement baseEl) {
    // FpML content: ('unadjustedDate', 'dateAdjustments', 'adjustedDate?')
    // The 'adjustedDate' element is ignored
    LocalDate unadjustedDate = parseDate(baseEl.getChildSingle("unadjustedDate"));
    Pair<BusinessDayConvention, Set<ExternalId>> adj = parseBusinessDayAdjustments(baseEl.getChildSingle("dateAdjustments"));
    return Pairs.of(unadjustedDate, adj);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'BusinessDayAdjustments' to a {@code BusinessDayAdjustment}.
   * 
   * @param baseEl  the FpML business centers or reference element to parse 
   * @return the business day adjustment
   * @throws FpmlParseException if the holiday calendar is not known
   */
  private Pair<BusinessDayConvention, Set<ExternalId>> parseBusinessDayAdjustments(XmlElement baseEl) {
    // FpML content: ('businessDayConvention', 'BusinessCentersOrReference.model?')
    BusinessDayConvention bdc = FpmlConversions.businessDayConvention(
        baseEl.getChildSingle("businessDayConvention").getContent());
    Optional<XmlElement> centersEl = baseEl.getChildOptional("businessCenters");
    Optional<XmlElement> centersRefEl = baseEl.getChildOptional("businessCentersReference");
    Set<ExternalId> calendars = centersEl.isPresent() || centersRefEl.isPresent() ?
        parseBusinessCenters(baseEl) : new HashSet<ExternalId>();
    return Pairs.of(bdc, calendars);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'BusinessCentersOrReference.model' to a {@code Set<ExternalId>}.
   * 
   * @param baseEl  the FpML business centers or reference element to parse 
   * @return the holiday calendar
   * @throws FpmlParseException if the holiday calendar is not known
   */
  private Set<ExternalId> parseBusinessCenters(final XmlElement baseEl) {
    // FpML content: ('businessCentersReference' | 'businessCenters')
    // FpML 'businessCenters' content: ('businessCenter+')
    // Each 'businessCenter' is a location treated as a holiday calendar
    Optional<XmlElement> optionalBusinessCentersEl = baseEl.getChildOptional("businessCenters");
    XmlElement businessCentersEl;
    if (!optionalBusinessCentersEl.isPresent()) {
      businessCentersEl = lookupReference(baseEl.getChildSingle("businessCentersReference"));
    } else {
      businessCentersEl = optionalBusinessCentersEl.get();
    }
    Set<ExternalId> calendars = new HashSet<>();
    for (XmlElement businessCenterEl : businessCentersEl.getChildren("businessCenter")) {
      calendars.add(parseBusinessCenter(businessCenterEl));
    }
    return calendars;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'BusinessCenter' to an {@code ExternalId}.
   * 
   * @param baseEl  the FpML calendar element to parse 
   * @return the calendar
   * @throws FpmlParseException if the calendar is not known
   */
  private ExternalId parseBusinessCenter(XmlElement baseEl) {
    validateScheme(baseEl, "businessCenterScheme", "http://www.fpml.org/coding-scheme/business-center");
    return ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, baseEl.getContent());
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'FloatingRateIndex.model' to an {@code Index}.
   * 
   * @param baseEl  the FpML floating rate index element to parse 
   * @return the index
   */
  private ExternalId parseIndexId(XmlElement baseEl) {
    XmlElement indexEl = baseEl.getChildSingle("floatingRateIndex");
    validateScheme(indexEl, "floatingRateIndexScheme", "http://www.fpml.org/coding-scheme/floating-rate-index");
    Optional<XmlElement> tenorEl = baseEl.getChildOptional("indexTenor");
    if (tenorEl.isPresent()) {
      PeriodFrequency freq = parseFrequency(tenorEl.get());
      return ExternalId.of(ExternalSchemes.ISDA, indexEl.getContent() + "-" + freq.getPeriod().toString().substring(1));
    } else {
      return ExternalId.of(ExternalSchemes.ISDA, indexEl.getContent());
    }
  }

  /**
   * Converts an FpML 'FloatingRateIndex' with multiple tenors to an {@code Index}.
   * 
   * @param baseEl  the FpML floating rate index element to parse 
   * @return the index frequency
   */
  private PeriodFrequency parseIndexFrequency(XmlElement baseEl) {
    XmlElement tenorEl = baseEl.getChildSingle("indexTenor");
    return parseFrequency(tenorEl);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'Period' to a {@code Period}.
   * 
   * @param baseEl  the FpML business centers or reference element to parse 
   * @return the period
   */
  private Period parsePeriod(XmlElement baseEl) {
    // FpML content: ('periodMultiplier', 'period')
    String multiplier = baseEl.getChildSingle("periodMultiplier").getContent();
    String unit = baseEl.getChildSingle("period").getContent();
    return Period.parse("P" + multiplier + unit);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML frequency to a {@code Frequency}.
   * 
   * @param baseEl  the FpML business centers or reference element to parse 
   * @return the frequency
   */
  private PeriodFrequency parseFrequency(XmlElement baseEl) {
    // FpML content: ('periodMultiplier', 'period')
    String multiplier = baseEl.getChildSingle("periodMultiplier").getContent();
    String unit = baseEl.getChildSingle("period").getContent();
    if (unit.equals("T")) {
      return PeriodFrequency.NEVER;
    }
    return PeriodFrequency.of(Period.parse("P" + multiplier + unit));
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'Money' to a {@code CurrencyAmount}.
   * 
   * @param baseEl  the FpML money element to parse 
   * @return the currency amount
   * @throws FpmlParseException if the currency is not known
   */
  private CurrencyAmount parseCurrencyAmount(XmlElement baseEl) {
    // FpML content: ('currency', 'amount')
    Currency currency = parseCurrency(baseEl.getChildSingle("currency"));
    double amount = parseDecimal(baseEl.getChildSingle("amount"));
    return CurrencyAmount.of(currency, amount);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'Currency' to a {@code Currency}.
   * 
   * @param baseEl  the FpML currency element to parse 
   * @return the currency
   * @throws FpmlParseException if the currency is not known
   */
  private Currency parseCurrency(XmlElement baseEl) {
    validateScheme(baseEl, "currencyScheme", "http://www.fpml.org/coding-scheme/external/iso4217");
    return Currency.of(baseEl.getContent());
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'DayCountFraction' to a {@code DayCount}.
   * 
   * @param baseEl  the FpML day count element to parse 
   * @return the day count
   * @throws FpmlParseException if the day count is not known
   */
  private DayCount parseDayCountFraction(XmlElement baseEl) {
    validateScheme(baseEl, "dayCountFractionScheme", "http://www.fpml.org/coding-scheme/day-count-fraction");
    return FpmlConversions.dayCount(baseEl.getContent());
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'decimal' to a {@code double}.
   * 
   * @param baseEl  the FpML element to parse 
   * @return the double
   * @throws FpmlParseException if the double is invalid
   */
  private double parseDecimal(XmlElement baseEl) {
    try {
      return Double.parseDouble(baseEl.getContent());
    } catch (NumberFormatException ex) {
      throw new FpmlParseException("Invalid number in '" + baseEl.getName() + "': " + ex.getMessage());
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Converts an FpML 'date' to a {@code LocalDate}.
   * 
   * @param baseEl  the FpML element to parse 
   * @return the date
   * @throws FpmlParseException if the date is invalid
   */
  private LocalDate parseDate(XmlElement baseEl) {
    try {
      return FpmlConversions.date(baseEl.getContent());
    } catch (DateTimeParseException ex) {
      throw new FpmlParseException("Invalid date in '" + baseEl.getName() + "': " + ex.getMessage());
    }
  }

  //-------------------------------------------------------------------------
  // validate that a specific element is not present
  private void validateNotPresent(XmlElement baseEl, String elementName) {
    if (baseEl.getChildOptional(elementName).isPresent()) {
      throw new FpmlParseException("Unsupported element: '" + elementName + "'");
    }
  }

  // validates that the scheme attribute is known
  private void validateScheme(XmlElement baseEl, String schemeAttr, String schemeValue) {
    if (baseEl.getAttributes().containsKey(schemeAttr)) {
      String scheme = baseEl.getAttribute(schemeAttr);
      if (!scheme.startsWith(schemeValue)) {
        throw new FpmlParseException("Unknown '" + schemeAttr + "' attribute value: " + scheme);
      }
    }
  }

  //-------------------------------------------------------------------------
  // locate our party href/id reference
  private String findOurParty(String ourParty) {
    for (Entry<String, String> entry : _parties.entries()) {
      if (ourParty.equals(entry.getValue())) {
        return entry.getKey();
      }
    }
    throw new FpmlParseException(
        "Document does not contain our party ID: " + ourParty + " not found in " + _parties);
  }

  // lookup an element via href/id reference
  private XmlElement lookupReference(XmlElement hrefEl) {
    String hrefId = hrefEl.getAttribute(HREF);
    XmlElement el = _refs.get(hrefId);
    if (el == null) {
      throw new FpmlParseException("Document reference not found: href='" + hrefId + "'");
    }
    return el;
  }

}
