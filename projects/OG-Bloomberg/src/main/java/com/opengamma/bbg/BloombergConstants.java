/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.bloomberglp.blpapi.Name;
import com.google.common.collect.ImmutableSet;

/**
 * Bloomberg Constants
 */
public final class BloombergConstants {

  /**
   * Restricted constructor.
   */
  private BloombergConstants() {
  }

  /**
   * BLOOMBERG datasource name.
   */
  public static final String BLOOMBERG_DATA_SOURCE_NAME = "BLOOMBERG";
  /**
   * BLOOMBERG default port.
   */
  public static final String DEFAULT_PORT = "8194";
  /**
   * Name for Unknown dataProvider.
   */
  public static final String DATA_PROVIDER_UNKNOWN = "UNKNOWN";
  /**
   * Name for default data provider.
   */
  public static final String DEFAULT_DATA_PROVIDER = "DEFAULT";
  /**
   * OPTION CHAIN FIELD
   */
  public static final String FIELD_OPT_CHAIN = "OPT_CHAIN";
  /**
   * Future chain
   */
  public static final String FIELD_FUT_CHAIN = "FUT_CHAIN";
  /**
   * Default start date for timeseries and securities
   */
  public static final LocalDate DEFAULT_START_DATE = LocalDate.of(1900, Month.JANUARY, 1);

  // CSOFF: TODO: actually write the docs
  public static final String MARKET_SECTOR_GOVT = "Govt";
  public static final String MARKET_SECTOR_CORP = "Corp";
  public static final String MARKET_SECTOR_MUNI = "Muni";
  public static final String MARKET_SECTOR_CURNCY = "Curncy";
  public static final String MARKET_SECTOR_COMDTY = "Comdty";
  public static final String MARKET_SECTOR_EQUITY = "Equity";
  public static final String MARKET_SECTOR_INDEX = "Index";
  public static final String MARKET_SECTOR_PREFERRED = "Pfd";
  public static final String MARKET_SECTOR_MMKT = "M-Mkt";
  public static final String MARKET_SECTOR_MTGE = "Mtge";

  public static final String FIELD_SECURITY_TYPE = "SECURITY_TYP";
  public static final String FIELD_SECURITY_TYPE2 = "SECURITY_TYP2";
  public static final String FIELD_OPT_EXERCISE_TYP = "OPT_EXER_TYP";
  public static final String FIELD_OPT_PUT_CALL = "OPT_PUT_CALL";
  public static final String FIELD_OPT_STRIKE_PX = "OPT_STRIKE_PX";
  public static final String FIELD_OPT_UNDL_CRNCY = "OPT_UNDL_CRNCY";
  public static final String FIELD_OPT_EXPIRE_DT = "OPT_EXPIRE_DT";
  public static final String FIELD_OPT_UNDL_TICKER = "OPT_UNDL_TICKER";
  public static final String FIELD_OPT_UNDERLYING_SECURITY_DES = "UNDERLYING_SECURITY_DES";
  public static final String FIELD_OPTION_ROOT_TICKER = "OPTION_ROOT_TICKER";
  /**
   * OPTION TICK VALUE FIELD
   */
  public static final String FIELD_OPT_TICK_VAL = "OPT_TICK_VAL";
  public static final String FIELD_OPT_VAL_PT = "OPT_VAL_PT";

