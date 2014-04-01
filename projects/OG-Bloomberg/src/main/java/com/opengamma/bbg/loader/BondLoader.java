/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.FIELD_AMT_ISSUED;
import static com.opengamma.bbg.BloombergConstants.FIELD_ANNOUNCE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_BASE_CPI;
import static com.opengamma.bbg.BloombergConstants.FIELD_BB_COMPOSITE;
import static com.opengamma.bbg.BloombergConstants.FIELD_BULLET;
import static com.opengamma.bbg.BloombergConstants.FIELD_CALC_TYP_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_CALLABLE;
import static com.opengamma.bbg.BloombergConstants.FIELD_CNTRY_ISSUE_ISO;
import static com.opengamma.bbg.BloombergConstants.FIELD_CPN;
import static com.opengamma.bbg.BloombergConstants.FIELD_CPN_FREQ;
import static com.opengamma.bbg.BloombergConstants.FIELD_CPN_TYP;
import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_DAYS_TO_SETTLE;
import static com.opengamma.bbg.BloombergConstants.FIELD_DAY_CNT_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_FIRST_CPN_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FLOATER;
import static com.opengamma.bbg.BloombergConstants.FIELD_FLT_BENCH_MULTIPLIER;
import static com.opengamma.bbg.BloombergConstants.FIELD_FLT_DAYS_PRIOR;
import static com.opengamma.bbg.BloombergConstants.FIELD_FLT_SPREAD;
import static com.opengamma.bbg.BloombergConstants.FIELD_GUARANTOR;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BB_SEC_NUM_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_INDUSTRY_GROUP;
import static com.opengamma.bbg.BloombergConstants.FIELD_INDUSTRY_SECTOR;
import static com.opengamma.bbg.BloombergConstants.FIELD_INFLATION_LAG;
import static com.opengamma.bbg.BloombergConstants.FIELD_INFLATION_LINKED_INDICATOR;
import static com.opengamma.bbg.BloombergConstants.FIELD_INT_ACC_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_ISSUER;
import static com.opengamma.bbg.BloombergConstants.FIELD_ISSUE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_ISSUE_PX;
import static com.opengamma.bbg.BloombergConstants.FIELD_IS_PERPETUAL;
import static com.opengamma.bbg.BloombergConstants.FIELD_MARKET_SECTOR_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_MATURITY;
import static com.opengamma.bbg.BloombergConstants.FIELD_MIN_INCREMENT;
import static com.opengamma.bbg.BloombergConstants.FIELD_MIN_PIECE;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_PAR_AMT;
import static com.opengamma.bbg.BloombergConstants.FIELD_REDEMP_VAL;
import static com.opengamma.bbg.BloombergConstants.FIELD_REFERENCE_INDEX;
import static com.opengamma.bbg.BloombergConstants.FIELD_RESET_IDX;
import static com.opengamma.bbg.BloombergConstants.FIELD_RTG_FITCH;
import static com.opengamma.bbg.BloombergConstants.FIELD_RTG_MOODY;
import static com.opengamma.bbg.BloombergConstants.FIELD_RTG_SP;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_TYP;
import static com.opengamma.bbg.BloombergConstants.FIELD_SETTLE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_TICKER;
import static com.opengamma.bbg.BloombergConstants.FIELD_ZERO_CPN;
import static com.opengamma.bbg.BloombergConstants.MARKET_SECTOR_MUNI;
import static com.opengamma.bbg.BloombergConstants.FIELD_INTERPOLATION_FOR_COUPON_CALC;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.security.BloombergSecurityProvider;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.FloatingRateNoteSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Loads the data for a bond from Bloomberg.
 */
