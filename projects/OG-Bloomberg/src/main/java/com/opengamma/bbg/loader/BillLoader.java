/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.FIELD_ANNOUNCE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_BB_COMPOSITE;
import static com.opengamma.bbg.BloombergConstants.FIELD_CALC_TYP_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_CNTRY_ISSUE_ISO;
import static com.opengamma.bbg.BloombergConstants.FIELD_CPN;
import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_DAYS_TO_SETTLE;
import static com.opengamma.bbg.BloombergConstants.FIELD_DAY_CNT_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_GUARANTOR;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_ISSUE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_MARKET_SECTOR_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_MATURITY;
import static com.opengamma.bbg.BloombergConstants.FIELD_MIN_INCREMENT;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_RTG_FITCH;
import static com.opengamma.bbg.BloombergConstants.FIELD_RTG_MOODY;
import static com.opengamma.bbg.BloombergConstants.FIELD_RTG_SP;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_TYP;
import static com.opengamma.bbg.BloombergConstants.FIELD_TICKER;
import static com.opengamma.bbg.BloombergConstants.FIELD_ZERO_CPN;
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
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Loads the data for a bill from Bloomberg.
 */
public class BillLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BillLoader.class);
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_BILL_FIELDS = ImmutableSet.of(
      FIELD_ISSUE_DT,
      FIELD_CNTRY_ISSUE_ISO,
      FIELD_SECURITY_TYP,
      FIELD_CALC_TYP_DES,
      FIELD_GUARANTOR,
      FIELD_CRNCY,
      FIELD_MATURITY,
      FIELD_ZERO_CPN,
      FIELD_DAY_CNT_DES,
      FIELD_MIN_INCREMENT,
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
      FIELD_PARSEKYABLE_DES,
      FIELD_DAYS_TO_SETTLE);

  /**
   * The valid Bloomberg security types for bills
   */
  public static final Set<String> VALID_SECURITY_TYPES2 = ImmutableSet.of("Bill");

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public BillLoader(final ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.BILL);
  }

  private String validateAndGetStringField(final FudgeMsg fieldData, final String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      s_logger.warn(fieldName + " is null, cannot construct bill security");
      throw new OpenGammaRuntimeException(fieldName + " is null, cannot construct bill security");
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
      s_logger.warn(fieldName + " is null, cannot construct bill security");
      throw new OpenGammaRuntimeException(fieldName + " is null, cannot construct bill security");
    }
    return fieldData.getDouble(fieldName);
  }

  private Integer validateAndGetIntegerField(final FudgeMsg fieldData, final String fieldName) {
    if (!isValidField(fieldData.getString(fieldName))) {
      s_logger.warn(fieldName + " is null, cannot construct bill security");
      throw new OpenGammaRuntimeException(fieldName + " is null, cannot construct bill security");
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

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(final FudgeMsg fieldData) {
    try {
      final String isZeroCoupon = validateAndGetStringField(fieldData, FIELD_ZERO_CPN);
      if (!"Y".equalsIgnoreCase(isZeroCoupon)) {
        throw new OpenGammaRuntimeException("Bill is not a zero coupon");
      }
      final String country = validateAndGetStringField(fieldData, FIELD_CNTRY_ISSUE_ISO);
      final String currencyStr = validateAndGetStringField(fieldData, FIELD_CRNCY);
      final Currency currency = Currency.parse(currencyStr);
      final String yieldConventionStr = validateAndGetStringField(fieldData, FIELD_CALC_TYP_DES);
      final YieldConvention yieldConvention = YieldConventionFactory.INSTANCE.getYieldConvention(yieldConventionStr);
      if (yieldConvention == null) {
        throw new OpenGammaRuntimeException("Cannot get yield convention called " + yieldConventionStr);
      }
      final String maturityStr = validateAndGetNullableStringField(fieldData, FIELD_MATURITY);
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
      String dayCountString = validateAndGetStringField(fieldData, FIELD_DAY_CNT_DES);
      // REVIEW: jim 27-Jan-2011 -- remove this and fix it properly.
      if (dayCountString.equals("ACT/ACT") || dayCountString.equals("ACT/ACT NON-EOM")) {
        dayCountString = "Actual/Actual ICMA";
      }
      final ZonedDateTime announcementDate = validateAndGetNullableDateField(fieldData, FIELD_ANNOUNCE_DT);
      if (currencyStr.equals("GBP")) {
        if (announcementDate.toLocalDate().isAfter(LocalDate.of(1998, 11, 1)) && dayCountString.equals("ACT/ACT")) {
          dayCountString = "Actual/Actual ICMA";
        } else if (dayCountString.equals("ACT/365")) {
          dayCountString = "Actual/365";
        }
      }
      final DayCount dayCount = DayCountFactory.of(dayCountString);
      final Double minimumIncrement = validateAndGetDoubleField(fieldData, FIELD_MIN_INCREMENT);
      final ZonedDateTime issueDate = validateAndGetNullableDateField(fieldData, FIELD_ISSUE_DT);
      final int daysToSettle = validateAndGetIntegerField(fieldData, FIELD_DAYS_TO_SETTLE);
      final String des = validateAndGetStringField(fieldData, FIELD_SECURITY_DES);
      final ExternalId regionId = ExternalSchemes.financialRegionId(country);
      final String cusip = validateAndGetStringField(fieldData, FIELD_ID_CUSIP);
      final ExternalId legalEntityId = ExternalId.of(ExternalSchemes.CUSIP_ENTITY_STUB, cusip.substring(0, 6));
      final ManageableSecurity billSecurity = new BillSecurity(currency, maturity, issueDate, minimumIncrement, daysToSettle,
          regionId, yieldConvention, dayCount, legalEntityId);

      billSecurity.setName(des.trim());
      if (rtgFitch != null) {
        billSecurity.addAttribute("RatingFitch", rtgFitch);
      }
      if (rtgMoody != null) {
        billSecurity.addAttribute("RatingMoody", rtgMoody);
      }
      if (rtgSp != null) {
        billSecurity.addAttribute("RatingSP", rtgSp);
      }
      if (bbComposite != null) {
        billSecurity.addAttribute("RatingComposite", bbComposite);
      }
      // set identifiers
      parseIdentifiers(fieldData, billSecurity);
      return billSecurity;
    } catch (final OpenGammaRuntimeException ogre) {
      s_logger.error("Error loading bill {}: {}",
          fieldData.getValue(FIELD_ID_ISIN), ogre.getMessage());
      return null;
    }
  }

  /**
   * Parse the identifiers from the response.  Note that we populate BLOOMBERG_TICKER with PARSEKYABLE_DES.
   * @param fieldData  the response, not null
   * @param security  the security to populate, not null
   */
  @Override
  protected void parseIdentifiers(final FudgeMsg fieldData, final ManageableSecurity security) {
    final String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    final String cusip = fieldData.getString(FIELD_ID_CUSIP);
    final String isin = fieldData.getString(FIELD_ID_ISIN);
    final String sedol1 = fieldData.getString(FIELD_ID_SEDOL1);
    final String ticker = fieldData.getString(FIELD_PARSEKYABLE_DES);
    final String coupon = fieldData.getString(FIELD_CPN);
    final String maturity = fieldData.getString(FIELD_MATURITY);
    final String marketSector = fieldData.getString(FIELD_MARKET_SECTOR_DES);

    final Set<ExternalId> identifiers = new HashSet<>();
    if (isValidField(bbgUnique)) {
      identifiers.add(ExternalSchemes.bloombergBuidSecurityId(bbgUnique));
      security.setUniqueId(BloombergSecurityProvider.createUniqueId(bbgUnique));
    }
    if (isValidField(ticker)) {
      identifiers.add(ExternalSchemes.bloombergTickerSecurityId(ticker));
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
      } catch (final Exception e) {
        s_logger.warn("Couldn't add Bloomberg TCM to bill", e);
      }
    }
    security.setExternalIdBundle(ExternalIdBundle.of(identifiers));
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_BILL_FIELDS;
  }

}