  public static final String FIELD_NAME = "NAME";
  public static final String FIELD_TICKER = "TICKER";
  public static final String FIELD_EXCH_CODE = "EXCH_CODE";
  public static final String FIELD_SECURITY_DES = "SECURITY_DES";
  public static final String FIELD_SECURITY_SHORT_DES = "SECURITY_SHORT_DES";
  public static final String FIELD_PRIMARY_EXCHANGE_NAME = "EQY_PRIM_EXCH";
  public static final String FIELD_PRIMARY_EXCHANGE_CODE = "EQY_PRIM_EXCH_SHRT";
  public static final String FIELD_MIC_LOCAL_EXCH = "ID_MIC_LOCAL_EXCH";
  public static final String FIELD_MIC_PRIM_EXCH = "ID_MIC_PRIM_EXCH";
  public static final String FIELD_MIC_1 = "ID_MIC1";
  public static final String FIELD_CRNCY = "CRNCY";
  public static final String FIELD_COUNTRY_ISO = "COUNTRY_ISO";
  public static final String FIELD_TICKER_AND_EXCH_CODE = "TICKER_AND_EXCH_CODE";
  public static final String FIELD_MARKET_SECTOR_DES = "MARKET_SECTOR_DES";
  public static final String FIELD_ISSUER = "ISSUER";
  public static final String FIELD_ISSUE_DT = "ISSUE_DT";
  public static final String FIELD_ID_CUSIP = "ID_CUSIP";
  public static final String FIELD_ID_ISIN = "ID_ISIN";
  public static final String FIELD_ID_SEDOL1 = "ID_SEDOL1";
  public static final String FIELD_ID_BBG_UNIQUE = "ID_BB_UNIQUE";
  public static final String FIELD_ID_BB_SEC_NUM_DES = "ID_BB_SEC_NUM_DES";
  public static final String FIELD_UNDL_ID_BB_UNIQUE = "UNDL_ID_BB_UNIQUE";
  public static final String FIELD_FUT_LAST_TRADE_DT = "FUT_LAST_TRADE_DT";
  public static final String FIELD_FUT_FIRST_TRADE_DT = "FUT_FIRST_TRADE_DT";
  public static final String FIELD_FUT_TRADING_HRS = "FUT_TRADING_HRS";
  public static final String FIELD_FUT_LONG_NAME = "FUT_LONG_NAME";
  public static final String FIELD_FUT_EXCH_NAME_SHRT = "FUT_EXCH_NAME_SHRT";
  public static final String FIELD_FUTURES_CATEGORY = "FUTURES_CATEGORY";
  public static final String FIELD_FUT_DLV_DT_FIRST = "FUT_DLV_DT_FIRST";
  public static final String FIELD_FUT_DLV_DT_LAST = "FUT_DLV_DT_LAST";
  public static final String FIELD_FUT_TRADING_UNITS = "FUT_TRADING_UNITS";
  /**
   * A future contract changes by this amount for a single point move of the underlier
   */
  public static final String FIELD_FUT_VAL_PT = "FUT_VAL_PT";
  public static final String FIELD_GICS_SUB_INDUSTRY = "GICS_SUB_INDUSTRY";
  public static final String FIELD_FUT_DLVRBLE_BNDS_BB_UNIQUE = "FUT_DLVRBLE_BNDS_BB_UNIQUE";
  public static final String FIELD_FUT_DELIVERABLE_BONDS = "FUT_DELIVERABLE_BONDS";
  public static final String FIELD_QUOTE_TYP = "QUOTE_TYP";
  public static final String FIELD_QUOTE_UNITS = "QUOTE_UNITS";
  public static final String FIELD_QUOTED_CRNCY = "QUOTED_CRNCY";
  public static final String FIELD_ID_MIC_PRIM_EXCH = "ID_MIC_PRIM_EXCH";
  public static final String FIELD_FUT_CONT_SIZE = "FUT_CONT_SIZE";
  public static final String FIELD_UNDL_SPOT_TICKER = "UNDL_SPOT_TICKER";
  public static final String FIELD_EID_DATA = "eidData";
  public static final String FIELD_PARSEKYABLE_DES = "PARSEKYABLE_DES";
  public static final String FIELD_LAST_TRADEABLE_DT = "LAST_TRADEABLE_DT";

  //bonds
  public static final String FIELD_INDUSTRY_GROUP = "INDUSTRY_GROUP";
  public static final String FIELD_INDUSTRY_SECTOR = "INDUSTRY_SECTOR";
  public static final String FIELD_CNTRY_ISSUE_ISO = "CNTRY_ISSUE_ISO";
  public static final String FIELD_SECURITY_TYP = "SECURITY_TYP";
  public static final String FIELD_CALC_TYP_DES = "CALC_TYP_DES";
  public static final String FIELD_INFLATION_LINKED_INDICATOR = "INFLATION_LINKED_INDICATOR";
  public static final String FIELD_MTY_TYP = "MTY_TYP";
  public static final String FIELD_CALLABLE = "CALLABLE";
  public static final String FIELD_IS_PERPETUAL = "IS_PERPETUAL";
  public static final String FIELD_BULLET = "BULLET";

