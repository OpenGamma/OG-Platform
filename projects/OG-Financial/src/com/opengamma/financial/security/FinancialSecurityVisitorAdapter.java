/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.BondSecurityVisitor;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurityVisitor;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurityVisitor;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cash.CashSecurityVisitor;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquitySecurityVisitor;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurityVisitor;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.FRASecurityVisitor;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurityVisitor;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurityVisitor;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurityVisitor;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurityVisitor;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurityVisitor;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurityVisitor;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurityVisitor;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurityVisitor;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurityVisitor;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurityVisitor;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurityVisitor;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurityVisitor;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurityVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurityVisitor;

/**
 * Adapter for visiting all concrete asset classes.
 * 
 * @param <T> Return type for visitor.
 */
public class FinancialSecurityVisitorAdapter<T> implements FinancialSecurityVisitor<T> {

  private final BondSecurityVisitor<T> _bondSecurityVisitor;
  private final CashSecurityVisitor<T> _cashSecurityVisitor;
  private final EquitySecurityVisitor<T> _equitySecurityVisitor;
  private final FRASecurityVisitor<T> _fraSecurityVisitor;
  private final FutureSecurityVisitor<T> _futureSecurityVisitor;
  private final SwapSecurityVisitor<T> _swapSecurityVisitor;
  private final EquityIndexOptionSecurityVisitor<T> _equityIndexOptionSecurityVisitor;
  private final EquityOptionSecurityVisitor<T> _equityOptionSecurityVisitor;
  private final EquityBarrierOptionSecurityVisitor<T> _equityBarrierOptionSecurityVisitor;
  private final FXOptionSecurityVisitor<T> _fxOptionSecurityVisitor;
  private final NonDeliverableFXOptionSecurityVisitor<T> _nonDeliverableFxOptionSecurityVisitor;
  private final SwaptionSecurityVisitor<T> _swaptionSecurityVisitor;
  //  private final InterestRateFutureSecurityVisitor<T> _irfutureSecurityVisitor;
  private final IRFutureOptionSecurityVisitor<T> _irfutureOptionSecurityVisitor;
  private final FXBarrierOptionSecurityVisitor<T> _fxBarrierOptionSecurityVisitor;
  private final FXDigitalOptionSecurityVisitor<T> _fxDigitalOptionSecurityVisitor;
  private final NonDeliverableFXDigitalOptionSecurityVisitor<T> _nonDeliverableFxDigitalOptionSecurityVisitor;
  private final FXForwardSecurityVisitor<T> _fxForwardSecurityVisitor;
  private final NonDeliverableFXForwardSecurityVisitor<T> _nonDeliverableFxForwardSecurityVisitor;
  private final CapFloorSecurityVisitor<T> _capFloorSecurityVisitor;
  private final CapFloorCMSSpreadSecurityVisitor<T> _capFloorCMSSpreadSecurityVisitor;
  private final EquityVarianceSwapSecurityVisitor<T> _equityVarianceSwapSecurityVisitor;
  private final EquityIndexDividendFutureOptionSecurityVisitor<T> _equityIndexDividendFutureOptionSecurityVisitor;

