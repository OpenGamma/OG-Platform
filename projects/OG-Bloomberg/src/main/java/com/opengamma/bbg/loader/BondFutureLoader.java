/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_BOND_FUTURE_TYPE;
import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUTURES_CATEGORY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_CONT_SIZE;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_DELIVERABLE_BONDS;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_DLV_DT_FIRST;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_DLV_DT_LAST;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LAST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LONG_NAME;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_TRADING_HRS;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_VAL_PT;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_MIC_PRIM_EXCH;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Loads the data for a Bond Future from Bloomberg.
 */
public class BondFutureLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BondFutureLoader.class);
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_BOND_FUTURE_FIELDS = Collections.unmodifiableSet(Sets.newHashSet(
      FIELD_FUT_LONG_NAME,
      FIELD_ID_MIC_PRIM_EXCH,
      FIELD_CRNCY,
      FIELD_FUT_LAST_TRADE_DT,
      FIELD_FUT_TRADING_HRS,
      FIELD_FUT_DELIVERABLE_BONDS,
      FIELD_FUT_DELIVERABLE_BONDS,
      FIELD_FUTURES_CATEGORY,
      FIELD_FUT_DLV_DT_FIRST,
      FIELD_FUT_DLV_DT_LAST,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_PARSEKYABLE_DES,
      FIELD_FUT_VAL_PT,
      FIELD_FUT_CONT_SIZE));
  
  /**
   * The valid Bloomberg future categories for Bond Futures
   */
  public static final Set<String> VALID_FUTURE_CATEGORIES = ImmutableSet.of(BLOOMBERG_BOND_FUTURE_TYPE);

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public BondFutureLoader(ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.BOND_FUTURE);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(FudgeMsg fieldData) {
    String expiryDate = fieldData.getString(FIELD_FUT_LAST_TRADE_DT);
    String futureTradingHours = fieldData.getString(FIELD_FUT_TRADING_HRS);
    String micExchangeCode = fieldData.getString(FIELD_ID_MIC_PRIM_EXCH);
    String currencyStr = fieldData.getString(FIELD_CRNCY);
    String category = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUTURES_CATEGORY), " ");
    String firstDeliveryDateStr = fieldData.getString(FIELD_FUT_DLV_DT_FIRST);
    String lastDeliveryDateStr = fieldData.getString(FIELD_FUT_DLV_DT_LAST);
    String name = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUT_LONG_NAME), " ");
    String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    Double unitAmount = fieldData.getDouble(FIELD_FUT_CONT_SIZE);
    if (!fieldData.hasField(FIELD_FUT_CONT_SIZE) || unitAmount == null) {
      s_logger.warn("FIELD_FUT_VAL_PT does not contain a numeric value (" + fieldData.getString(FIELD_FUT_VAL_PT) + ")");
      return null;
    }

    if (!isValidField(bbgUnique)) {
      s_logger.warn("bbgUnique is null, cannot construct bond future security");
      return null;
    }
    if (!isValidField(futureTradingHours)) {
      s_logger.warn("futures trading hours is null, cannot construct bond future security");
      return null;
    }
    if (!isValidField(expiryDate)) {
      s_logger.warn("expiry date is null, cannot construct bond future security");
      return null;
    }
    if (!isValidField(micExchangeCode)) {
      s_logger.warn("settlement exchange is null, cannot construct bond future security");
      return null;
    }
    if (!isValidField(currencyStr)) {
      s_logger.warn("currency is null, cannot construct bond future security");
      return null;
    }
    if (!isValidField(category)) {
      s_logger.warn("category is null, cannot construct bond future security");
      return null;
    }
    if (!isValidField(firstDeliveryDateStr)) {
      s_logger.warn("first delivery date is invalid, cannot construct bond future security");
    }
    if (!isValidField(lastDeliveryDateStr)) {
      s_logger.warn("lastt delivery date is invalid, cannot construct bond future security");
    }

    Expiry expiry = decodeExpiry(expiryDate, futureTradingHours);
    if (expiry == null) {
      return null;
    }
    Currency currency = Currency.parse(currencyStr);

    ZonedDateTime firstDeliverDate = decodeDeliveryDate(firstDeliveryDateStr);
    ZonedDateTime lastDeliverDate = decodeDeliveryDate(lastDeliveryDateStr);
    Set<BondFutureDeliverable> basket = createBondDeliverables(fieldData);
    BondFutureSecurity security = new BondFutureSecurity(expiry, micExchangeCode, micExchangeCode, currency, unitAmount, basket,
                                                         firstDeliverDate, lastDeliverDate, category);

    // set identifiers
    parseIdentifiers(fieldData, security);
    security.setName(name);
    return security;
  }

  /**
   * @param fieldData
   * @return
   */
  private Set<BondFutureDeliverable> createBondDeliverables(FudgeMsg fieldData) {
    Set<BondFutureDeliverable> result = new HashSet<BondFutureDeliverable>();
    for (FudgeField field : fieldData.getAllByName(FIELD_FUT_DELIVERABLE_BONDS)) {
      if (field.getValue() instanceof FudgeMsg) {
        FudgeMsg deliverableContainer = (FudgeMsg) field.getValue();
        Double conversionFactor = deliverableContainer.getDouble("Conversion Factor");
        String tcm = deliverableContainer.getString("Ticker, Coupon, Maturity of Deliverable Bonds");
        if (conversionFactor != null && isValidField(tcm)) {
          ExternalIdBundle ids = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(tcm));
          BondFutureDeliverable deliverable = new BondFutureDeliverable(ids, conversionFactor);
          result.add(deliverable);
        }
      }
    }
    return result;
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_BOND_FUTURE_FIELDS;
  }

}