  public static final String FIELD_GUARANTOR = "GUARANTOR"; // maybe?
  public static final String FIELD_MATURITY = "MATURITY";
  public static final String FIELD_CPN_TYP = "CPN_TYP";
  public static final String FIELD_CPN = "CPN";
  public static final String FIELD_CPN_FREQ = "CPN_FREQ"; // times per year, 0=@maturity.
  public static final String FIELD_ZERO_CPN = "ZERO_CPN";
  public static final String FIELD_DAY_CNT_DES = "DAY_CNT_DES";
  public static final String FIELD_ANNOUNCE_DT = "ANNOUNCE_DT";
  public static final String FIELD_INT_ACC_DT = "INT_ACC_DT";
  public static final String FIELD_SETTLE_DT = "SETTLE_DT";
  public static final String FIELD_FIRST_CPN_DT = "FIRST_CPN_DT";
  public static final String FIELD_ISSUE_PX = "ISSUE_PX";
  public static final String FIELD_AMT_ISSUED = "AMT_ISSUED";
  public static final String FIELD_MIN_PIECE = "MIN_PIECE";
  public static final String FIELD_MIN_INCREMENT = "MIN_INCREMENT";
  public static final String FIELD_PAR_AMT = "PAR_AMT";
  public static final String FIELD_REDEMP_VAL = "REDEMP_VAL";
  public static final String FIELD_FLOATER = "FLOATER";
  public static final String FIELD_RTG_FITCH = "RTG_FITCH";
  public static final String FIELD_RTG_MOODY = "RTG_MOODY";
  public static final String FIELD_RTG_SP = "RTG_SP";
  public static final String FIELD_BB_COMPOSITE = "BB_COMPOSITE";
  public static final String FIELD_DAYS_TO_SETTLE = "DAYS_TO_SETTLE";
  public static final String FIELD_FLT_DAYS_PRIOR = "FLT_DAYS_PRIOR";
  public static final String FIELD_FLT_SPREAD = "FLT_SPREAD";
  public static final String FIELD_FLT_BENCH_MULTIPLIER = "FLT_BENCH_MULTIPLIER";
  public static final String FIELD_RESET_IDX = "RESET_IDX";
  public static final String FIELD_INDX_SOURCE = "INDX_SOURCE";
  public static final String FIELD_BASE_CPI = "BASE_CPI";
  public static final String FIELD_REFERENCE_INDEX = "REFERENCE_INDEX";
  public static final String FIELD_INFLATION_LAG = "INFLATION_LAG";
  public static final String FIELD_INTERPOLATION_FOR_COUPON_CALC = "INTERPOLATION_FOR_COUPON_CALC";

  public static final String BLOOMBERG_FIELDS_REQUEST = "fields";
  public static final String BLOOMBERG_SECURITIES_REQUEST = "securities";
  public static final String REF_DATA_SVC_NAME = "//blp/refdata";
  public static final String AUTH_SVC_NAME = "//blp/apiauth";
  public static final String MKT_DATA_SVC_NAME = "//blp/mktdata";
  public static final String BLOOMBERG_REFERENCE_DATA_REQUEST = "ReferenceDataRequest";
  public static final String BLOOMBERG_HISTORICAL_DATA_REQUEST = "HistoricalDataRequest";
  public static final String BLOOMBERG_INVALID_SECURITY = "INVALID_SECURITY";

  //option volatility
  public static final String FIELD_OPT_IMPLIED_VOLATILITY_BST = "OPT_IMPLIED_VOLATILITY_BST";
  public static final String FIELD_HIST_PUT_IMP_VOL = "HIST_PUT_IMP_VOL";

  //Different bloomberg equity type
  public static final String BBG_COMMON_STOCK_TYPE = "Common Stock";
  public static final String BBG_PREFERENCE_TYPE = "Preference";
  public static final String BBG_ADR_TYPE = "ADR";
  public static final String BBG_OPEN_END_FUND_TYPE = "Open-End Fund";
  public static final String BBG_CLOSED_END_FUND_TYPE = "Closed-End Fund";
  public static final String BBG_ETP_TYPE = "ETP";
  public static final String BBG_REIT_TYPE = "REIT";
  public static final String BBG_TRACKING_STOCK = "Tracking Stk";
  public static final String BBG_UNIT_TYPE = "Unit";
  public static final String BBG_RIGHT_TYPE = "Right";
  public static final String BBG_LTD_PART_TYPE = "Ltd Part";
  public static final String BBG_NY_REG_SHRS_TYPE = "NY Reg Shrs";
  public static final String BBG_PUBLIC_TYPE = "PUBLIC";
  public static final String BBG_EQUITY_WRT_TYPE = "Equity WRT";