  /**
   * Builder for the visitor adapter.
   * 
   * @param <T> Return type for the visitor
   */
  public static final class Builder<T> {
    private BondSecurityVisitor<T> _bondSecurityVisitor;
    private CashSecurityVisitor<T> _cashSecurityVisitor;
    private EquitySecurityVisitor<T> _equitySecurityVisitor;
    private FRASecurityVisitor<T> _fraSecurityVisitor;
    private FutureSecurityVisitor<T> _futureSecurityVisitor;
    private SwapSecurityVisitor<T> _swapSecurityVisitor;
    private EquityIndexOptionSecurityVisitor<T> _equityIndexOptionSecurityVisitor;
    private EquityIndexDividendFutureOptionSecurityVisitor<T> _equityIndexDividendFutureOptionSecurityVisitor;
    private EquityOptionSecurityVisitor<T> _equityOptionSecurityVisitor;
    private EquityBarrierOptionSecurityVisitor<T> _equityBarrierOptionSecurityVisitor;
    private FXOptionSecurityVisitor<T> _fxOptionSecurityVisitor;
    private NonDeliverableFXOptionSecurityVisitor<T> _nonDeliverableFxOptionSecurityVisitor;
    private SwaptionSecurityVisitor<T> _swaptionSecurityVisitor;
    private IRFutureOptionSecurityVisitor<T> _irfutureSecurityVisitor;
    private FXBarrierOptionSecurityVisitor<T> _fxBarrierOptionSecurityVisitor;
    private FXDigitalOptionSecurityVisitor<T> _fxDigitalOptionSecurityVisitor;
    private NonDeliverableFXDigitalOptionSecurityVisitor<T> _nonDeliverableFxDigitalOptionSecurityVisitor;
    private FXForwardSecurityVisitor<T> _fxForwardSecurityVisitor;
    private NonDeliverableFXForwardSecurityVisitor<T> _nonDeliverableFxForwardSecurityVisitor;
    private CapFloorSecurityVisitor<T> _capFloorSecurityVisitor;
    private CapFloorCMSSpreadSecurityVisitor<T> _capFloorCMSSpreadSecurityVisitor;
    private EquityVarianceSwapSecurityVisitor<T> _equityVarianceSwapSecurityVisitor;

    private Builder() {
    }

    public Builder<T> bondSecurityVisitor(final BondSecurityVisitor<T> bondSecurityVisitor) {
      _bondSecurityVisitor = bondSecurityVisitor;
      return this;
    }

    public Builder<T> cashSecurityVisitor(final CashSecurityVisitor<T> cashSecurityVisitor) {
      _cashSecurityVisitor = cashSecurityVisitor;
      return this;
    }

    public Builder<T> equitySecurityVisitor(final EquitySecurityVisitor<T> equitySecurityVisitor) {
      _equitySecurityVisitor = equitySecurityVisitor;
      return this;
    }

    public Builder<T> fraSecurityVisitor(final FRASecurityVisitor<T> fraSecurityVisitor) {
      _fraSecurityVisitor = fraSecurityVisitor;
      return this;
    }

    public Builder<T> futureSecurityVisitor(final FutureSecurityVisitor<T> futureSecurityVisitor) {
      _futureSecurityVisitor = futureSecurityVisitor;
      return this;
    }

    public Builder<T> swapSecurityVisitor(final SwapSecurityVisitor<T> swapSecurityVisitor) {
      _swapSecurityVisitor = swapSecurityVisitor;
      return this;
    }

    public Builder<T> equityIndexOptionVisitor(final EquityIndexOptionSecurityVisitor<T> equityIndexOptionSecurityVisitor) {
      _equityIndexOptionSecurityVisitor = equityIndexOptionSecurityVisitor;
      return this;
    }

    public Builder<T> equityIndexDividendFutureOptionVisitor(final EquityIndexDividendFutureOptionSecurityVisitor<T> equityIndexDividendFutureOptionSecurityVisitor) {
      _equityIndexDividendFutureOptionSecurityVisitor = equityIndexDividendFutureOptionSecurityVisitor;
      return this;
    }

    public Builder<T> equityOptionVisitor(final EquityOptionSecurityVisitor<T> equityOptionSecurityVisitor) {
      _equityOptionSecurityVisitor = equityOptionSecurityVisitor;
      return this;
    }

    public Builder<T> equityBarrierOptionVisitor(final EquityBarrierOptionSecurityVisitor<T> equityBarrierOptionSecurityVisitor) {
      _equityBarrierOptionSecurityVisitor = equityBarrierOptionSecurityVisitor;
      return this;
    }

    public Builder<T> fxOptionVisitor(final FXOptionSecurityVisitor<T> fxOptionSecurityVisitor) {
      _fxOptionSecurityVisitor = fxOptionSecurityVisitor;
      return this;
    }

    public Builder<T> nonDeliverableFxOptionVisitor(final NonDeliverableFXOptionSecurityVisitor<T> nonDeliverableFxOptionSecurityVisitor) {
      _nonDeliverableFxOptionSecurityVisitor = nonDeliverableFxOptionSecurityVisitor;
      return this;
    }

