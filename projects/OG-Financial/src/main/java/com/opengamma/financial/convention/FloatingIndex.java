/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A collection of core floating indexes commonly permitted for OTC clearing.
 * While clients may choose to use any index that the underlying implementations
 * support, in general, using one of these enumerated constants can ensure that
 * data is correctly pulled through by the underlying margin calculators and
 * is recommended.
 * In order to create a floating index rate (e.g. {@see FloatingInterestRateLeg#setFloatingReferenceRateId(ExternalId)})
 * you should get the floating index, and then call foo.
 */
public enum FloatingIndex implements NamedInstance {
  /**
   * The AUD-AONIA-OIS-COMPOUND index.
   */
  AUD_AONIA_OIS_COMPOUND(Currency.AUD, "AONIA", "AUD-AONIA-OIS-COMPOUND"),
  /**
   * The AUD-BBR-BBSW ISDA index.
   */
  AUD_BBR_BBSW(Currency.AUD, "BBR", "AUD-BBR-BBSW"),
  /**
   * The AUD-LIBOR-BBA ISDA index.
   */
  AUD_LIBOR_BBA(Currency.AUD, "LIBOR", "AUD-LIBOR-BBA"),
  /**
   * The CAD-BA-CDOR ISDA index.
   */
  CAD_BA_CDOR(Currency.CAD, "BA", "CAD-BA-CDOR"),
  /**
   * The CAD-LIBOR-BBA ISDA index.
   */
  CAD_LIBOR_BBA(Currency.CAD, "LIBOR", "CAD-LIBOR-BBA"),
  /**
   * The CAD-CORRA-OIS-COMPOUND ISDA index.
   */
  CAD_CORRA_OIS_COMPOUND(Currency.CAD, "CORRA", "CAD-CORRA-OIS-COMPOUND"),
  /**
   * The CHF-LIBOR-BBA ISDA index.
   */
  CHF_LIBOR_BBA(Currency.CHF, "LIBOR", "CHF-LIBOR-BBA"),
  /**
   * The CHF-TOIS-OIS-COMPOUND ISDA index.
   */
  CHF_TOIS_OIS_COMPOUND(Currency.CHF, "TOIS", "CHF-TOIS-OIS-COMPOUND"),
  /**
   * The CZK-PRIBOR-PRBO ISDA index.
   */
  CZK_PRIBOR_PRBO(Currency.CZK, "PRIBOR", "CZK-PRIBOR-PRBO"),
  /**
   * The DKK-CIBOR2-DKNA13 ISDA index.
   */
  DKK_CIBOR2_DKNA13(Currency.DKK, "CIBOR2", "DKK-CIBOR2-DKNA13"),
  /**
   * The DKK-CIBOR-DKNA13 ISDA index.
   */
  DKK_CIBOR_DKNA13(Currency.DKK, "CIBOR", "DKK-CIBOR-DKNA13"),
  /**
   * The EUR-EURIBOR-Reuters ISDA index.
   */
  EUR_EURIBOR_REUTERS(Currency.EUR, "EURIBOR", "EUR-EURIBOR-Reuters"),
  /**
   * The EUR-EURIBOR-Telerate ISDA index.
   */
  EUR_EURIBOR_TELERATE(Currency.EUR, "EURIBOR", "EUR-EURIBOR-Telerate"),
  /**
   * The EUR-LIBOR-BBA ISDA index.
   */
  EUR_LIBOR_BBA(Currency.EUR, "LIBOR", "EUR-LIBOR-BBA"),
  /**
   * The EUR-EONIA-OIS-COMPOUND ISDA index.
   */
  EUR_EONIA_OIS_COMPOUND(Currency.EUR, "EONIA", "EUR-EONIA-OIS-COMPOUND"),
  /**
   * The GBP-LIBOR-BBA ISDA index.
   */
  GBP_LIBOR_BBA(Currency.GBP, "LIBOR", "GBP-LIBOR-BBA"),
  /**
   * The GBP-WMBA-SONIA-COMPOUND ISDA index.
   */
  GBP_WMBA_SONIA_COMPOUND(Currency.GBP, "SONIA", "GBP-WMBA-SONIA-COMPOUND"),
  /**
   * The HKD-HIBOR-HIBOR ISDA index.
   */
  HKD_HIBOR_HIBOR(Currency.HKD, "HIBOR", "HKD-HIBOR-HIBOR"),
  /**
   * The HKD-HIBOR-HKAB ISDA index.
   */
  HKD_HIBOR_HKAB(Currency.HKD, "HIBOR", "HKD-HIBOR-HKAB"),
  /**
   * The HKD-HIBOR-ISDC ISDA index.
   */
  HKD_HIBOR_ISDC(Currency.HKD, "HIBOR", "HKD-HIBOR-ISDC"),
  /**
   * The HUF-BUBOR-Reuters ISDA index.
   */
  HUF_BUBOR_REUTERS(Currency.HUF, "BUBOR", "HUF-BUBOR-Reuters"),
  /**
   * The JPY-LIBOR-BBA ISDA index.
   */
  JPY_LIBOR_BBA(Currency.JPY, "LIBOR", "JPY-LIBOR-BBA"),
  /**
   * The JPY-TONA-OIS-COMPOUND ISDA index.
   */
  JPY_TONA_OIS_COMPOUND(Currency.JPY, "TONA", "JPY-TONA-OIS-COMPOUND"),
  /**
   * The NOK-NIBOR-NIBR ISDA index.
   */
  NOK_NIBOR_NIBR(Currency.NOK, "NIBOR", "NOK-NIBOR-NIBR"),
  /**
   * The MXN-TIIE-Banxico ISDA index.
   */
  MXN_TIIE_Banxico (Currency.of("MXN"), "TIIE", "MXN-TIIE-Banxico"),
  /**
   * The NZD-BBR-FRA ISDA index.
   */
  NZD_BBR_FRA(Currency.NZD, "BBR", "NZD-BBR-FRA"),
  /**
   * The NZD-BBR-FRA ISDA index.
   */
  NZD_LIBOR_BBA(Currency.NZD, "LIBOR", "NZD-LIBOR-BBA"),
  /**
   * The NZD-BBR-Telerate ISDA index.
   */
  NZD_BBR_TELERATE(Currency.NZD, "BBR", "NZD-BBR-Telerate"),
  /**
   * The PLN-WIBOR-WIBO ISDA index.
   */
  PLN_WIBOR_WIBO(Currency.of("PLN"), "WIBOR", "PLN-WIBOR-WIBO"),
  /**
   * The SEK-STIBOR-SIDE ISDA index.
   */
  SEK_STIBOR_SIDE(Currency.SEK, "STIBOR", "SEK-STIBOR-SIDE"),
  /**
   * The SGD-SOR-Reuters ISDA index.
   */
  SGD_SOR_REUTERS(Currency.of("SGD"), "SOR", "SGD-SOR-Reuters"),
  /**
   * The SGD-SOR-VWAP ISDA index.
   */
  SGD_SOR_VWAP(Currency.of("SGD"), "SOR", "SGD-SOR-VWAP"),
  /**
   * The USD-LIBOR-BBA ISDA index.
   */
  USD_LIBOR_BBA(Currency.USD, "LIBOR", "USD-LIBOR-BBA"),
  /**
   * The USD-Federal Funds-H.15 ISDA index.
   */
  USD_FEDFUND(Currency.USD, "FEDFUND", "USD-Federal Funds-H.15"),
  /**
   * The USD-Federal Funds-H.15-OIS_COMPOUND ISDA index.
   */
  USD_FEDFUND_OIS_COMPOUND(Currency.USD, "FEDFUND", "USD-Federal Funds-H.15-OIS-COMPOUND"),
  /**
   * The ZAR-JIBAR-SAFEX ISDA index.
   */
  ZAR_JIBAR_SAFEX(Currency.of("ZAR"), "JIBAR", "ZAR-JIBAR-SAFEX"),
  ;

  private final Currency _currency;
  private final String _indexName;
  private final String _isdaName;
  private final ExternalId _externalId;

  private FloatingIndex(Currency currency, String indexName, String isdaName) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(indexName, "indexName");
    ArgumentChecker.notNull(isdaName, "isdaName");
    _currency = currency;
    _indexName = indexName;
    _isdaName = isdaName;
    _externalId = ExternalSchemes.isda(getIsdaName());
  }

  /**
   * Gets the currency.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the indexName.
   * @return the indexName
   */
  public String getIndexName() {
    return _indexName;
  }

  /**
   * Gets the isdaName.
   * @return the isdaName
   */
  public String getIsdaName() {
    return _isdaName;
  }

  @Override
  public String getName() {
    return getIsdaName();
  }
  
  public ExternalId toRawExternalId() {
    return _externalId;
  }
  
  /**
   * Obtain the ID that should be provided as the index on a {@link FloatingInterestRateLeg}
   * for a floating leg with the specified frequency.
   * 
   * @param frequency the floating interest rate leg frequency
   * @return the identifier that should be used on the leg
   */
  public ExternalId toFrequencySpecificExternalId(Frequency frequency) {
    ArgumentChecker.notNull(frequency, "frequency");
    String idValue = getIsdaName() + "-";
    switch (frequency.getName()) {
      case Frequency.DAILY_NAME:
        idValue += "1D";
        break;
      case Frequency.WEEKLY_NAME:
        idValue += "1W";
        break;
      case Frequency.BIWEEKLY_NAME:
        idValue += "2W";
        break;
      case Frequency.THREE_WEEK_NAME:
        idValue += "3W";
        break;
      case Frequency.TWENTY_EIGHT_DAYS_NAME:
        idValue += "28D";
        break;
      case Frequency.MONTHLY_NAME:
        idValue += "1M";
        break;
      case Frequency.BIMONTHLY_NAME:
        idValue += "2M";
        break;
      case Frequency.QUARTERLY_NAME:
        idValue += "3M";
        break;
      case Frequency.FOUR_MONTH_NAME:
        idValue += "4M";
        break;
      case Frequency.FIVE_MONTH_NAME:
        idValue += "5M";
        break;
      case Frequency.SEMI_ANNUAL_NAME:
        idValue += "6M";
        break;
      case Frequency.SEVEN_MONTH_NAME:
        idValue += "7M";
        break;
      case Frequency.EIGHT_MONTH_NAME:
        idValue += "8M";
        break;
      case Frequency.NINE_MONTH_NAME:
        idValue += "9M";
        break;
      case Frequency.TEN_MONTH_NAME:
        idValue += "10M";
        break;
      case Frequency.ELEVEN_MONTH_NAME:
        idValue += "11M";
        break;
      case Frequency.ANNUAL_NAME:
        idValue += "12M";
        break;
      default:
        throw new IllegalArgumentException("Only standard IBOR frequencies supported. Frequency provided is " + frequency.getName());
    }
    return ExternalId.of(_externalId.getScheme(), idValue);
  }

}
