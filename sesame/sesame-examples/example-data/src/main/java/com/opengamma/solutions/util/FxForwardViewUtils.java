package com.opengamma.solutions.util;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.CurveSelectorMulticurveBundleFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.fxforward.DiscountingFXForwardPVFn;
import com.opengamma.sesame.fxforward.FXForwardPVFn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.trade.FXForwardTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import java.math.BigDecimal;
import java.util.List;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.column;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;

/**
 * Utility class for Fx Forward views
 */
public class FxForwardViewUtils {

  private FxForwardViewUtils(){/*Private Constructor*/}

  /** List of FX Forward Trades */
  public static final List<Object> FX_TRADE_INPUTS =
      ImmutableList.<Object>of(createFxForwardTrade());

  /** List of FX Forward Securities */
  public static final List<Object> FX_SECURITY_INPUTS =
      ImmutableList.<Object>of(createFxForwardSecurity());

  /**
   * Utility for creating a credit specific view column
   * @param exposureConfig exposure function, not null
   * @param currencyMatrixLink currency matrix, not null
   */
  public static ViewConfig createViewConfig(ConfigLink<ExposureFunctions> exposureConfig, ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    return
        configureView(
            "FX Forward Remote view",
            config(
                arguments(
                    function(
                        MarketExposureSelector.class,
                        argument("exposureFunctions", exposureConfig)),
                    function(
                        DefaultHistoricalMarketDataFn.class,
                        argument("currencyMatrix", currencyMatrixLink))),
                implementations(
                    CurveSelector.class, MarketExposureSelector.class,
                    DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class,
                    FXForwardPVFn.class, DiscountingFXForwardPVFn.class)),
            column(OutputNames.FX_PRESENT_VALUE));
  }

  /**
   * Create an instance of a Fx Forward Trade
   * @return FXForwardTrade
   */
  private static FXForwardTrade createFxForwardTrade() {

    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(1);
    LocalDate tradeDate = LocalDate.of(2014, 7, 11);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(createFxForwardSecurity(), tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(0.00);
    trade.setPremiumDate(LocalDate.of(2014, 7, 25));
    trade.setPremiumCurrency(Currency.GBP);

    FXForwardTrade fxForwardTrade = new FXForwardTrade(trade);

    return fxForwardTrade;
  }

  /**
   * Create an instance of a Fx Forward Security
   * @return FXForwardSecurity
   */
  private static FXForwardSecurity createFxForwardSecurity() {

    Currency payCurrency = Currency.GBP;
    Currency recCurrency = Currency.USD;

    double payAmount = 1_000_000;
    double recAmount = 1_600_000;

    ZonedDateTime forwardDate = DateUtils.getUTCDate(2019, 2, 4);

    ExternalId region = ExternalSchemes.currencyRegionId(Currency.GBP);

    FXForwardSecurity fxForwardSecurity = new FXForwardSecurity(payCurrency, payAmount, recCurrency, recAmount, forwardDate, region);

    return fxForwardSecurity;
  }


}
