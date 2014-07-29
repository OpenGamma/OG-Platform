/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedCompoundedONCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts swaptions from {@link SwaptionSecurity} to the {@link InstrumentDefinition}s.
 */
public class SwaptionSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {

  private final SwapSecurityConverter _swapConverter;
  private final InterestRateSwapSecurityConverter _irsSwapConverter;

  /**
   * @param swapConverter the underlying swap converter (for old-style swaps), not null
   * @param irsConverter the underlying swap converter (for new-style IRSs), not null
   */
  public SwaptionSecurityConverter(final SwapSecurityConverter swapConverter,
                                   final InterestRateSwapSecurityConverter irsConverter) {

    _swapConverter = ArgumentChecker.notNull(swapConverter, "swapConverter");
    _irsSwapConverter = ArgumentChecker.notNull(irsConverter, "irsConverter");
  }

  @Override
  public InstrumentDefinition<?> visitSwaptionSecurity(final SwaptionSecurity swaptionSecurity) {

    ArgumentChecker.notNull(swaptionSecurity, "swaption security");
    final ZonedDateTime expiry = swaptionSecurity.getExpiry().getExpiry();

    final FinancialSecurity underlyingSecurity = swaptionSecurity.getUnderlyingLink().resolve();

    final FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> converter;
    final boolean isCall;

    if (underlyingSecurity instanceof InterestRateSwapSecurity) {

      final InterestRateSwapSecurity swapSecurity = (InterestRateSwapSecurity) underlyingSecurity;
      isCall = swapSecurity.getPayLeg() instanceof FixedInterestRateSwapLeg;
      converter = _irsSwapConverter;
    } else {

      final SwapSecurity swapSecurity = (SwapSecurity) underlyingSecurity;
      isCall = swapSecurity.getPayLeg() instanceof FixedInterestRateLeg;
      converter = _swapConverter;
    }

    final SwapDefinition swapDefinition = (SwapDefinition) underlyingSecurity.accept(converter);
    final boolean isCashSettled = swaptionSecurity.isCashSettled();
    final boolean isLong = swaptionSecurity.isLong();

    if (swaptionSecurity.getCurrency().equals(Currency.BRL)) {

      if (swapDefinition instanceof SwapFixedCompoundedONCompoundedDefinition) {

        final SwapFixedCompoundedONCompoundedDefinition onSwapDefinition =
            (SwapFixedCompoundedONCompoundedDefinition) swapDefinition;
        return isCashSettled ?
            SwaptionCashFixedCompoundedONCompoundingDefinition.from(expiry, onSwapDefinition, isCall, isLong) :
            SwaptionPhysicalFixedCompoundedONCompoundedDefinition.from(expiry, onSwapDefinition, isCall, isLong);

      } else {
        throw new OpenGammaRuntimeException("Underlying BRL swap must be fixed compounded / overnight compounded - received: " +
                                                swapDefinition.getClass());
      }
    }

    if (swapDefinition instanceof SwapFixedIborDefinition) {

      final SwapFixedIborDefinition fixedIbor = (SwapFixedIborDefinition) swapDefinition;
      return isCashSettled ?
          SwaptionCashFixedIborDefinition.from(expiry, fixedIbor, isCall, isLong) :
          SwaptionPhysicalFixedIborDefinition.from(expiry, fixedIbor, isCall, isLong);
    } else {
      throw new OpenGammaRuntimeException("Underlying swap of a swaption must be a fixed / ibor swap - received: " +
                                              swapDefinition.getClass());
    }
  }
}
