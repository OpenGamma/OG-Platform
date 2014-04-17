/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import static com.opengamma.bbg.BloombergConstants.DATA_PROVIDER_UNKNOWN;
import static com.opengamma.bbg.BloombergConstants.DEFAULT_DATA_PROVIDER;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_CHAIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_CHAIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.ON_OFF_FIELDS;
import static org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH;
import static org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR;
import static org.threeten.bp.temporal.ChronoField.YEAR;
import static org.threeten.bp.temporal.ChronoUnit.MONTHS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.ehcache.CacheManager;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Schema.Datatype;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.historical.normalization.BloombergFixedRateHistoricalTimeSeriesNormalizer;
import com.opengamma.bbg.historical.normalization.BloombergRateHistoricalTimeSeriesNormalizer;
import com.opengamma.bbg.livedata.normalization.BloombergRateRuleProvider;
import com.opengamma.bbg.normalization.BloombergRateClassifier;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataError;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesConstants;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.core.value.MarketDataRequirementNamesHelper;
import com.opengamma.engine.marketdata.availability.DomainMarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.financial.analytics.ircurve.NextMonthlyExpiryAdjuster;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.normalization.FieldFilter;
import com.opengamma.livedata.normalization.FieldHistoryUpdater;
import com.opengamma.livedata.normalization.FieldNameChange;
import com.opengamma.livedata.normalization.ImpliedVolatilityCalculator;
import com.opengamma.livedata.normalization.MarketValueCalculator;
import com.opengamma.livedata.normalization.NormalizationRule;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.normalization.RequiredFieldFilter;
import com.opengamma.livedata.normalization.SecurityRuleApplier;
import com.opengamma.livedata.normalization.SecurityRuleProvider;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.normalization.UnitChange;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustmentMap;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.impl.PositionSearchIterator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Utilities for working with data in the Bloomberg schema.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class BloombergDataUtils {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergDataUtils.class);

  private static final Pattern s_bloombergTickerPattern = buildPattern();

  private static final TemporalAdjuster s_monthlyExpiryAdjuster = new NextMonthlyExpiryAdjuster();

  /**
   * The standard fields required for Bloomberg data, as a list.
   */
  public static final List<String> STANDARD_FIELDS_LIST = ImmutableList.of("BID",
      "ASK",
      "LAST_PRICE",
      "PX_SETTLE",
      "VOLUME",
      "OPT_IMPLIED_VOLATILITY_BID_RT",
      "OPT_IMPLIED_VOLATILITY_ASK_RT",
      "OPT_IMPLIED_VOLATILITY_LAST_RT",
      "OPT_IMPLIED_VOLATILITY_MID_RT",
      "YLD_CNV_MID", //TODO BBG-96
      "YLD_YTM_MID", //TODO BBG-96
      "PX_DIRTY_MID", //TODO BBG-96
      "EQY_DVD_YLD_EST");

  /**
   * The standard fields required for Bloomberg data, as a set.
   */
  public static final Set<String> STANDARD_FIELDS_SET = new ImmutableSet.Builder<String>().addAll(STANDARD_FIELDS_LIST).build();

  /**
   * Map from RIC to BBG prefixes, for exceptions only
   */
  private static final Map<String, String> s_ricToBbgPrefixMap;
  static {
    s_ricToBbgPrefixMap = new HashMap<String, String>();
    s_ricToBbgPrefixMap.put("EDD", "FD");
    s_ricToBbgPrefixMap.put("EDM", "0D");
    s_ricToBbgPrefixMap.put("EDD", "FD");
    s_ricToBbgPrefixMap.put("FEI", "ER");
    s_ricToBbgPrefixMap.put("FME", "0R");
    s_ricToBbgPrefixMap.put("2FME", "2R");
    s_ricToBbgPrefixMap.put("FSS", "L ");
    s_ricToBbgPrefixMap.put("FMS", "0L");
    s_ricToBbgPrefixMap.put("2FMS", "2L");
    s_ricToBbgPrefixMap.put("FES", "ES");
  }

  /**
   * Number format to use in BBC strike price
   */
  private static final DecimalFormat THREE_DECIMAL_PLACES = new DecimalFormat("#0.000");

  /**
   * Map from month to BBG month code
   */
  private static final BiMap<Month, String> s_monthCode;
  static {
    s_monthCode = HashBiMap.create();
    s_monthCode.put(Month.JANUARY, "F");
    s_monthCode.put(Month.FEBRUARY, "G");
    s_monthCode.put(Month.MARCH, "H");
    s_monthCode.put(Month.APRIL, "J");
    s_monthCode.put(Month.MAY, "K");
    s_monthCode.put(Month.JUNE, "M");
    s_monthCode.put(Month.JULY, "N");
    s_monthCode.put(Month.AUGUST, "Q");
    s_monthCode.put(Month.SEPTEMBER, "U");
    s_monthCode.put(Month.OCTOBER, "V");
    s_monthCode.put(Month.NOVEMBER, "X");
    s_monthCode.put(Month.DECEMBER, "Z");
  }

  /**
   * The observation time map.
   */
  private static final Map<String, String> s_observationTimeMap = ImmutableMap.<String, String>builder()
      .put("CMPL", HistoricalTimeSeriesConstants.LONDON_CLOSE)
      .put("CMPT", HistoricalTimeSeriesConstants.TOKYO_CLOSE)
      .put("CMPN", HistoricalTimeSeriesConstants.NEWYORK_CLOSE)
      .put(DEFAULT_DATA_PROVIDER, HistoricalTimeSeriesConstants.DEFAULT_OBSERVATION_TIME)
      .build();

  /**
   * Restricted constructor.
   */
  private BloombergDataUtils() {
  }

  private static Pattern buildPattern() {
    if (BloombergConstants.MARKET_SECTORS.isEmpty()) {
      throw new OpenGammaRuntimeException("Bloomberg market sectors can not be empty");
    }
    final List<String> marketSectorList = Lists.newArrayList(BloombergConstants.MARKET_SECTORS);
    String sectorPattern = marketSectorList.get(0);
    for (int i = 1; i < marketSectorList.size(); i++) {
      sectorPattern += "|" + marketSectorList.get(i);
    }
    final Pattern bloombergTickerPattern = Pattern.compile(String.format("^(.+)(\\s+)((%s))$", sectorPattern), Pattern.CASE_INSENSITIVE);
    return bloombergTickerPattern;
  }

  public static Collection<NormalizationRuleSet> getDefaultNormalizationRules(final ReferenceDataProvider referenceDataProvider, final CacheManager cacheManager, ExternalScheme bbgScheme) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");

    final Collection<NormalizationRuleSet> returnValue = new ArrayList<NormalizationRuleSet>();
    returnValue.add(StandardRules.getNoNormalization());

    final List<NormalizationRule> openGammaRules = new ArrayList<NormalizationRule>();

    // Filter out non-price updates
    openGammaRules.add(new FieldFilter(STANDARD_FIELDS_LIST));

    // Standardize field names.
    openGammaRules.add(new FieldNameChange("BID", MarketDataRequirementNames.BID));
    openGammaRules.add(new FieldNameChange("ASK", MarketDataRequirementNames.ASK));
    openGammaRules.add(new FieldNameChange("LAST_PRICE", MarketDataRequirementNames.LAST));
    openGammaRules.add(new FieldNameChange("PX_SETTLE", MarketDataRequirementNames.SETTLE_PRICE));
    openGammaRules.add(new FieldNameChange("VOLUME", MarketDataRequirementNames.VOLUME));
    openGammaRules.add(new FieldNameChange("OPT_IMPLIED_VOLATILITY_BID_RT", MarketDataRequirementNames.BID_IMPLIED_VOLATILITY));
    openGammaRules.add(new FieldNameChange("OPT_IMPLIED_VOLATILITY_ASK_RT", MarketDataRequirementNames.ASK_IMPLIED_VOLATILITY));
    openGammaRules.add(new FieldNameChange("OPT_IMPLIED_VOLATILITY_LAST_RT", MarketDataRequirementNames.LAST_IMPLIED_VOLATILITY));
    openGammaRules.add(new FieldNameChange("OPT_IMPLIED_VOLATILITY_MID_RT", MarketDataRequirementNames.MID_IMPLIED_VOLATILITY));
    openGammaRules.add(new FieldNameChange("YLD_CNV_MID", MarketDataRequirementNames.YIELD_CONVENTION_MID));
    openGammaRules.add(new FieldNameChange("YLD_YTM_MID", MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID));
    openGammaRules.add(new FieldNameChange("PX_DIRTY_MID", MarketDataRequirementNames.DIRTY_PRICE_MID));
    openGammaRules.add(new FieldNameChange("EQY_DVD_YLD_EST", MarketDataRequirementNames.DIVIDEND_YIELD));

    // Calculate market value
    openGammaRules.add(new MarketValueCalculator());

    // Normalize the market value
    if (referenceDataProvider != null) {
      final BloombergRateClassifier rateClassifier = new BloombergRateClassifier(referenceDataProvider, cacheManager, bbgScheme);
      final SecurityRuleProvider quoteRuleProvider = new BloombergRateRuleProvider(rateClassifier);
      openGammaRules.add(new SecurityRuleApplier(quoteRuleProvider));
    }
    openGammaRules.add(new UnitChange(0.01, MarketDataRequirementNames.DIVIDEND_YIELD, MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID)); // returned as % from bbg

    // Calculate implied vol value
    openGammaRules.add(new ImpliedVolatilityCalculator());

    // At this point, BID, ASK, LAST, MARKET_VALUE, Volume and various Bloomberg implied vol fields are stored in the history.
    openGammaRules.add(new FieldHistoryUpdater());

    // Filter out non-OpenGamma fields (i.e., BID, ASK, various Bloomberg implied vol fields)
    openGammaRules.add(new FieldFilter(
        MarketDataRequirementNames.MARKET_VALUE,
        MarketDataRequirementNames.SETTLE_PRICE,
        MarketDataRequirementNames.VOLUME,
        MarketDataRequirementNames.IMPLIED_VOLATILITY,
        MarketDataRequirementNames.YIELD_CONVENTION_MID,
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID,
        MarketDataRequirementNames.DIRTY_PRICE_MID,
        MarketDataRequirementNames.DIVIDEND_YIELD));
    openGammaRules.add(new RequiredFieldFilter(MarketDataRequirementNames.MARKET_VALUE));

    final NormalizationRuleSet openGammaRuleSet = new NormalizationRuleSet(
        StandardRules.getOpenGammaRuleSetId(),
        "",
        openGammaRules);
    returnValue.add(openGammaRuleSet);

    return returnValue;
  }

  public static HistoricalTimeSeriesFieldAdjustmentMap createFieldAdjustmentMap(final ReferenceDataProvider referenceDataProvider, final CacheManager cacheManager) {
    final HistoricalTimeSeriesFieldAdjustmentMap fieldAdjustmentMap = new HistoricalTimeSeriesFieldAdjustmentMap(BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME);
    final BloombergRateClassifier rateClassifier = new BloombergRateClassifier(referenceDataProvider, cacheManager, ExternalSchemes.BLOOMBERG_BUID);
    final HistoricalTimeSeriesAdjuster rateNormalizer = new BloombergRateHistoricalTimeSeriesNormalizer(rateClassifier);
    final BloombergFixedRateHistoricalTimeSeriesNormalizer div100 = new BloombergFixedRateHistoricalTimeSeriesNormalizer(new HistoricalTimeSeriesAdjustment.DivideBy(100.0));
    fieldAdjustmentMap.addFieldAdjustment(MarketDataRequirementNames.SETTLE_PRICE, null, BloombergConstants.BBG_FIELD_SETTLE_PRICE, rateNormalizer);
    fieldAdjustmentMap.addFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE, null, BloombergConstants.BBG_FIELD_LAST_PRICE, rateNormalizer);
    fieldAdjustmentMap.addFieldAdjustment(BloombergConstants.BBG_FIELD_LAST_PRICE, null, BloombergConstants.BBG_FIELD_LAST_PRICE, rateNormalizer); 
    fieldAdjustmentMap.addFieldAdjustment(MarketDataRequirementNames.VOLUME, null, BloombergConstants.BBG_FIELD_VOLUME, null);
    fieldAdjustmentMap.addFieldAdjustment(MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, null, BloombergConstants.BBG_FIELD_YIELD_TO_MATURITY_MID, null);
    fieldAdjustmentMap.addFieldAdjustment(MarketDataRequirementNames.DIVIDEND_YIELD, null, BloombergConstants.BBG_FIELD_DIVIDEND_YIELD, div100);
    return fieldAdjustmentMap;
  }

  public static MarketDataAvailabilityFilter createAvailabilityFilter() {
    final Set<ExternalScheme> acceptableSchemes = ImmutableSet.of(
        ExternalSchemes.BLOOMBERG_BUID_WEAK,
        ExternalSchemes.BLOOMBERG_BUID,
        ExternalSchemes.BLOOMBERG_TICKER_WEAK,
        ExternalSchemes.BLOOMBERG_TICKER);
    final Collection<String> validMarketDataRequirementNames = MarketDataRequirementNamesHelper.constructValidRequirementNames();
    return new DomainMarketDataAvailabilityFilter(acceptableSchemes, validMarketDataRequirementNames);
  }

  public static FudgeMsg parseElement(final Element element) {
    final MutableFudgeMsg fieldData = FudgeContext.GLOBAL_DEFAULT.newMessage();
    for (int iSubElement = 0; iSubElement < element.numElements(); iSubElement++) {
      final Element subElement = element.getElement(iSubElement);
      if (subElement.numValues() == 0) {
        continue;
      }
      final String name = subElement.elementDefinition().name().toString();
      final Object value = parseValue(subElement);
      if (value instanceof List<?>) {
        for (final Object obj : (List<?>) value) {
          fieldData.add(name, obj);
        }
      } else if (value != null) {
        fieldData.add(name, value);
      } else {
        s_logger.warn("Unable to extract value named {} from element {}", name, subElement);
      }
    }
    return fieldData;
  }

  /**
   * @param valueElement the value element
   * @return the parsed value
   */
  public static Object parseValue(final Element valueElement) {
    final Datatype datatype = valueElement.datatype();
    if (datatype == Datatype.STRING) {
      return valueElement.getValueAsString();
    } else if (datatype == Datatype.BOOL) {
      return valueElement.getValueAsBool();
    } else if (datatype == Datatype.BYTEARRAY) {  // CSIGNORE
      // REVIEW kirk 2009-10-22 -- How do we extract this? Intentionally fall through.
    } else if (datatype == Datatype.CHAR) {
      final char c = valueElement.getValueAsChar();
      return new String("" + c);
    } else if (datatype == Datatype.CHOICE) {  // CSIGNORE
      // REVIEW kirk 2009-10-22 -- How do we extract this? Intentionally fall through.
    } else if (datatype == Datatype.DATE) {
      final Datetime date = valueElement.getValueAsDate();
      return date.toString();
    } else if (datatype == Datatype.DATETIME) {
      // REVIEW kirk 2009-10-22 -- This is clearly wrong.
      final Datetime date = valueElement.getValueAsDatetime();
      return date.toString();
      //return date.calendar().getTime();
    } else if (datatype == Datatype.ENUMERATION) {
      return valueElement.getValueAsString();
    } else if (datatype == Datatype.FLOAT32) {
      return valueElement.getValueAsFloat32();
    } else if (datatype == Datatype.FLOAT64) {
      return valueElement.getValueAsFloat64();
    } else if (datatype == Datatype.INT32) {
      return valueElement.getValueAsInt32();
    } else if (datatype == Datatype.INT64) {
      return valueElement.getValueAsInt64();
    } else if (datatype == Datatype.TIME) {
      // REVIEW kirk 2009-10-22 -- This is clearly wrong.
      final Datetime date = valueElement.getValueAsDate();
      return date.toString();
      //return date.calendar().getTime();
    } else if (datatype == Datatype.SEQUENCE) {
      final int numValues = valueElement.numValues();
      final List<FudgeMsg> valueAsList = new ArrayList<FudgeMsg>(numValues);
      for (int i = 0; i < numValues; i++) {
        final Element sequenceElem = valueElement.getValueAsElement(i);
        final FudgeMsg sequenceElemAsMsg = parseElement(sequenceElem);
        valueAsList.add(sequenceElemAsMsg);
      }
      return valueAsList;
    }
    s_logger.warn("Unhandled Datatype of {}, data {}", datatype, valueElement);
    return null;
  }

  public static String getSingleBUID(final ReferenceDataProvider refDataProvider, final String securityDes) {
    final Map<String, String> ticker2buid = getBUID(refDataProvider, Collections.singleton(securityDes));
    return ticker2buid.get(securityDes);
  }

  public static Map<String, String> getBUID(final ReferenceDataProvider refDataProvider, final Set<String> bloombergKeys) {
    return refDataProvider.getReferenceDataValues(bloombergKeys, FIELD_ID_BBG_UNIQUE);
  }

  public static Set<String> getIndexMembers(final ReferenceDataProvider refDataProvider, final String indexTicker) {
    final Set<String> result = new TreeSet<String>();
    final Set<String> bbgFields = new HashSet<String>();
    bbgFields.add("INDX_MEMBERS");
    bbgFields.add("INDX_MEMBERS2");
    bbgFields.add("INDX_MEMBERS3");
    final ReferenceDataProviderGetRequest dataRequest = ReferenceDataProviderGetRequest.createGet(indexTicker, bbgFields, true);
    final ReferenceDataProviderGetResult dataResult = refDataProvider.getReferenceData(dataRequest);
    final ReferenceData perSecResult = dataResult.getReferenceData(indexTicker);
    if (perSecResult.isIdentifierError()) {
      final List<ReferenceDataError> errors = perSecResult.getErrors();
      if (!errors.isEmpty()) {
        s_logger.warn("Unable to lookup Index {} members because of exceptions {}", indexTicker, errors.toString());
        throw new OpenGammaRuntimeException("Unable to lookup Index members because of exceptions " + errors.toString());
      }
    }
    addIndexMembers(result, perSecResult, "INDX_MEMBERS");
    addIndexMembers(result, perSecResult, "INDX_MEMBERS2");
    addIndexMembers(result, perSecResult, "INDX_MEMBERS3");
    return result;
  }

  /**
   * @param result
   * @param perSecResult
   * @return
   */
  private static void addIndexMembers(final Set<String> result, final ReferenceData perSecResult, final String fieldName) {
    final FudgeMsg fieldData = perSecResult.getFieldValues();
    final List<FudgeField> fields = fieldData.getAllByName(fieldName);
    for (final FudgeField fudgeField : fields) {
      final FudgeMsg msg = (FudgeMsg) fudgeField.getValue();
      final String memberTicker = msg.getString("Member Ticker and Exchange Code");
      result.add(memberTicker);
    }
  }

  public static Set<ExternalId> getOptionChain(final ReferenceDataProvider refDataProvider, final String securityID) {
    ArgumentChecker.notNull(securityID, "security name");
    final Set<ExternalId> result = new TreeSet<ExternalId>();
    final FudgeMsg fieldData = refDataProvider.getReferenceData(Collections.singleton(securityID), Collections.singleton(FIELD_OPT_CHAIN)).get(securityID);
    if (fieldData == null) {
      s_logger.info("Reference data for security {} cannot be null", securityID);
      return null;
    }
    for (final FudgeField field : fieldData.getAllByName(FIELD_OPT_CHAIN)) {
      final FudgeMsg chainContainer = (FudgeMsg) field.getValue();
      final String identifier = StringUtils.trimToNull(chainContainer.getString("Security Description"));
      if (identifier != null) {
        final ExternalId ticker = ExternalSchemes.bloombergTickerSecurityId(BloombergDataUtils.removeDuplicateWhiteSpace(identifier, " "));
        result.add(ticker);
      }
    }
    return result;
  }

  /**
   * Get the future chain for a security. There may be futures on multiple exchanges - in general need to restrict to exchanges using the same currency. Equities: restrict to One Chicago futures with
   * a lead market maker (e.g. AAPL=G3 OC Equity)
   * 
   * @param refDataProvider the reference data provider
   * @param securityID the security
   * @return the (ordered)
   */
  public static Set<ExternalId> getFuturechain(final ReferenceDataProvider refDataProvider, final String securityID) {
    ArgumentChecker.notNull(securityID, "security name");
    final Set<ExternalId> result = new TreeSet<>();

    final FudgeMsg fieldData = refDataProvider.getReferenceData(Collections.singleton(securityID), Collections.singleton(FIELD_FUT_CHAIN)).get(securityID);
    if (fieldData == null) {
      s_logger.info("Reference data for security {} cannot be null", securityID);
      return null;
    }

    for (final FudgeField field : fieldData.getAllByName(FIELD_FUT_CHAIN)) {
      final FudgeMsg chainContainer = (FudgeMsg) field.getValue();
      final String identifier = StringUtils.trimToNull(chainContainer.getString("Security Description"));
      if (identifier != null) {
        if (identifier.endsWith("OC Equity") && identifier.startsWith(securityID.split("\\s+")[0] + "=")) { // equity
          final ExternalId ticker = ExternalSchemes.bloombergTickerSecurityId(BloombergDataUtils.removeDuplicateWhiteSpace(identifier, " "));
          result.add(ticker);
        }
      }
    }
    return result;
  }

  /**
   * Checks if the specified field contains valid data.
   * 
   * @param name the field name, not null
   * @return true if the field is valid
   */
  public static boolean isValidField(final String name) {
    return ON_OFF_FIELDS.contains(name) || (StringUtils.isNotBlank(name) && !name.equalsIgnoreCase("N.A"));
  }

  public static ExternalIdBundleWithDates parseIdentifiers(final FudgeMsg fieldData, final String firstTradeDateField, final String lastTradeDateField) {
    ArgumentChecker.notNull(fieldData, "fieldData");
    final String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    final String cusip = fieldData.getString(FIELD_ID_CUSIP);
    final String isin = fieldData.getString(FIELD_ID_ISIN);
    final String sedol1 = fieldData.getString(FIELD_ID_SEDOL1);
    final String securityIdentifier = fieldData.getString(FIELD_PARSEKYABLE_DES);
    final String validFromStr = firstTradeDateField != null ? fieldData.getString(firstTradeDateField) : null;
    final String validToStr = lastTradeDateField != null ? fieldData.getString(lastTradeDateField) : null;

    final Set<ExternalIdWithDates> identifiers = new HashSet<ExternalIdWithDates>();
    if (isValidField(bbgUnique)) {
      final ExternalId buid = ExternalSchemes.bloombergBuidSecurityId(bbgUnique);
      identifiers.add(ExternalIdWithDates.of(buid, null, null));
    }
    if (isValidField(cusip)) {
      final ExternalId cusipId = ExternalSchemes.cusipSecurityId(cusip);
      identifiers.add(ExternalIdWithDates.of(cusipId, null, null));
    }
    if (isValidField(sedol1)) {
      final ExternalId sedol1Id = ExternalSchemes.sedol1SecurityId(sedol1);
      identifiers.add(ExternalIdWithDates.of(sedol1Id, null, null));
    }
    if (isValidField(isin)) {
      final ExternalId isinId = ExternalSchemes.isinSecurityId(isin);
      identifiers.add(ExternalIdWithDates.of(isinId, null, null));
    }
    if (isValidField(securityIdentifier)) {
      final ExternalId tickerId = ExternalSchemes.bloombergTickerSecurityId(securityIdentifier.replaceAll("\\s+", " "));
      LocalDate validFrom = null;
      if (isValidField(validFromStr)) {
        try {
          validFrom = LocalDate.parse(validFromStr);
        } catch (final DateTimeParseException ex) {
          s_logger.warn("valid from date not in yyyy-mm-dd format - {}", validFromStr);
        }
      }
      LocalDate validTo = null;
      if (isValidField(validToStr)) {
        try {
          validTo = LocalDate.parse(validToStr);
        } catch (final DateTimeParseException ex) {
          s_logger.warn("valid to date not in yyyy-mm-dd format - {}", validToStr);
        }
      }
      identifiers.add(ExternalIdWithDates.of(tickerId, validFrom, validTo));
    }
    return new ExternalIdBundleWithDates(identifiers);
  }

  /**
   * @param bundleWithDates the identifier bundle with dates
   * @return {@link ExternalIdBundleWithDates} with single and 2 digit year codes
   */
  public static ExternalIdBundleWithDates addTwoDigitYearCode(final ExternalIdBundleWithDates bundleWithDates) {
    ArgumentChecker.notNull(bundleWithDates, "bundleWithDates");
    final Set<ExternalIdWithDates> identifiers = new HashSet<ExternalIdWithDates>();
    for (final ExternalIdWithDates identifierWithDates : bundleWithDates) {
      final ExternalId identifier = identifierWithDates.toExternalId();
      final String identifierValue = identifier.getValue();
      if (identifierValue.contains(BloombergConstants.MARKET_SECTOR_COMDTY) && identifierValue.indexOf(' ') == identifierValue.lastIndexOf(' ')) {
        //found a future code
        final int splitIndex = identifierValue.lastIndexOf(' ');
        final String secCode = identifierValue.substring(0, splitIndex);

        //get two year digit code
        final int length = secCode.length();
        String yearStr = secCode.substring(length - 2);
        try {
          Integer.parseInt(yearStr);
          identifiers.add(ExternalIdWithDates.of(identifierWithDates.toExternalId(), identifierWithDates.getValidTo().plusDays(1), null));

          //add single digit as well
          final StringBuilder buf = new StringBuilder(secCode.substring(0, secCode.indexOf(yearStr)));
          buf.append(yearStr.charAt(yearStr.length() - 1));
          buf.append(identifierValue.substring(splitIndex));
          final ExternalId singleYearId = ExternalSchemes.bloombergTickerSecurityId(buf.toString());
          identifiers.add(ExternalIdWithDates.of(singleYearId, identifierWithDates.getValidFrom(), identifierWithDates.getValidTo()));
        } catch (final NumberFormatException ex) {
          // try the single digit
          yearStr = secCode.substring(length - 1);
          try {
            Integer.parseInt(yearStr);
            identifiers.add(identifierWithDates);
            //add double digit as well
            final LocalDate validTo = identifierWithDates.getValidTo();
            final String endYear = String.valueOf(validTo.getYear());
            final StringBuilder buf = new StringBuilder(secCode.substring(0, secCode.indexOf(yearStr)));
            buf.append(endYear.substring(endYear.length() - 2));
            buf.append(identifierValue.substring(splitIndex));
            final ExternalId doubleDigitYearId = ExternalSchemes.bloombergTickerSecurityId(buf.toString());
            identifiers.add(ExternalIdWithDates.of(doubleDigitYearId, identifierWithDates.getValidTo().plusDays(1), null));
          } catch (final NumberFormatException ex2) {
            s_logger.warn("cannot make out year code for {}", identifier);
          }
        }
      } else {
        identifiers.add(identifierWithDates);
      }
    }
    return new ExternalIdBundleWithDates(identifiers);
  }

  /**
   * Given a position master, it pulls the current positions identifier bundles.
   * 
   * @param positionMaster the position master, not-null
   * @return a set of bundles of current positions
   */
  public static Set<ExternalIdBundle> getCurrentIdentifiers(final PositionMaster positionMaster) {
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    final PositionSearchRequest searchRequest = new PositionSearchRequest();
    final Set<ExternalIdBundle> securities = new HashSet<ExternalIdBundle>();
    for (final PositionDocument doc : PositionSearchIterator.iterable(positionMaster, searchRequest)) {
      securities.add(doc.getPosition().getSecurityLink().getExternalId()); // TODO: doesn't work if linked by object id
    }
    return securities;
  }

  /**
   * Removes duplicate whitespace from the specified field.
   * 
   * @param field the field name, not null
   * @param replacement the replacement string, not null
   * @return the stripped field, not null
   */
  public static String removeDuplicateWhiteSpace(final String field, final String replacement) {
    if (field != null) {
      ArgumentChecker.notNull(field, "field");
      ArgumentChecker.notNull(replacement, "replacement");
      return field.replaceAll("\\s+", replacement);
    } else {
      return null;
    }
  }

  public static Set<ExternalId> identifierLoader(final Reader reader) {
    ArgumentChecker.notNull(reader, "reader");
    final Set<ExternalId> result = Sets.newHashSet();
    final BufferedReader inputReader = new BufferedReader(reader);
    try {
      String line;
      while ((line = inputReader.readLine()) != null) {
        if (StringUtils.isBlank(line) || line.charAt(0) == '#') {
          continue;
        }
        result.add(ExternalId.parse(line));
      }
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("cannot read from reader", ex);
    } finally {
      try {
        inputReader.close();
      } catch (final IOException ex) {
        s_logger.warn("cannot close reader ", ex);
      }
    }
    return result;
  }

  /**
   * Convert bundles to preferred bloomberg keys. Where possible these keys will be BUIDs [BBG-87]
   * 
   * @param identifiers the collection of bundles, not null
   * @param refDataProvider the ReferenceDataProvider to use to resolve bundles not containing BUIDs, not null
   * @return BiMap of bloomberg key (hopefully a buid key) to bundle, not null
   */
  public static BiMap<String, ExternalIdBundle> convertToBloombergBuidKeys(final Collection<ExternalIdBundle> identifiers, final ReferenceDataProvider refDataProvider) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(refDataProvider, "refDataProvider");

    final Set<String> nonBuids = new HashSet<String>();
    final BiMap<String, ExternalIdBundle> bundle2Bbgkey = HashBiMap.create();
    for (final ExternalIdBundle identifierBundle : identifiers) {
      final ExternalId preferredIdentifier = BloombergDomainIdentifierResolver.resolvePreferredIdentifier(identifierBundle);
      final String bloombergKey = BloombergDomainIdentifierResolver.toBloombergKey(preferredIdentifier);
      if (bloombergKey == null) {
        s_logger.warn("bundle {} resolves to null bloomberg key", identifierBundle);
        continue;
      }
      //REVIEW simon : is it ok that we discard duplicates here
      bundle2Bbgkey.put(bloombergKey, identifierBundle);
      if (!preferredIdentifier.getScheme().equals(ExternalSchemes.BLOOMBERG_BUID)) {
        nonBuids.add(bloombergKey);
      }
    }

    if (!nonBuids.isEmpty()) {
      //BBG-87 Map everything to BUIDs
      final Map<String, String> remaps = getBUID(refDataProvider, nonBuids);
      for (final Entry<String, String> entry : remaps.entrySet()) {
        final String nonBuid = entry.getKey();
        final String buid = entry.getValue();
        final ExternalId buidExternalId = ExternalId.of(ExternalSchemes.BLOOMBERG_BUID, buid);
        final String buidKey = BloombergDomainIdentifierResolver.toBloombergKey(buidExternalId);
        changeKey(nonBuid, buidKey, bundle2Bbgkey);
      }
    }
    return bundle2Bbgkey;
  }

  private static <TKey, TValue> void changeKey(final TKey oldKey, final TKey newKey, final BiMap<TKey, TValue> map) {
    //REVIEW simon : is it ok that we discard duplicates here
    final TValue oldValue = map.remove(oldKey);
    map.put(newKey, oldValue);
  }

  /**
   * Splits a ticker at the market sector, returning a pair of the ticker excluding the market sector and the market sector itself.
   * 
   * @param ticker the ticker, not null
   * @return a pair of the ticker excluding the market sector, and the market sector, not null
   */
  public static Pair<String, String> splitTickerAtMarketSector(final String ticker) {
    ArgumentChecker.notNull(ticker, "ticker");
    final int splitIdx = ticker.lastIndexOf(' ');
    if (splitIdx > 0) {
      return Pairs.of(ticker.substring(0, splitIdx), ticker.substring(splitIdx + 1));
    } else {
      return null;
    }
  }

  public static boolean isValidBloombergTicker(final String ticker) {
    ArgumentChecker.notNull(ticker, "ticker");
    final Matcher matcher = s_bloombergTickerPattern.matcher(ticker);
    if (matcher.matches()) {
      final String marketSector = matcher.group(3);
      s_logger.debug("market sector {} extracted from ticker {}", marketSector, ticker);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Generates an equity option ticker from details about the option.
   * 
   * @param underlyingTicker the ticker of the underlying equity, not null
   * @param expiry the option expiry, not null
   * @param optionType the option type, not null
   * @param strike the strike rate
   * @return the equity option ticker, not null
   */
  public static ExternalId generateEquityOptionTicker(final String underlyingTicker, final Expiry expiry, final OptionType optionType, final double strike) {
    ArgumentChecker.notNull(underlyingTicker, "underlyingTicker");
    ArgumentChecker.notNull(expiry, "expiry");
    ArgumentChecker.notNull(optionType, "optionType");
    Pair<String, String> tickerMarketSectorPair = splitTickerAtMarketSector(underlyingTicker);
    DateTimeFormatter expiryFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
    DecimalFormat strikeFormat = new DecimalFormat("0.###");
    String strikeString = strikeFormat.format(strike);
    StringBuilder sb = new StringBuilder();
    sb.append(tickerMarketSectorPair.getFirst())
        .append(' ')
        .append(expiry.getExpiry().toString(expiryFormatter))
        .append(' ')
        .append(optionType == OptionType.PUT ? 'P' : 'C')
        .append(strikeString)
        .append(' ')
        .append(tickerMarketSectorPair.getSecond());
    return ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, sb.toString());
  }

  public static String dateToBbgCode(final LocalDate date) {
    final String y = Integer.toString(date.getYear());
    return s_monthCode.get(date.getMonth()) + y.charAt(y.length() - 1);
  }

  public static ExternalId ricToBbgFuture(final String ric) {
    return ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER,
        ricToBbgFuturePrefix(ric) + ric.substring(ric.length() - 2, ric.length()) + " Comdty");
  }

  public static ExternalId ricToBbgFutureOption(final String ric, final boolean isCall, final double strike, final LocalDate expiry) {
    final String result = ricToBbgFuturePrefix(ric) + dateToBbgCode(expiry) + (isCall ? "C" : "P") + " " + THREE_DECIMAL_PLACES.format(strike) + " Comdty";
    return ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, result);
  }

  private static String ricToBbgFuturePrefix(String ric) {
    if (ric.length() < 4) {
      return null;
    }
    ric = ric.trim().toUpperCase();
    final String front = ric.substring(0, (Character.isDigit(ric.charAt(0)) ? 4 : 3));
    if (s_ricToBbgPrefixMap.containsKey(front)) {
      return s_ricToBbgPrefixMap.get(front);
    } else {
      throw new OpenGammaRuntimeException("Could not map RIC onto BBG code");
    }
  }

  public static ExternalId futureBundleToGenericFutureTicker(ExternalIdBundle bundle, ZonedDateTime now, OffsetTime futureExpiryTime, ZoneId futureExpiryTimeZone) {
    ZonedDateTime nextExpiry = now.toLocalDate().with(s_monthlyExpiryAdjuster).atTime(now.toLocalTime()).atZone(now.getZone());
    ExternalId bbgTicker = bundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    if (bbgTicker == null) {
      throw new OpenGammaRuntimeException("Could not find a Bloomberg Ticker in the supplied bundle " + bundle.toString());
    }
    final String code = bbgTicker.getValue();
    final String marketSector = splitTickerAtMarketSector(code).getSecond();
    try {
      String typeCode;
      String monthCode;
      int year;
      if (code.length() > 4 && code.charAt(4) == ' ') {
        // four letter futures code
        typeCode = code.substring(0, 2);
        monthCode = code.substring(2, 3);
        year = Integer.parseInt(code.substring(3, 4));

        final int thisYear = now.getYear();
        if ((thisYear % 10) > year) {
          year = ((thisYear / 10) * 10) + 10 + year;
        } else if ((thisYear % 10) == year) {
          // This code assumes that the code is for this year, so constructs a trial date using the year and month and adjusts it forward to the expiry
          // note we're not taking into account exchange closing time here.
          final Month month = s_monthCode.inverse().get(monthCode);
          if (month == null) {
            throw new OpenGammaRuntimeException("Invalid month code " + monthCode);
          }
          LocalDate nextExpiryIfThisYear = LocalDate.of((((thisYear / 10) * 10) + year), month, 1).with(s_monthlyExpiryAdjuster);
          ZonedDateTime nextExpiryDateTimeIfThisYear = nextExpiryIfThisYear.atTime(futureExpiryTime).atZoneSimilarLocal(futureExpiryTimeZone);
          if (now.isAfter(nextExpiryDateTimeIfThisYear)) {
            year = ((thisYear / 10) * 10) + 10 + year;
          } else {
            year = ((thisYear / 10) * 10) + year;
          }
        } else {
          year = ((thisYear / 10) * 10) + year;
        }
      } else if (code.length() > 5 && code.charAt(5) == ' ') {
        // five letter futures code
        typeCode = code.substring(0, 2);
        monthCode = code.substring(2, 3);
        s_logger.warn("Parsing retired futures code format {}", code);
        year = Integer.parseInt(code.substring(3, 5));
        if (year > 70) { // 58 year time bomb and ticking...
          year += 1900;
        } else {
          year += 2000;
        }
      } else {
        s_logger.warn("Unknown futures code format {}", code);
        return null;
      }
      // phew.
      // now we generate the expiry of the future from the code:
      // Again, note that we're not taking into account exchange trading hours.
      LocalDate expiryDate = LocalDate.of(year, s_monthCode.inverse().get(monthCode), 1).with(s_monthlyExpiryAdjuster);
      ZonedDateTime expiry = expiryDate.atTime(futureExpiryTime).atZoneSimilarLocal(futureExpiryTimeZone);
      int quarters = (int) nextExpiry.periodUntil(expiry, MONTHS) / 3;
      int genericFutureNumber = quarters + 1;
      StringBuilder sb = new StringBuilder();
      sb.append(typeCode);
      sb.append(genericFutureNumber);
      sb.append(" ");
      sb.append(marketSector);
      return ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, sb.toString());
    } catch (final NumberFormatException nfe) {
      s_logger.error("Could not parse futures code {}", code);
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the data provider name.
   * 
   * @param dataProvider the data provider, null returns the unknown value
   * @return the resolver data provider, not null
   */
  public static String resolveDataProvider(final String dataProvider) {
    return (dataProvider == null || dataProvider.equalsIgnoreCase(DATA_PROVIDER_UNKNOWN) || dataProvider.equalsIgnoreCase(DEFAULT_DATA_PROVIDER) ? DEFAULT_DATA_PROVIDER : dataProvider);
  }

  /**
   * Resolves the data provider to provide an observation time.
   * 
   * @param dataProvider the data provider, null returns the unknown value
   * @return the corresponding observation time for the given data provider
   */
  public static String resolveObservationTime(String dataProvider) {
    if (dataProvider == null || dataProvider.equalsIgnoreCase(DATA_PROVIDER_UNKNOWN) || dataProvider.equalsIgnoreCase(DEFAULT_DATA_PROVIDER)) {
      dataProvider = DEFAULT_DATA_PROVIDER;
    }
    return s_observationTimeMap.get(dataProvider);

  }

  private static DateTimeFormatter s_bloombergDateFormatter = new DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .appendValue(YEAR, 4)
      .appendValue(MONTH_OF_YEAR, 2)
      .appendValue(DAY_OF_MONTH, 2)
      .toFormatter();

  public static String toBloombergDate(LocalDate localDate) {
    localDate = localDate.withYear(Math.min(9999, localDate.getYear()));
    return localDate.toString(s_bloombergDateFormatter);
  }

  /**
   * Returns future month code for a given month
   * 
   * @param month the month of year, not null
   * @return the future month code, null if not available
   */
  public static String futureMonthCode(final Month month) {
    return s_monthCode.get(month);
  }

}