    public Builder<T> swaptionVisitor(final SwaptionSecurityVisitor<T> swaptionSecurityVisitor) {
      _swaptionSecurityVisitor = swaptionSecurityVisitor;
      return this;
    }

    public Builder<T> irfutureOptionVisitor(final IRFutureOptionSecurityVisitor<T> irfutureSecurityVisitor) {
      _irfutureSecurityVisitor = irfutureSecurityVisitor;
      return this;
    }

    public Builder<T> fxBarrierOptionVisitor(final FXBarrierOptionSecurityVisitor<T> fxBarrierOptionSecurityVisitor) {
      _fxBarrierOptionSecurityVisitor = fxBarrierOptionSecurityVisitor;
      return this;
    }

    public Builder<T> fxDigitalOptionVisitor(final FXDigitalOptionSecurityVisitor<T> fxSecurityVisitor) {
      _fxDigitalOptionSecurityVisitor = fxSecurityVisitor;
      return this;
    }

    public Builder<T> fxNonDeliverableDigitalOptionVisitor(final NonDeliverableFXDigitalOptionSecurityVisitor<T> nonDeliverableFxDigitalOptionSecurityVisitor) {
      _nonDeliverableFxDigitalOptionSecurityVisitor = nonDeliverableFxDigitalOptionSecurityVisitor;
      return this;
    }

    public Builder<T> fxForwardVisitor(final FXForwardSecurityVisitor<T> fxForwardSecurityVisitor) {
      _fxForwardSecurityVisitor = fxForwardSecurityVisitor;
      return this;
    }

    public Builder<T> nonDeliverableFxForwardVisitor(final NonDeliverableFXForwardSecurityVisitor<T> nonDeliverableFxForwardSecurityVisitor) {
      _nonDeliverableFxForwardSecurityVisitor = nonDeliverableFxForwardSecurityVisitor;
      return this;
    }

    public Builder<T> capFloorVisitor(final CapFloorSecurityVisitor<T> capFloorSecurityVisitor) {
      _capFloorSecurityVisitor = capFloorSecurityVisitor;
      return this;
    }

    public Builder<T> capFloorCMSSpreadVisitor(final CapFloorCMSSpreadSecurityVisitor<T> capFloorCMSSpreadSecurityVisitor) {
      _capFloorCMSSpreadSecurityVisitor = capFloorCMSSpreadSecurityVisitor;
      return this;
    }

    public Builder<T> equityVarianceSwapSecurityVisitor(final EquityVarianceSwapSecurityVisitor<T> equityVarianceSwapSecurityVisitor) {
      _equityVarianceSwapSecurityVisitor = equityVarianceSwapSecurityVisitor;
      return this;
    }

    public FinancialSecurityVisitorAdapter<T> create() {
      return new FinancialSecurityVisitorAdapter<T>(this);
    }

  }

