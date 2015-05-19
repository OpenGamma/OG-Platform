package com.opengamma.sesame.bondfutureoption;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesExpStrikeProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.IssuerProviderBundle;
import com.opengamma.sesame.IssuerProviderFn;
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
import com.opengamma.sesame.trade.BondFutureOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Default implementation to return instances of {@link BlackBondFuturesProviderInterface}.
 */
public class BlackExpStrikeBondFuturesProviderFn implements BlackBondFuturesProviderFn {

  private final IssuerProviderFn _issuerProviderFn;

  /**
   * Constructors a black volatility provider function for bond future options.
   * @param issuerProviderFn the issuer provider function, not null.
   */
  public BlackExpStrikeBondFuturesProviderFn(IssuerProviderFn issuerProviderFn) {
    ArgumentChecker.notNull(issuerProviderFn, "discountingMulticurveCombinerFn");
    _issuerProviderFn = issuerProviderFn;
  }

  @Override
  public Result<BlackBondFuturesProviderInterface> getBlackBondFuturesProvider(Environment env,
                                                                               BondFutureOptionTrade tradeWrapper) {
    BondFutureOptionSecurity security = tradeWrapper.getSecurity();
    //TODO can we use a dummy legal entity here?
    LegalEntity legalEntity = new LegalEntity("", "", Sets.<CreditRating>newHashSet(), Sector.of(""), Region.of(""));
    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, tradeWrapper.getTrade());
    Result<VolatilitySurface> surfaceResult =
        env.getMarketDataBundle().get(VolatilitySurfaceId.of(security.getTradingExchange()), VolatilitySurface.class);

    if (Result.allSuccessful(bundleResult, surfaceResult)) {
      IssuerProviderDiscount multicurve = (IssuerProviderDiscount) bundleResult.getValue().getParameterIssuerProvider();
      VolatilitySurface volSurface = surfaceResult.getValue();

      BlackBondFuturesProviderInterface black = new BlackBondFuturesExpStrikeProvider(multicurve,
                                                                                      volSurface.getSurface(),
                                                                                      legalEntity);
      return Result.success(black);
    } else {
      return Result.failure(bundleResult, surfaceResult);
    }
  }
}