  //Bloomberg currency types
  public static final String BBG_CROSS_CURRENCY_TYPE = "Cross Currency";
  public static final String BBG_CURRENCY_TYPE = "Currency";

  //Bloomberg metal future types
  public static final String BBG_PRECIOUS_METAL_TYPE = "Precious Metal";
  public static final String BBG_BASE_METAL_TYPE = "Base Metal";

  //Bloomberg energy future types
  public static final String BBG_REFINED_PRODUCTS = "Refined Products";
  public static final String BBG_ELECTRICITY = "Electricity";
  public static final String BBG_COAL = "Coal";
  public static final String BBG_CRUDE_OIL = "Crude Oil";
  public static final String BBG_NATURAL_GAS = "Natural Gas";

  //Bloomberg agriculture future types
  public static final String BBG_WHEAT = "Wheat";
  public static final String BBG_SOY = "Soy";
  public static final String BBG_LIVESTOCK = "Livestock";
  public static final String BBG_FOODSTUFF = "Foodstuff";
  public static final String BBG_CORN = "Corn";

  //Bloomberg index future type
  public static final String BLOOMBERG_EQUITY_INDEX_TYPE = "Equity Index";
  public static final String BBG_NON_EQUITY_INDEX_TYPE = "Non-Equity Index";
  public static final String BBG_WEEKLY_INDEX_OPTIONS_TYPE = "Weekly Index Options";

  public static final String BLOOMBERG_INTEREST_RATE_TYPE = "Interest Rate";
  public static final String BLOOMBERG_FINANCIAL_COMMODITY_OPTION_TYPE = "Financial commodity option.";
  public static final String BLOOMBERG_CURRENCY_TYPE = "Currency";

  //Bloomberg equity dividend future type
  public static final String BBG_STOCK_FUTURE_TYPE = "STOCK FUTURE";
  public static final String BLOOMBERG_EQUITY_DIVIDEND_TYPE = "SINGLE STOCK DIVIDEND FUTURE";

  /**
   * Bloomberg bond future security type description
   */
  public static final String BLOOMBERG_BOND_FUTURE_TYPE = "Bond";

  public static final String BLOOMBERG_EQUITY_OPTION_SECURITY_TYPE = "Equity Option";
  public static final String BLOOMBERG_INDEX_OPTION_SECURITY_TYPE = "Index Option";
  public static final String BLOOMBERG_AMERICAN_OPTION_TYPE = "American";
  public static final String EUROPEAN_OPTION_TYPE = "European";
  public static final String BLOOMBERG_US_DOMESTIC_BOND_SECURITY_TYPE = "US DOMESTIC";
  public static final String BLOOMBERG_US_GOVERNMENT_BOND_SECURITY_TYPE = "US GOVERNMENT";
  public static final String BLOOMBERG_UK_GILT_BOND_SECURITY_TYPE = "UK GILT STOCK";
  public static final String BLOOMBERG_GLOBAL_BOND_SECURITY_TYPE = "GLOBAL";
  public static final String BLOOMBERG_SINGLE_STOCK_FUTURE_SECURITY_TYPE = "SINGLE STOCK FUTURE";
  public static final String BLOOMBERG_PHYSICAL_COMMODITY_FUTURE_TYPE = "Physical commodity future.";
  public static final String BLOOMBERG_FINANCIAL_COMMODITY_FUTURE_TYPE = "Financial commodity future.";
  public static final String BLOOMBERG_PHYSICAL_COMMODITY_FUTURE_OPTION_TYPE = "Physical commodity option.";
  public static final String BLOOMBERG_PHYSICAL_INDEX_FUTURE_TYPE = "Physical index future.";

  /**
   * Rates
   */
  public static final String BLOOMBERG_NON_DELIVERABLE_IRS_SWAP_TYPE = "NON-DELIVERABLE IRS SWAP";
  public static final String BLOOMBERG_IMM_SWAP_TYPE = "IMM SWAP";