  public FinancialSecurityVisitorAdapter(
      final BondSecurityVisitor<T> bondSecurityVisitor, //CSIGNORE
      final CashSecurityVisitor<T> cashSecurityVisitor, final EquitySecurityVisitor<T> equitySecurityVisitor, final FRASecurityVisitor<T> fraSecurityVisitor,
      final FutureSecurityVisitor<T> futureSecurityVisitor, final SwapSecurityVisitor<T> swapSecurityVisitor, final EquityIndexOptionSecurityVisitor<T> equityIndexOptionSecurityVisitor,
      final EquityIndexDividendFutureOptionSecurityVisitor<T> equityIndexDividendFutureOptionSecurityVisitor, final EquityOptionSecurityVisitor<T> equityOptionSecurityVisitor,
      final EquityBarrierOptionSecurityVisitor<T> equityBarrierOptionSecurityVisitor, final FXOptionSecurityVisitor<T> fxOptionSecurityVisitor,
      final NonDeliverableFXOptionSecurityVisitor<T> nonDeliverableFxOptionSecurityVisitor, final SwaptionSecurityVisitor<T> swaptionSecurityVisitor,
      final IRFutureOptionSecurityVisitor<T> irfutureOptionSecurityVisitor, final FXBarrierOptionSecurityVisitor<T> fxBarrierOptionSecurityVisitor,
      final FXDigitalOptionSecurityVisitor<T> fxDigitalOptionSecurityVisitor, final NonDeliverableFXDigitalOptionSecurityVisitor<T> nonDeliverableFxDigitalOptionSecurityVisitor,
      final FXForwardSecurityVisitor<T> fxForwardSecurityVisitor, final NonDeliverableFXForwardSecurityVisitor<T> nonDeliverableFxForwardSecurityVisitor,
      final CapFloorSecurityVisitor<T> capFloorSecurityVisitor, final CapFloorCMSSpreadSecurityVisitor<T> capFloorCMSSpreadSecurityVisitor,
      final EquityVarianceSwapSecurityVisitor<T> equityVarianceSwapSecurityVisitor) {
    _bondSecurityVisitor = bondSecurityVisitor;
    _cashSecurityVisitor = cashSecurityVisitor;
    _equitySecurityVisitor = equitySecurityVisitor;
    _fraSecurityVisitor = fraSecurityVisitor;
    _futureSecurityVisitor = futureSecurityVisitor;
    _swapSecurityVisitor = swapSecurityVisitor;
    _equityIndexOptionSecurityVisitor = equityIndexOptionSecurityVisitor;
    _equityIndexDividendFutureOptionSecurityVisitor = equityIndexDividendFutureOptionSecurityVisitor;
    _equityOptionSecurityVisitor = equityOptionSecurityVisitor;
    _equityBarrierOptionSecurityVisitor = equityBarrierOptionSecurityVisitor;
    _fxOptionSecurityVisitor = fxOptionSecurityVisitor;
    _nonDeliverableFxOptionSecurityVisitor = nonDeliverableFxOptionSecurityVisitor;
    _swaptionSecurityVisitor = swaptionSecurityVisitor;
    _irfutureOptionSecurityVisitor = irfutureOptionSecurityVisitor;
    _fxBarrierOptionSecurityVisitor = fxBarrierOptionSecurityVisitor;
    _fxDigitalOptionSecurityVisitor = fxDigitalOptionSecurityVisitor;
    _nonDeliverableFxDigitalOptionSecurityVisitor = nonDeliverableFxDigitalOptionSecurityVisitor;
    _fxForwardSecurityVisitor = fxForwardSecurityVisitor;
    _nonDeliverableFxForwardSecurityVisitor = nonDeliverableFxForwardSecurityVisitor;
    _capFloorSecurityVisitor = capFloorSecurityVisitor;
    _capFloorCMSSpreadSecurityVisitor = capFloorCMSSpreadSecurityVisitor;
    _equityVarianceSwapSecurityVisitor = equityVarianceSwapSecurityVisitor;
  }

  public FinancialSecurityVisitorAdapter() {
    this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  public static <T> Builder<T> builder() {
    return new Builder<T>();
  }

  protected FinancialSecurityVisitorAdapter(final Builder<T> builder) {
    this(builder._bondSecurityVisitor, builder._cashSecurityVisitor, builder._equitySecurityVisitor, builder._fraSecurityVisitor, builder._futureSecurityVisitor, builder._swapSecurityVisitor,
        builder._equityIndexOptionSecurityVisitor, builder._equityIndexDividendFutureOptionSecurityVisitor, builder._equityOptionSecurityVisitor, builder._equityBarrierOptionSecurityVisitor,
        builder._fxOptionSecurityVisitor, builder._nonDeliverableFxOptionSecurityVisitor, builder._swaptionSecurityVisitor, builder._irfutureSecurityVisitor, builder._fxBarrierOptionSecurityVisitor,
        builder._fxDigitalOptionSecurityVisitor, builder._nonDeliverableFxDigitalOptionSecurityVisitor, builder._fxForwardSecurityVisitor, builder._nonDeliverableFxForwardSecurityVisitor,
        builder._capFloorSecurityVisitor, builder._capFloorCMSSpreadSecurityVisitor, builder._equityVarianceSwapSecurityVisitor);
  }

  // FinancialSecurityVisitor

  @Override
  public T visitBondSecurity(final BondSecurity security) {
    return (_bondSecurityVisitor != null) ? security.accept(_bondSecurityVisitor) : null;
  }

  @Override
  public T visitCashSecurity(final CashSecurity security) {
    return (_cashSecurityVisitor != null) ? security.accept(_cashSecurityVisitor) : null;
  }

  @Override
  public T visitEquitySecurity(final EquitySecurity security) {
    return (_equitySecurityVisitor != null) ? security.accept(_equitySecurityVisitor) : null;
  }

  @Override
  public T visitFRASecurity(final FRASecurity security) {
    return (_fraSecurityVisitor != null) ? security.accept(_fraSecurityVisitor) : null;
  }

  @Override
  public T visitFutureSecurity(final FutureSecurity security) {
    return (_futureSecurityVisitor != null) ? security.accept(_futureSecurityVisitor) : null;
  }

  @Override
  public T visitSwapSecurity(final SwapSecurity security) {
    return (_swapSecurityVisitor != null) ? security.accept(_swapSecurityVisitor) : null;
  }

  @Override
  public T visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return (_equityIndexOptionSecurityVisitor != null) ? security.accept(_equityIndexOptionSecurityVisitor) : null;
  }