public class BondLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BondLoader.class);
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_BOND_FIELDS = ImmutableSet.of(
      FIELD_ISSUER,
      FIELD_INDUSTRY_GROUP,
      FIELD_INDUSTRY_SECTOR,
      FIELD_CNTRY_ISSUE_ISO,
      FIELD_SECURITY_TYP,
      FIELD_CALC_TYP_DES,
      FIELD_GUARANTOR,
      FIELD_CRNCY,
      FIELD_MATURITY,
      FIELD_CPN_TYP,
      FIELD_CPN,
      FIELD_CPN_FREQ,
      FIELD_ZERO_CPN,
      FIELD_DAY_CNT_DES,
      FIELD_ANNOUNCE_DT,
      FIELD_INT_ACC_DT,
      FIELD_SETTLE_DT,
      FIELD_FIRST_CPN_DT,
      FIELD_ISSUE_PX,
      FIELD_AMT_ISSUED,
      FIELD_MIN_PIECE,
      FIELD_MIN_INCREMENT,
      FIELD_PAR_AMT,
      FIELD_REDEMP_VAL,
      FIELD_FLOATER,
      FIELD_INFLATION_LINKED_INDICATOR,
      FIELD_REFERENCE_INDEX,
      FIELD_CALLABLE,
      FIELD_IS_PERPETUAL,
      FIELD_BULLET,
      FIELD_BASE_CPI,
      FIELD_RTG_FITCH,
      FIELD_RTG_MOODY,
      FIELD_RTG_SP,
      FIELD_BB_COMPOSITE,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_TICKER,
      FIELD_MARKET_SECTOR_DES,
      FIELD_SECURITY_DES,
      FIELD_FLT_DAYS_PRIOR,
      FIELD_FLT_SPREAD,
      FIELD_FLT_BENCH_MULTIPLIER,
      FIELD_ISSUE_DT,
      FIELD_DAYS_TO_SETTLE,
      FIELD_RESET_IDX,
      FIELD_PARSEKYABLE_DES,
      FIELD_ID_BB_SEC_NUM_DES,
      FIELD_INFLATION_LAG, 
      FIELD_INTERPOLATION_FOR_COUPON_CALC);

  /**
   * The valid Bloomberg security types for Bond
   */
  public static final Set<String> VALID_SECURITY_TYPES = ImmutableSet.of(
      "Prvt CMO FLT",
      "EURO MTN",
      "EURO-ZONE",
      "CF",
      "ABS Other",
      "EURO NON-DOLLAR",
      "CMBS",
      "ABS Auto",
      "PRIV PLACEMENT",
      "GLOBAL",
      "EURO-DOLLAR",
      "YANKEE",
      "US DOMESTIC",
      "ABS Card",
      "Prvt CMO Other",
      "SN",
      "Agncy ABS Other",
      "US GOVERNMENT",
      "UK GILT STOCK",
      "CANADIAN",
      "DOMESTIC",
      "AUSTRALIAN");

  private static final String SOVEREIGN = "Sovereign";

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public BondLoader(final ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.BOND);
  }

  private String validateAndGetStringField(final FudgeMsg fieldData, final String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      s_logger.warn(fieldName + " is null, cannot construct bond security");
      throw new OpenGammaRuntimeException(fieldName + " is null, cannot construct bond security");
    }
    return fieldData.getString(fieldName);
  }

  private String validateAndGetNullableStringField(final FudgeMsg fieldData, final String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      return null;
    }
    return fieldData.getString(fieldName);
  }

  private Double validateAndGetDoubleField(final FudgeMsg fieldData, final String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      s_logger.warn(fieldName + " is null, cannot construct bond security");
      throw new OpenGammaRuntimeException(fieldName + " is null, cannot construct bond security");
    }
    return fieldData.getDouble(fieldName);
  }

  private Double validateAndGetNullableDoubleField(final FudgeMsg fieldData, final String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      return null;
    }
    return fieldData.getDouble(fieldName);
  }

  private Integer validateAndGetNullableIntegerField(final FudgeMsg fieldData, final String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      return null;
    }
    return fieldData.getInt(fieldName);
  }

  private ZonedDateTime validateAndGetNullableDateField(final FudgeMsg fieldData, final String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      return null;
    }
    // These will need to be sorted out.
    final LocalTime expiryTime = LocalTime.of(17, 00);
    final ZoneId zone = ZoneOffset.UTC;

    final LocalDate localDate = LocalDate.parse(fieldData.getString(fieldName));
    return localDate.atTime(expiryTime).atZone(zone);
  }

  private Integer validateAndGetIntegerField(final FudgeMsg fieldData, final String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      s_logger.warn(fieldName + " is null, cannot construct bond security");
      throw new OpenGammaRuntimeException(fieldName + " is null, cannot construct bond security");
    }
    return fieldData.getInt(fieldName);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(final FudgeMsg fieldData) {
    try {
      final String issuerName = validateAndGetStringField(fieldData, FIELD_ISSUER);
      final String issuerType = validateAndGetStringField(fieldData, FIELD_INDUSTRY_GROUP);
      final String issuerSector = validateAndGetStringField(fieldData, FIELD_INDUSTRY_SECTOR);
      final String inflationIndicator = validateAndGetNullableStringField(fieldData, FIELD_INFLATION_LINKED_INDICATOR);
      final String isPerpetualStr = validateAndGetNullableStringField(fieldData, FIELD_IS_PERPETUAL);
      final boolean isPerpetual = (isPerpetualStr != null && isPerpetualStr.trim().toUpperCase().contains("Y"));
      final String isBulletStr = validateAndGetNullableStringField(fieldData, FIELD_BULLET);
      final boolean isBullet = (isBulletStr != null && isBulletStr.trim().toUpperCase().contains("Y"));
      final String isFloaterStr = validateAndGetNullableStringField(fieldData, FIELD_FLOATER);
      final boolean isFloater = (isFloaterStr != null && isFloaterStr.trim().toUpperCase().contains("Y"));
      final String callable = validateAndGetNullableStringField(fieldData, FIELD_CALLABLE);
      final boolean isCallable = (callable != null && callable.trim().toUpperCase().contains("Y"));
      final String issuerDomicile = validateAndGetStringField(fieldData, FIELD_CNTRY_ISSUE_ISO);
      final String market = validateAndGetStringField(fieldData, FIELD_SECURITY_TYP);
      final String currencyStr = validateAndGetStringField(fieldData, FIELD_CRNCY);
      final Currency currency = Currency.parse(currencyStr);
      final String yieldConventionStr = validateAndGetStringField(fieldData, FIELD_CALC_TYP_DES);
      final YieldConvention yieldConvention = YieldConventionFactory.INSTANCE.getYieldConvention(yieldConventionStr);
      if (yieldConvention == null) {
        throw new OpenGammaRuntimeException("Cannot get yield Convention called " + yieldConventionStr);
      }
      final String guaranteeType = fieldData.getString(FIELD_GUARANTOR); // bit unsure about this one.
      String maturityStr = validateAndGetNullableStringField(fieldData, FIELD_MATURITY);
      if (maturityStr == null && isPerpetual) {
        maturityStr = "2049-06-29"; // fake date, need to remove.
      }
      // These will need to be sorted out.
      final LocalTime expiryTime = LocalTime.of(17, 00);
      final ZoneId zone = ZoneOffset.UTC;
      Expiry maturity = null;
      try {
        maturity = new Expiry(LocalDate.parse(maturityStr).atTime(expiryTime).atZone(zone), ExpiryAccuracy.DAY_MONTH_YEAR);
      } catch (final Exception e) {
        throw new OpenGammaRuntimeException(maturityStr + " returned from bloomberg not in format yyyy-mm-dd", e);
      }
      final String rtgFitch = validateAndGetNullableStringField(fieldData, FIELD_RTG_FITCH);
      final String rtgMoody = validateAndGetNullableStringField(fieldData, FIELD_RTG_MOODY);
      final String rtgSp = validateAndGetNullableStringField(fieldData, FIELD_RTG_SP);
      final String bbComposite = validateAndGetNullableStringField(fieldData, FIELD_BB_COMPOSITE);
      final String couponType = validateAndGetStringField(fieldData, FIELD_CPN_TYP);
      final Double couponRate = validateAndGetDoubleField(fieldData, FIELD_CPN);
      final String zeroCoupon = validateAndGetStringField(fieldData, FIELD_ZERO_CPN);
      final String cusip = validateAndGetStringField(fieldData, FIELD_ID_CUSIP);
      Frequency couponFrequency;
      if ("Y".equals(zeroCoupon)) {
        couponFrequency = SimpleFrequency.NEVER;
      } else {
        final Integer couponFrequencyInt = validateAndGetNullableIntegerField(fieldData, FIELD_CPN_FREQ);
        couponFrequency = couponFrequencyInt != null ? SimpleFrequencyFactory.INSTANCE.getFrequency(couponFrequencyInt) : SimpleFrequency.NEVER;
      }
      String dayCountString = validateAndGetStringField(fieldData, FIELD_DAY_CNT_DES);
      // REVIEW: jim 27-Jan-2011 -- remove this and fix it properly.
      boolean isEOM = true;
      if (dayCountString.endsWith("NON-EOM")) {
        isEOM = false;
      }
      if (dayCountString.equals("ACT/ACT") || dayCountString.equals("ACT/ACT NON-EOM")) {
        dayCountString = "Actual/Actual ICMA";
      }
      final ZonedDateTime announcementDate = validateAndGetNullableDateField(fieldData, FIELD_ANNOUNCE_DT);
      final ZonedDateTime interestAccrualDate = validateAndGetNullableDateField(fieldData, FIELD_INT_ACC_DT);
      final ZonedDateTime settlementDate = validateAndGetNullableDateField(fieldData, FIELD_SETTLE_DT);
      final ZonedDateTime firstCouponDate = validateAndGetNullableDateField(fieldData, FIELD_FIRST_CPN_DT);
      if (currencyStr.equals("GBP")) {
        if (announcementDate.toLocalDate().isAfter(LocalDate.of(1998, 11, 1)) && dayCountString.equals("ACT/ACT")) {
          dayCountString = "Actual/Actual ICMA";
        } else if (dayCountString.equals("ACT/365")) {
          dayCountString = "Actual/365";
        }
      }
      final DayCount dayCount = DayCountFactory.of(dayCountString);
      final Double issuancePrice = validateAndGetNullableDoubleField(fieldData, FIELD_ISSUE_PX);
      final Double totalAmountIssued = validateAndGetDoubleField(fieldData, FIELD_AMT_ISSUED);
      final Double minimumAmount = validateAndGetDoubleField(fieldData, FIELD_MIN_PIECE);
      final Double minimumIncrement = validateAndGetDoubleField(fieldData, FIELD_MIN_INCREMENT);
      final Double parAmount = validateAndGetDoubleField(fieldData, FIELD_PAR_AMT);
      final Double redemptionValue = validateAndGetDoubleField(fieldData, FIELD_REDEMP_VAL);

      //String bbgUnique = validateAndGetStringField(fieldData, FIELD_ID_BBG_UNIQUE);
      final String marketSector = validateAndGetStringField(fieldData, FIELD_MARKET_SECTOR_DES);
      final String des = validateAndGetStringField(fieldData, FIELD_SECURITY_DES);

      ManageableSecurity bondSecurity;
      final ExternalId legalEntityId = ExternalId.of(ExternalSchemes.CUSIP_ENTITY_STUB, cusip.substring(0, 6));
      if ((inflationIndicator != null) && (inflationIndicator.trim().toUpperCase().startsWith("Y"))) {
        // six character stub of CUSIP to link to legal entity.
        final String referenceIndexStr = validateAndGetStringField(fieldData, FIELD_REFERENCE_INDEX);
        final String baseCPI = validateAndGetStringField(fieldData, FIELD_BASE_CPI); // keep as string because going into attributes
        final String inflationLag = validateAndGetStringField(fieldData, FIELD_INFLATION_LAG);
        final String daysToSettle = validateAndGetStringField(fieldData, FIELD_DAYS_TO_SETTLE);
        final String interpolationMethod = validateAndGetStringField(fieldData, FIELD_INTERPOLATION_FOR_COUPON_CALC);
        final ExternalId referenceIndex = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, referenceIndexStr);
        bondSecurity = new InflationBondSecurity(issuerName, issuerType, issuerDomicile, market, currency,
            yieldConvention, maturity, couponType, couponRate,
            couponFrequency, dayCount, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice,
            totalAmountIssued, minimumAmount, minimumIncrement, parAmount,
            redemptionValue);
        ((BondSecurity) bondSecurity).setAnnouncementDate(announcementDate);
        ((BondSecurity) bondSecurity).setGuaranteeType(guaranteeType);
        ((BondSecurity) bondSecurity).addAttribute("BaseCPI", baseCPI);
        ((BondSecurity) bondSecurity).addAttribute("ReferenceIndexId", referenceIndex.toString());
        ((BondSecurity) bondSecurity).addAttribute("InflationLag", inflationLag);
        ((BondSecurity) bondSecurity).addAttribute("daysToSettle", daysToSettle);
        ((BondSecurity) bondSecurity).addAttribute("interpolationMethod", interpolationMethod);
      } else if (isFloater) {
        // six character stub of CUSIP to link to legal entity.
        final String benchmarkRateStr = validateAndGetStringField(fieldData, FIELD_RESET_IDX)  + " Index"; //TODO safe to assume the suffix?
        final ExternalId benchmarkRateId = ExternalSchemes.bloombergTickerSecurityId(benchmarkRateStr);
        final ZonedDateTime issueDate = validateAndGetNullableDateField(fieldData, FIELD_ISSUE_DT);
        final int daysToSettle = validateAndGetIntegerField(fieldData, FIELD_DAYS_TO_SETTLE);
        final int resetDays = validateAndGetIntegerField(fieldData, FIELD_FLT_DAYS_PRIOR);
        final double spread = validateAndGetDoubleField(fieldData, FIELD_FLT_SPREAD);
        final double leverage = validateAndGetDoubleField(fieldData, FIELD_FLT_BENCH_MULTIPLIER);
        final String country = validateAndGetStringField(fieldData, FIELD_CNTRY_ISSUE_ISO);
        final ExternalId regionId = ExternalSchemes.financialRegionId(country);
        bondSecurity = new FloatingRateNoteSecurity(currency, maturity, issueDate, minimumIncrement, daysToSettle,
            resetDays, dayCount, regionId, legalEntityId, benchmarkRateId, spread, leverage, couponFrequency);
      } else if (issuerType.trim().equals(SOVEREIGN)) {
        bondSecurity = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency,
            yieldConvention, maturity, couponType, couponRate,
            couponFrequency, dayCount, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice,
            totalAmountIssued, minimumAmount, minimumIncrement, parAmount,
            redemptionValue);
        ((BondSecurity) bondSecurity).setAnnouncementDate(announcementDate);
        ((BondSecurity) bondSecurity).setGuaranteeType(guaranteeType);
      } else if (marketSector.equals(MARKET_SECTOR_MUNI)) {
        bondSecurity = new MunicipalBondSecurity(issuerName, issuerType, issuerDomicile, market, currency,
            yieldConvention, maturity, couponType, couponRate,
            couponFrequency, dayCount, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice,
            totalAmountIssued, minimumAmount, minimumIncrement, parAmount,
            redemptionValue);
        ((BondSecurity) bondSecurity).setAnnouncementDate(announcementDate);
        ((BondSecurity) bondSecurity).setGuaranteeType(guaranteeType);
      } else {
        bondSecurity = new CorporateBondSecurity(issuerName, issuerType, issuerDomicile, market, currency,
            yieldConvention, maturity, couponType, couponRate,
            couponFrequency, dayCount, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice,
            totalAmountIssued, minimumAmount, minimumIncrement, parAmount,
            redemptionValue);
        ((BondSecurity) bondSecurity).setAnnouncementDate(announcementDate);
        ((BondSecurity) bondSecurity).setGuaranteeType(guaranteeType);
      }

      bondSecurity.setName(des.trim());
      bondSecurity.addAttribute("Bullet", isBullet ? "Y" : "N");
      bondSecurity.addAttribute("Callable", isCallable ? "Y" : "N");
      bondSecurity.addAttribute("Perpetual", isPerpetual ? "Y" : "N");
      bondSecurity.addAttribute("EOM", isEOM ? "Y" : "N");
      bondSecurity.addAttribute("LegalEntityId", legalEntityId.toString());
      if (rtgFitch != null) {
        bondSecurity.addAttribute("RatingFitch", rtgFitch);
      }
      if (rtgMoody != null) {
        bondSecurity.addAttribute("RatingMoody", rtgMoody);
      }
      if (rtgSp != null) {
        bondSecurity.addAttribute("RatingSP", rtgSp);
      }
      if (issuerSector != null) {
        bondSecurity.addAttribute("IndustrySector", issuerSector);
      }
      if (bbComposite != null) {
        bondSecurity.addAttribute("RatingComposite", bbComposite);
      }
      // set identifiers
      parseIdentifiers(fieldData, bondSecurity);
      return bondSecurity;
    } catch (final OpenGammaRuntimeException ogre) {
      s_logger.error("Error loading bond {} - {} - FLOATER={}, Fields are {}",
          new Object[] {fieldData.getValue(FIELD_ID_ISIN), ogre.getMessage(), fieldData.getString(FIELD_FLOATER), null }); //fieldData });
      return null;
    }
  }

  /**
   * Parse the identifiers from the response.  Note that we don't populate BLOOMBERG_TICKER because it's always either S or T.
   * @param fieldData  the response, not null
   * @param security  the security to populate, not null
   */
  @Override
  protected void parseIdentifiers(final FudgeMsg fieldData, final ManageableSecurity security) {
    final String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    final String cusip = fieldData.getString(FIELD_ID_CUSIP);
    final String isin = fieldData.getString(FIELD_ID_ISIN);
    final String sedol1 = fieldData.getString(FIELD_ID_SEDOL1);
    final String ticker = fieldData.getString(FIELD_TICKER);
    final String coupon = fieldData.getString(FIELD_CPN);
    final String maturity = fieldData.getString(FIELD_MATURITY);
    final String marketSector = fieldData.getString(FIELD_MARKET_SECTOR_DES);
    final String parsekyableDes = fieldData.getString(FIELD_PARSEKYABLE_DES);
    final String idBbSecNumDes = fieldData.getString(FIELD_ID_BB_SEC_NUM_DES);

    final Set<ExternalId> identifiers = new HashSet<ExternalId>();
    if (isValidField(bbgUnique)) {
      identifiers.add(ExternalSchemes.bloombergBuidSecurityId(bbgUnique));
      security.setUniqueId(BloombergSecurityProvider.createUniqueId(bbgUnique));
    }
    if (isValidField(cusip)) {
      identifiers.add(ExternalSchemes.cusipSecurityId(cusip));
    }
    if (isValidField(sedol1)) {
      identifiers.add(ExternalSchemes.sedol1SecurityId(sedol1));
    }
    if (isValidField(isin)) {
      identifiers.add(ExternalSchemes.isinSecurityId(isin));
    }
    if (isValidField(idBbSecNumDes) && isValidField(marketSector)) {
      identifiers.add(ExternalSchemes.bloombergTickerSecurityId(idBbSecNumDes.replaceAll("\\s+", " ").concat(" ").concat(marketSector.trim())));      
    } else if (isValidField(parsekyableDes)) {
      s_logger.warn("For {} Could not find valid field BB_SEC_NUM_DES and/or MARKET_SECTOR " + 
                    "(essentially the Ticker, coupon, maturity + yellow key) so falling back to PARSEKYABLE_DES.  " + 
                    " This may mean bond future baskets won't link to the underlying correctly as they are in the TCM format.", parsekyableDes);
      identifiers.add(ExternalSchemes.bloombergTickerSecurityId(parsekyableDes.replaceAll("\\s+", " ")));
    }
    if (isValidField(ticker) && isValidField(coupon) && isValidField(maturity) && isValidField(marketSector)) {
      try {
        identifiers.add(ExternalSchemes.bloombergTCMSecurityId(ticker, coupon, maturity, marketSector));
      } catch (final Exception e) {
        s_logger.warn("Couldn't add Bloomberg TCM to bond", e);
      }
    }
    security.setExternalIdBundle(ExternalIdBundle.of(identifiers));
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_BOND_FIELDS;
  }

}
