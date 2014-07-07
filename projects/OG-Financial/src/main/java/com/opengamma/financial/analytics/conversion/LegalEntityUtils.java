/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.financial.security.CurrencyVisitor;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;

/**
 * Utility converter from com.opengamma.core.legalentity.LegalEntity to 
 * com.opengamma.analytics.financial.legalentity.Legalentity
 */
public class LegalEntityUtils {

  /** Rating agency strings */
  public static final String[] RATING_STRINGS = new String[] {"RatingMoody", "RatingFitch" };
  /** Sector name string */
  public static final String SECTOR_STRING = "IndustrySector";
  /** Market type string */
  public static final String MARKET_STRING = "Market";
  /** Market type string */
  private static CurrencyVisitor s_currencyVisitor = CurrencyVisitor.getInstance();
  
  public static LegalEntity convertFrom(com.opengamma.core.legalentity.LegalEntity entity, FinancialSecurity security) {
    Collection<Rating> ratings = entity.getRatings();
    String shortName = entity.getName();
    Set<CreditRating> creditRatings = null;
    for (Rating rating : ratings) {
      if (creditRatings == null) {
        creditRatings = new HashSet<>();
      }
      //TODO seniority level needs to go into the credit rating
      creditRatings.add(CreditRating.of(rating.getRater(), rating.getScore().toString(), true));
    }
    Region region = Region.of(entity.getName(), null, security.accept(s_currencyVisitor));
    return new LegalEntity(getTicker(security), shortName, creditRatings, null, region);
  }

  /**
   * Constructs a legal entity for a {@link BondSecurity}
   * @param tradeAttributes The trade attributes
   * @param security The bond security
   * @return A legal entity
   */
  public static LegalEntity getLegalEntityForBond(Map<String, String> tradeAttributes, BondSecurity security) {
    Map<String, String> securityAttributes = security.getAttributes();
    String shortName = security.getIssuerName();
    Set<CreditRating> creditRatings = null;
    for (String ratingString : RATING_STRINGS) {
      if (securityAttributes.containsKey(ratingString)) {
        if (creditRatings == null) {
          creditRatings = new HashSet<>();
        }
        creditRatings.add(CreditRating.of(securityAttributes.get(ratingString), ratingString, true));
      }
      if (tradeAttributes.containsKey(ratingString)) {
        if (creditRatings == null) {
          creditRatings = new HashSet<>();
        }
        creditRatings.add(CreditRating.of(tradeAttributes.get(ratingString), ratingString, true));
      }
    }
    String sectorName = security.getIssuerType();
    FlexiBean classifications = new FlexiBean();
    classifications.put(MARKET_STRING, security.getMarket());
    if (tradeAttributes.containsKey(SECTOR_STRING)) {
      classifications.put(SECTOR_STRING, tradeAttributes.get(SECTOR_STRING));
    }
    Sector sector = Sector.of(sectorName, classifications);
    Region region;
    if (security.getIssuerDomicile().equals("SNAT")) { // Supranational
      region = Region.of(security.getIssuerDomicile(), null, security.getCurrency());
    } else {
      region = Region.of(security.getIssuerDomicile(), Country.of(security.getIssuerDomicile()), security.getCurrency());
    }
    LegalEntity legalEntity = new LegalEntity(getTicker(security), shortName, creditRatings, sector, region);
    return legalEntity;
  }

  //TODO is the hardcoded usage of ISIN correct?
  private static String getTicker(FinancialSecurity security) {
    ExternalIdBundle identifiers = security.getExternalIdBundle();
    String ticker;
    if (identifiers != null) {
      String isin = identifiers.getValue(ExternalSchemes.ISIN);
      ticker = isin == null ? null : isin;
    } else {
      ticker = null;
    }
    return ticker;

  }

}
