/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CDSOptionExerciseType;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitor;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CreditDefaultSwapOptionSecurityConverter extends FinancialSecurityVisitorAdapter<CreditDefaultSwapOptionDefinition> {
  private static final Obligor DUMMY_OBLIGOR_A = new Obligor(
      "Dummy_A",
      "Dummy_A",
      "Dummy_A",
      CreditRating.A,
      CreditRating.A,
      CreditRatingMoodys.A,
      CreditRatingStandardAndPoors.A,
      CreditRatingFitch.A,
      false,
      Sector.BASICMATERIALS,
      Region.EUROPE,
      "CA");
  private static final Obligor DUMMY_OBLIGOR_B = new Obligor(
      "Dummy_B",
      "Dummy_B",
      "Dummy_B",
      CreditRating.A,
      CreditRating.A,
      CreditRatingMoodys.A,
      CreditRatingStandardAndPoors.A,
      CreditRatingFitch.A,
      false,
      Sector.BASICMATERIALS,
      Region.ASIA,
      "NY");
  private final SecuritySource _securitySource;
  private final CreditDefaultSwapSecurityConverter _underlyingConverter;

  public CreditDefaultSwapOptionSecurityConverter(final SecuritySource securitySource, final HolidaySource holidaySource, final RegionSource regionSource,
      final OrganizationSource organizationSource) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(organizationSource, "organization source");
    _securitySource = securitySource;
    _underlyingConverter = new CreditDefaultSwapSecurityConverter(holidaySource, regionSource, organizationSource);
  }

  @Override
  public CreditDefaultSwapOptionDefinition visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final BuySellProtection buySellProtection = security.isBuy() ? BuySellProtection.BUY : BuySellProtection.SELL;
    final Obligor protectionBuyer = DUMMY_OBLIGOR_A;
    final Obligor protectionSeller = DUMMY_OBLIGOR_B;
    final Currency currency = security.getCurrency();
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime maturityDate = security.getMaturityDate();
    final double notional = security.getNotional();
    final double strike = security.getStrike();
    final boolean isKnockOut = security.isKnockOut();
    final boolean isPayer = security.isPayer();
    final CDSOptionExerciseType optionExerciseType = convertExerciseType(security.getExerciseType());
    final FinancialSecurity underlyingSecurity = (FinancialSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId())); //TODO version correction
    final CreditDefaultSwapDefinition underlyingCDS = underlyingSecurity.accept(_underlyingConverter);
    //    underlyingCDS = underlyingCDS.withMaturityDate(maturityDate.plusYears(10));
    //    underlyingCDS = underlyingCDS.withEffectiveDate(maturityDate.plusDays(1));
    //    underlyingCDS = underlyingCDS.withStartDate(maturityDate);
    return new CreditDefaultSwapOptionDefinition(buySellProtection, protectionBuyer, protectionSeller, currency, startDate, maturityDate, notional, strike,
        isKnockOut, isPayer, optionExerciseType, underlyingCDS);
  }

  private static CDSOptionExerciseType convertExerciseType(final ExerciseType exerciseType) {
    final ExerciseTypeVisitor<CDSOptionExerciseType> visitor = new ExerciseTypeVisitor<CDSOptionExerciseType>() {

      @Override
      public CDSOptionExerciseType visitAmericanExerciseType(final AmericanExerciseType exerciseType) {
        return CDSOptionExerciseType.AMERICAN;
      }

      @Override
      public CDSOptionExerciseType visitAsianExerciseType(final AsianExerciseType exerciseType) {
        throw new UnsupportedOperationException();
      }

      @Override
      public CDSOptionExerciseType visitBermudanExerciseType(final BermudanExerciseType exerciseType) {
        return CDSOptionExerciseType.BERMUDAN;
      }

      @Override
      public CDSOptionExerciseType visitEuropeanExerciseType(final EuropeanExerciseType exerciseType) {
        return CDSOptionExerciseType.EUROPEAN;
      }
    };
    return exerciseType.accept(visitor);
  }
}
