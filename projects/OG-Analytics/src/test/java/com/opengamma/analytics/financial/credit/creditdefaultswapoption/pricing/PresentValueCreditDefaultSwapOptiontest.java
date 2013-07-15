/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.pricing;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CDSOptionExerciseType;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CDSOptionKnockoutType;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CDSOptionType;
import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Class to test the calculation of PV's for CDS swaptions
 */
public class PresentValueCreditDefaultSwapOptiontest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : 

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The CDS swaption contract parameters

  private static final BuySellProtection cdsSwaptionBuySellProtection = BuySellProtection.BUY;

  private static final String cdsSwaptionProtectionBuyerTicker = "BARC";
  private static final String cdsSwaptionProtectionBuyerShortName = "Barclays";
  private static final String cdsSwaptionProtectionBuyerREDCode = "ABC123";

  private static final String cdsSwaptionProtectionSellerTicker = "RBS";
  private static final String cdsSwaptionProtectionSellerShortName = "Royal Bank of Scotland";
  private static final String cdsSwaptionProtectionSellerREDCode = "XYZ321";

  private static final CreditRating cdsSwaptionProtectionBuyerCompositeRating = CreditRating.AA;
  private static final CreditRating cdsSwaptionProtectionBuyerImpliedRating = CreditRating.A;

  private static final CreditRating cdsSwaptionProtectionSellerCompositeRating = CreditRating.AA;
  private static final CreditRating cdsSwaptionProtectionSellerImpliedRating = CreditRating.A;

  private static final CreditRatingMoodys cdsSwaptionProtectionBuyerCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors cdsSwaptionProtectionBuyerCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch cdsSwaptionProtectionBuyerCreditRatingFitch = CreditRatingFitch.AA;

  private static final boolean cdsSwaptionProtectionBuyerHasDefaulted = false;

  private static final CreditRatingMoodys cdsSwaptionProtectionSellerCreditRatingMoodys = CreditRatingMoodys.AA;
  private static final CreditRatingStandardAndPoors cdsSwaptionProtectionSellerCreditRatingStandardAndPoors = CreditRatingStandardAndPoors.A;
  private static final CreditRatingFitch cdsSwaptionProtectionSellerCreditRatingFitch = CreditRatingFitch.AA;

  private static final boolean cdsSwaptionProtectionSellerHasDefaulted = false;

  private static final Sector cdsSwaptionProtectionBuyerSector = Sector.FINANCIALS;
  private static final Region cdsSwaptionProtectionBuyerRegion = Region.EUROPE;
  private static final String cdsSwaptionProtectionBuyerCountry = "United Kingdom";

  private static final Sector cdsSwaptionProtectionSellerSector = Sector.FINANCIALS;
  private static final Region cdsSwaptionProtectionSellerRegion = Region.EUROPE;
  private static final String cdsSwaptionProtectionSellerCountry = "United Kingdom";

  private static final Currency cdsSwaptionCurrency = Currency.EUR;

  private static final ZonedDateTime cdsSwaptionStartDate = DateUtils.getUTCDate(2012, 3, 20);
  private static final ZonedDateTime cdsSwaptionValuationDate = DateUtils.getUTCDate(2013, 4, 2);
  private static final ZonedDateTime cdsSwaptionEffectiveDate = cdsSwaptionValuationDate.plusDays(1);
  private static final ZonedDateTime cdsSwaptionExerciseDate = DateUtils.getUTCDate(2008, 3, 21);
  private static final ZonedDateTime cdsSwaptionMaturityDate = DateUtils.getUTCDate(2013, 3, 20);

  private static final double cdsSwaptionNotional = 10000000.0;
  private static final double cdsSwaptionStrike = 100.0;

  private static final CDSOptionKnockoutType cdsSwaptionKnockoutType = CDSOptionKnockoutType.KNOCKOUT;
  private static final CDSOptionType cdsSwaptionType = CDSOptionType.PAYER;
  private static final CDSOptionExerciseType cdsSwaptionExerciseType = CDSOptionExerciseType.EUROPEAN;

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