  @Override
  public T visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return (_equityOptionSecurityVisitor != null) ? security.accept(_equityOptionSecurityVisitor) : null;
  }

  @Override
  public T visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
    return (_equityBarrierOptionSecurityVisitor != null) ? security.accept(_equityBarrierOptionSecurityVisitor) : null;
  }

  @Override
  public T visitFXOptionSecurity(final FXOptionSecurity security) {
    return (_fxOptionSecurityVisitor != null) ? security.accept(_fxOptionSecurityVisitor) : null;
  }

  @Override
  public T visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
    return _nonDeliverableFxOptionSecurityVisitor != null ? security.accept(_nonDeliverableFxOptionSecurityVisitor) : null;
  }

  @Override
  public T visitSwaptionSecurity(final SwaptionSecurity security) {
    return (_swaptionSecurityVisitor != null) ? security.accept(_swaptionSecurityVisitor) : null;
  }

  //  @Override
  //  public T visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
  //    return (_irfutureSecurityVisitor != null) ? security.accept(_irfutureSecurityVisitor) : null;
  //  }

  @Override
  public T visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return (_irfutureOptionSecurityVisitor != null) ? security.accept(_irfutureOptionSecurityVisitor) : null;
  }

  @Override
  public T visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
    return (_fxBarrierOptionSecurityVisitor != null) ? security.accept(_fxBarrierOptionSecurityVisitor) : null;
  }

  @Override
  public T visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
    return (_fxDigitalOptionSecurityVisitor != null) ? security.accept(_fxDigitalOptionSecurityVisitor) : null;
  }

  @Override
  public T visitFXForwardSecurity(final FXForwardSecurity security) {
    return (_fxForwardSecurityVisitor != null) ? security.accept(_fxForwardSecurityVisitor) : null;
  }

  @Override
  public T visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
    return (_nonDeliverableFxForwardSecurityVisitor != null) ? security.accept(_nonDeliverableFxForwardSecurityVisitor) : null;
  }

  @Override
  public T visitCapFloorSecurity(final CapFloorSecurity security) {
    return (_capFloorSecurityVisitor != null) ? security.accept(_capFloorSecurityVisitor) : null;
  }

  @Override
  public T visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
    return (_capFloorCMSSpreadSecurityVisitor != null) ? security.accept(_capFloorCMSSpreadSecurityVisitor) : null;
  }

  @Override
  public T visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
    return (_equityVarianceSwapSecurityVisitor != null) ? security.accept(_equityVarianceSwapSecurityVisitor) : null;
  }

  @Override
  public T visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity security) {
    return (_equityIndexDividendFutureOptionSecurityVisitor != null) ? security.accept(_equityIndexDividendFutureOptionSecurityVisitor) : null;
  }

  @Override
  public T visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity security) {
    return (_nonDeliverableFxDigitalOptionSecurityVisitor != null) ? security.accept(_nonDeliverableFxDigitalOptionSecurityVisitor) : null;
  }
}