  public static final Name RESPONSE_ERROR = new Name("responseError");
  public static final Name SECURITY_DATA = new Name("securityData");
  public static final Name SECURITY = new Name("security");
  public static final Name FIELD_DATA = new Name("fieldData");
  public static final Name EID_DATA = new Name("eidData");
  public static final Name SECURITY_ERROR = new Name("securityError");
  public static final Name SUBCATEGORY = new Name("subcategory");
  public static final Name EXCEPTIONS = new Name("exceptions");
  public static final Name FIELD_EXCEPTIONS = new Name("fieldExceptions");
  public static final Name FIELD_ID = new Name("fieldId");
  public static final Name ERROR_INFO = new Name("errorInfo");
  public static final Name REASON = new Name("reason");
  public static final Name CATEGORY = new Name("category");
  public static final Name DESCRIPTION = new Name("description");
  public static final Name AUTHORIZATION_SUCCESS = new Name("AuthorizationSuccess");
  public static final Name AUTHORIZATION_FAILURE = new Name("AuthorizationFailure");

  //historical fields
  public static final String BBG_FIELD_LAST_PRICE = "PX_LAST";
  public static final String BBG_FIELD_CUR_MKT_CAP = "CUR_MKT_CAP";
  public static final String BBG_FIELD_VOLUME = "VOLUME";
  public static final String BBG_FIELD_VOLATILITY_30D = "VOLATILITY_30D";
  public static final String BBG_FIELD_YIELD_TO_MATURITY_MID = "YLD_YTM_MID";
  public static final String BBG_FIELD_SETTLE_PRICE = "PX_SETTLE";
  public static final String BBG_FIELD_DIVIDEND_YIELD = "EQY_DVD_YLD_EST";

  //quote unit information
  public static final String BBG_FIELD_FWD_SCALE = "FWD_SCALE";
  // CSON

  /**
   * Valid currency futures types.
   */
  public static final Set<String> VALID_CURRENCY_FUTURE_TYPES = ImmutableSet.of(
      BBG_CROSS_CURRENCY_TYPE,
      BBG_CURRENCY_TYPE);

  /**
   * Valid Equity types.
   */
  public static final Set<String> VALID_EQUITY_TYPES = ImmutableSet.of(
      BBG_COMMON_STOCK_TYPE,
      BBG_PREFERENCE_TYPE,
      BBG_ADR_TYPE,
      BBG_OPEN_END_FUND_TYPE,
      BBG_CLOSED_END_FUND_TYPE,
      BBG_ETP_TYPE,
      BBG_REIT_TYPE,
      BBG_TRACKING_STOCK,
      BBG_UNIT_TYPE,
      BBG_RIGHT_TYPE,
      BBG_LTD_PART_TYPE,
      BBG_PUBLIC_TYPE,
      BBG_NY_REG_SHRS_TYPE,
      BBG_EQUITY_WRT_TYPE);

  /**
   * Collections of fields known to go on/off intermittently from bloomberg.
   */
  public static final Set<String> ON_OFF_FIELDS = ImmutableSet.of(
      FIELD_MIC_PRIM_EXCH, // trading exchange
      FIELD_MIC_LOCAL_EXCH);

  /**
   * Fields loaded from Bloomberg by {@link com.opengamma.bbg.livedata.BloombergIdResolver}.
   */
  public static final Set<String> ID_RESOLVER_FIELDS = ImmutableSet.of(
      FIELD_ID_BBG_UNIQUE);

  /**
   * Fields loaded from Bloomberg by {@link com.opengamma.bbg.livedata.BloombergJmsTopicNameResolver}.
   */
  public static final Set<String> JMS_TOPIC_NAME_RESOLVER_FIELDS = ImmutableSet.of(
      FIELD_TICKER,
      FIELD_PRIMARY_EXCHANGE_NAME,
      FIELD_SECURITY_TYPE,
      FIELD_ISSUER,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_OPT_UNDL_TICKER);

  /**
   * Valid market sector
   */
  public static final Set<String> MARKET_SECTORS = ImmutableSet.of(
      MARKET_SECTOR_COMDTY,
      MARKET_SECTOR_CORP,
      MARKET_SECTOR_CURNCY,
      MARKET_SECTOR_EQUITY,
      MARKET_SECTOR_GOVT,
      MARKET_SECTOR_INDEX,
      MARKET_SECTOR_MMKT,
      MARKET_SECTOR_MTGE,
      MARKET_SECTOR_MUNI,
      MARKET_SECTOR_PREFERRED);

  /**
   * Bpipe application authentication  prefix
   */
  public static final String AUTH_APP_PREFIX = "AuthenticationMode=APPLICATION_ONLY;ApplicationAuthenticationType=APPNAME_AND_KEY;ApplicationName=";
  /**
   * The name of live data entitlement field
   */
  public static final String EID_FIELD = "EID";

}
