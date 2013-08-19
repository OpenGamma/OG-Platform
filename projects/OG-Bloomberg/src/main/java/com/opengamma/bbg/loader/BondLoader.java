/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.FIELD_AMT_ISSUED;
import static com.opengamma.bbg.BloombergConstants.FIELD_ANNOUNCE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_CALC_TYP_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_CNTRY_ISSUE_ISO;
import static com.opengamma.bbg.BloombergConstants.FIELD_CPN;
import static com.opengamma.bbg.BloombergConstants.FIELD_CPN_FREQ;
import static com.opengamma.bbg.BloombergConstants.FIELD_CPN_TYP;
import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_DAY_CNT_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_FIRST_CPN_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FLOATER;
import static com.opengamma.bbg.BloombergConstants.FIELD_GUARANTOR;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_INDUSTRY_GROUP;
import static com.opengamma.bbg.BloombergConstants.FIELD_INT_ACC_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_ISSUER;
import static com.opengamma.bbg.BloombergConstants.FIELD_ISSUE_PX;
import static com.opengamma.bbg.BloombergConstants.FIELD_MARKET_SECTOR_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_MATURITY;
import static com.opengamma.bbg.BloombergConstants.FIELD_MIN_INCREMENT;
import static com.opengamma.bbg.BloombergConstants.FIELD_MIN_PIECE;
import static com.opengamma.bbg.BloombergConstants.FIELD_PAR_AMT;
import static com.opengamma.bbg.BloombergConstants.FIELD_REDEMP_VAL;
import static com.opengamma.bbg.BloombergConstants.FIELD_INFLATION_LINKED_INDICATOR;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_TYP;
import static com.opengamma.bbg.BloombergConstants.FIELD_SETTLE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_TICKER;
import static com.opengamma.bbg.BloombergConstants.FIELD_ZERO_CPN;
import static com.opengamma.bbg.BloombergConstants.MARKET_SECTOR_MUNI;
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
 * Loads the data for a Bond Future from Bloomberg.
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
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_TICKER,
      FIELD_MARKET_SECTOR_DES,
      FIELD_SECURITY_DES);
  
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
      "DOMESTIC");
  
  private static final String SOVEREIGN = "Sovereign";

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public BondLoader(ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.BOND);
  }

  private String validateAndGetStringField(FudgeMsg fieldData, String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      s_logger.warn(fieldName + " is null, cannot construct bond security");
      throw new OpenGammaRuntimeException(fieldName + " is null, cannot construct bond security");
    }
    return fieldData.getString(fieldName);
  }
  
  private String validateAndGetNullableStringField(FudgeMsg fieldData, String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      return null;
    }
    return fieldData.getString(fieldName);
  }

  private Double validateAndGetDoubleField(FudgeMsg fieldData, String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      s_logger.warn(fieldName + " is null, cannot construct bond security");
      throw new OpenGammaRuntimeException(fieldName + " is null, cannot construct bond security");
    }
    return fieldData.getDouble(fieldName);
  }
  
  private Double validateAndGetNullableDoubleField(FudgeMsg fieldData, String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      return null;
    }
    return fieldData.getDouble(fieldName);
  }
  
  private Integer validateAndGetNullableIntegerField(FudgeMsg fieldData, String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      return null;
    }
    return fieldData.getInt(fieldName);
  }

  private ZonedDateTime validateAndGetNullableDateField(FudgeMsg fieldData, String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      return null;
    }
    // These will need to be sorted out.
    LocalTime expiryTime = LocalTime.of(17, 00);
    ZoneId zone = ZoneOffset.UTC;
 
    LocalDate localDate = LocalDate.parse(fieldData.getString(fieldName));
    return localDate.atTime(expiryTime).atZone(zone);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(FudgeMsg fieldData) {
    try {
      String issuerName = validateAndGetStringField(fieldData, FIELD_ISSUER);
      String issuerType = validateAndGetStringField(fieldData, FIELD_INDUSTRY_GROUP);
      String inflationIndicator = validateAndGetNullableStringField(fieldData, FIELD_INFLATION_LINKED_INDICATOR);
      String issuerDomicile = validateAndGetStringField(fieldData, FIELD_CNTRY_ISSUE_ISO);
      String market = validateAndGetStringField(fieldData, FIELD_SECURITY_TYP);
      String currencyStr = validateAndGetStringField(fieldData, FIELD_CRNCY);
      Currency currency = Currency.parse(currencyStr);
      String yieldConventionStr = validateAndGetStringField(fieldData, FIELD_CALC_TYP_DES);
      YieldConvention yieldConvention = YieldConventionFactory.INSTANCE.getYieldConvention(yieldConventionStr);
      if (yieldConvention == null) {
        throw new OpenGammaRuntimeException("Cannot get yield Convention called " + yieldConventionStr);
      }
      String guaranteeType = fieldData.getString(FIELD_GUARANTOR); // bit unsure about this one.
      String maturityStr = validateAndGetStringField(fieldData, FIELD_MATURITY);
      // These will need to be sorted out.
      LocalTime expiryTime = LocalTime.of(17, 00);
      ZoneId zone = ZoneOffset.UTC;
      Expiry maturity = null;
      try {
        maturity = new Expiry(LocalDate.parse(maturityStr).atTime(expiryTime).atZone(zone), ExpiryAccuracy.DAY_MONTH_YEAR);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException(maturityStr + " returned from bloomberg not in format yyyy-mm-dd", e);
      }
      String couponType = validateAndGetStringField(fieldData, FIELD_CPN_TYP);
      Double couponRate = validateAndGetDoubleField(fieldData, FIELD_CPN);
      String zeroCoupon = validateAndGetStringField(fieldData, FIELD_ZERO_CPN);
      Frequency couponFrequency;
      if ("Y".equals(zeroCoupon)) {
        couponFrequency = SimpleFrequency.NEVER;
      } else {
        Integer couponFrequencyInt = validateAndGetNullableIntegerField(fieldData, FIELD_CPN_FREQ);
        couponFrequency = couponFrequencyInt != null ? SimpleFrequencyFactory.INSTANCE.getFrequency(couponFrequencyInt) : SimpleFrequency.NEVER;
      }
      String dayCountString = validateAndGetStringField(fieldData, FIELD_DAY_CNT_DES);
      // REVIEW: jim 27-Jan-2011 -- remove this and fix it properly.
      if (dayCountString.equals("ACT/ACT")) {
        dayCountString = "Actual/Actual ICMA";
      }
      ZonedDateTime announcementDate = validateAndGetNullableDateField(fieldData, FIELD_ANNOUNCE_DT);
      ZonedDateTime interestAccrualDate = validateAndGetNullableDateField(fieldData, FIELD_INT_ACC_DT);
      ZonedDateTime settlementDate = validateAndGetNullableDateField(fieldData, FIELD_SETTLE_DT);
      ZonedDateTime firstCouponDate = validateAndGetNullableDateField(fieldData, FIELD_FIRST_CPN_DT);
      if (currencyStr.equals("GBP")) {
        if (announcementDate.toLocalDate().isAfter(LocalDate.of(1998, 11, 1)) && dayCountString.equals("ACT/ACT")) {
          dayCountString = "Actual/Actual ICMA";
        } else if (dayCountString.equals("ACT/365")) {
          dayCountString = "Actual/365";
        }
      }
      DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(dayCountString);
      if (dayCount == null) {
        throw new OpenGammaRuntimeException("Could not find day count convention " + dayCountString);
      }
      Double issuancePrice = validateAndGetNullableDoubleField(fieldData, FIELD_ISSUE_PX);
      Double totalAmountIssued = validateAndGetDoubleField(fieldData, FIELD_AMT_ISSUED);
      Double minimumAmount = validateAndGetDoubleField(fieldData, FIELD_MIN_PIECE);
      Double minimumIncrement = validateAndGetDoubleField(fieldData, FIELD_MIN_INCREMENT);
      Double parAmount = validateAndGetDoubleField(fieldData, FIELD_PAR_AMT);
      Double redemptionValue = validateAndGetDoubleField(fieldData, FIELD_REDEMP_VAL);
  
      //String bbgUnique = validateAndGetStringField(fieldData, FIELD_ID_BBG_UNIQUE);
      String marketSector = validateAndGetStringField(fieldData, FIELD_MARKET_SECTOR_DES);
      String des = validateAndGetStringField(fieldData, FIELD_SECURITY_DES);
      
      ManageableSecurity bondSecurity;
      if ((inflationIndicator != null) && (inflationIndicator.trim().toUpperCase().startsWith("Y"))) {
        bondSecurity = new InflationBondSecurity(issuerName, issuerType, issuerDomicile, market, currency,
            yieldConvention, maturity, couponType, couponRate,
            couponFrequency, dayCount, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice,
            totalAmountIssued, minimumAmount, minimumIncrement, parAmount,
            redemptionValue);
        ((BondSecurity) bondSecurity).setAnnouncementDate(announcementDate);
        ((BondSecurity) bondSecurity).setGuaranteeType(guaranteeType);
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
      // set identifiers
      parseIdentifiers(fieldData, bondSecurity);
      return bondSecurity;
    } catch (OpenGammaRuntimeException ogre) {
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
  protected void parseIdentifiers(FudgeMsg fieldData, final ManageableSecurity security) {
    final String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    final String cusip = fieldData.getString(FIELD_ID_CUSIP);
    final String isin = fieldData.getString(FIELD_ID_ISIN);
    final String sedol1 = fieldData.getString(FIELD_ID_SEDOL1);
    final String ticker = fieldData.getString(FIELD_TICKER);
    final String coupon = fieldData.getString(FIELD_CPN);
    final String maturity = fieldData.getString(FIELD_MATURITY);
    final String marketSector = fieldData.getString(FIELD_MARKET_SECTOR_DES);
    
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
    if (isValidField(ticker) && isValidField(coupon) && isValidField(maturity) && isValidField(marketSector)) {
      try {
        identifiers.add(ExternalSchemes.bloombergTCMSecurityId(ticker, coupon, maturity, marketSector));
      } catch (Exception e) {
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
