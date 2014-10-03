##############
Variance Swaps
##############

 
Theory
======


The payoff of an equity variance swap is given by: 

.. math::
   \text{VS}(T) = N_{var}\left[\frac{A}{n}\sum^{n}_{i=1}\left(\log\left(\frac{S_i}{S_{i-1}}\right)\right)^2 - K^2\right]

where :math:`S_i` is the closing price of a stock or index on the  i\ :sup:`th` observation date, the annualisation factor
:math:`A` is set by the contract (and usually 252), :math:`N_{var}` is the variance notional and :math:`K^2`  (the variance
strike) is usually chosen to make the initial value zero.  The notional is often quoted in units of volatility (i.e.
\$1M per vol), :math:`N_{vol}`, with :math:`N_{var} = \frac{N_{vol}}{2K}`. The only part of this equation that is not
fixed (by contract) is the *realized variance*:

.. math:: \operatorname{RV}(T) = \sum^{n}_{i=1}\left(\log\left(\frac{S_i}{S_{i-1}}\right)\right)^2
   :label: realisedVar
  

Provided that the dynamics of the underlying does not contain jumps (e.g. discretely paid dividends), then Neuberger's formula [Neuberger1992]_ states:

.. math::
   \operatorname{RV}(T) \approx -2\log\left(\frac{S_T}{S_0}\right) + 2\sum_{i=1}^n\frac{1}{S_{i-1}}(S_i-S_{i-1})
   
What this says is that you can replicate the realised variance up to the expiry, :math:`T`, by holding :math:`2\log(S_0)` zero coupon bonds with expiry at :math:`T`,
shorting two log-contracts\ [#]_ and daily delta hedging with a portfolio whose delta is :math:`2/S_{i-1}`.

The expected value of the realised variance  (or just expected variance) is

.. math:: \operatorname{EV}(T) = 2\log(F_T) - 2\mathbb{E}[\log(S_T)]

and therefore the fair value of the variance strike is 

.. math:: K = \sqrt{\frac{2A}{n}\left(\log(F_T) - \mathbb{E}[\log(S_T)]\right)}

where :math:`F_T` is the forward price.  This is model independent provided the stock process is a diffusion, [GAT.2006.1]_. 


Static Replication
------------------

For a payoff at time  :math:`T` of :math:`H(S_T)` that depends just on the terminal value of the underlying, :math:`S_T`, it can be shown that 

.. math::
   \mathbb{E}[H(S_T)|\mathcal{F}_t]  = H(F_T)  + \int_0^{F_T}H''(k)P(k)dk + \int_{F_T}^{\infty}H''(k)C(k)dk 

where :math:`P(k)` and :math:`C(k)` are the prices of vanilla put and call options with strike :math:`k` and expiry :math:`T`.

For our log contract we have :math:`H''(k) = -1/k^2`, so the expected variance can be written as 

.. math:: EV(T)  = 2\left(\int_0^{F_T}\frac{P(k)}{k^2}dk + \int_{F_T}^{\infty}\frac{C(k)}{k^2}dk   \right)
   :label: staticRep

To use this equation as is, requires a continuum of option prices from zero to infinite strike.\ [#]_ In practice only a finite number of liquid call and put
prices are available in the market and the expiries may not match that of the variance swap. We use these option prices 
to generate a (continuous) Black volatility surface, :math:`\sigma(T,k)` [#]_ which is then used to compute the expected variance, as:
 
.. math:: EV(T)  = 2 \int_0^{F_T}\frac{\text{Black}(F_T,k,T,\sigma(T,k),2\mathbb{I}_{k>F_T} - 1)}{k^2}dk
    :label: staticRepVol

where :math:`\text{Black}(F,K,T,\sigma,\chi)` [#]_ is Black's formula [Black1976]_.  This integral converges provided
that :math:`\sigma(T,0) < \infty` (since then :math:`\lim_{k\rightarrow 0}\frac{P(k)}{k^2} = 0`), and the call price 
is monotonically decreasing in strike. It should be clear that the extrapolation of the smile to low and high strikes will
have a significant effect on the calculated expected variance. This will be covered in the examples below.  



Discrete Dividends
------------------ 

In this example we assume that any dividends are paid continuously, and the dividend yield is encoded in the forward curve. 
This means this example is equally applicable to FX variance swaps.  

The handling of discretely paid dividends is discussed `here <http://developers.opengamma.com/quantitative-research/Equity-Variance-Swaps-with-Dividends-OpenGamma.pdf>`_.



Code Examples
=============

This is an example of how to price an equity variance swap.
The code for this example is in: ``EquityVarianceSwapDemo`` with the various methods demonstrating different features.  We use two calculators, ``RealizedVariance`` and ``VarianceSwapStaticReplication`` - the former is an implementation of  equation :eq:`realisedVar`, while the latter has an ``expectedVariance`` method which implements equation :eq:`staticRep`, and a ``presentValue`` method which computes the fair value of a variance swap by combining the realised variance (if any observations have already been made) with the expected variance (of the remaining observations). The full demo set up is:

.. code-block:: java

  private static final RealizedVariance REALIZED_VOL_CAL = new RealizedVariance();
  private static final VarianceSwapStaticReplication PRICER = new VarianceSwapStaticReplication();
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final NormalDistribution NORMAL = new NormalDistribution(0, 1, RANDOM);
  private static final ZoneId UTC = ZoneId.of("UTC");

  private static final double s_Spot = 80;
  private static final double s_Drift = 0.05;
  private static final double s_Vol = 0.3;
  private static final ForwardCurve s_FwdCurve = new ForwardCurve(s_Spot, s_Drift);
  private static final YieldAndDiscountCurve s_DiscountCurve = new YieldCurve("Discount", ConstantDoublesCurve.from(s_Drift));
  private static final BlackVolatilitySurfaceStrike s_FlatVolSurf = new BlackVolatilitySurfaceStrike(ConstantDoublesSurface.from(s_Vol));

  private static final ZonedDateTime s_ObsStartTime = ZonedDateTime.of(2013, 12, 16, 12, 0, 0, 0, UTC);// ZonedDateTime.of(2013, 7, 27, 12, 0, 0, 0, UTC); // Saturday
  private static final ZonedDateTime s_ObsEndTime = ZonedDateTime.of(2015, 7, 30, 12, 0, 0, 0, UTC); // Thursday
  private static final ZonedDateTime s_SettlementTime = ZonedDateTime.of(2015, 8, 3, 12, 0, 0, 0, UTC);// Monday
  private static final Currency s_Ccy = Currency.EUR;
  private static final Calendar s_Calendar = new CalendarTarget("Eur");
  private static final double s_AnnualizationFactor = 252.0;
  private static final double s_VolStrike = 0.3;
  private static final double s_VolNotional = 1e6;



Building a Variance Swap
------------------------

This demonstrate building an equity variance swap and adding time series of observations. The full code is in the method ``buildSwap``. 
We build the variance swap using data in the example by 

     .. code-block:: java

      EquityVarianceSwapDefinition def = new EquityVarianceSwapDefinition(s_ObsStartTime, s_ObsEndTime, s_SettlementTime, s_Ccy, s_Calendar, s_AnnualizationFactor, s_VolStrike, s_VolNotional, false);

This is a description of the trade that reflects the information that would be seen on the term sheet. We can turn this
into an *analytical* description (at a particular valuation date) using 

.. code-block:: java
      
      ZonedDateTime valDate = ZonedDateTime.of(2014, 8, 11, 12, 0, 0, 0, UTC);
      EquityVarianceSwap varSwap = def.toDerivative(valDate);
       
This ``varSwap`` object is used by the analytic pricing code. Note that no observations of the closing price have been made;
since the valuation date is after the first observation date, these are treated as disrupted observations. 
As no observations are yet made, we should have zero realized volatility

.. code-block:: java
   
       assertEquals(0.0, REALIZED_VOL_CAL.evaluate(varSwap)); 

After the last observation date, we have the time series of closing prices. For this example we create a random price series,
based on log-normal returns with an (annualised) volatility of 30% 

   .. code-block:: java

    double dailyDrift = (s_Drift - 0.5 * s_Vol * s_Vol) / s_AnnualizationFactor;
    double dailySD = s_Vol / Math.sqrt(s_AnnualizationFactor);

    dates[0] = s_ObsStartTime.toLocalDate();
    Prices[0] = 100.0;
    logPrices[0] = Math.log(100.0);
    double sum2 = 0;
    for (int i = 1; i < observationDays; i++) {
      dates[i] = BusinessDayDateUtils.addWorkDays(dates[i - 1], 1, s_Calendar);
      logPrices[i] = logPrices[i - 1] + dailyDrift + dailySD * NORMAL.nextRandom();
      Prices[i] = Math.exp(logPrices[i]);
      double rtn = logPrices[i] - logPrices[i - 1];
      sum2 += rtn * rtn;
      }
    
    LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(dates, Prices);    

We can use this to create a new  *analytical* description of the instrument at the settlement date, and compute the realised variance:

.. code-block:: java
 
    varSwap = def.toDerivative(s_SettlementTime, ts);
    double relVar = REALIZED_VOL_CAL.evaluate(varSwap);

The computed realised variance agrees with equation :eq:`realisedVar`, however the value is not :math:`0.3^2` - even with Black-Scholes dynamics, the realized variance will differ from the expected variance.  

   
The ``VarianceSwapStaticReplication`` 
computes the price of the swap as a combination of realised variance (from price
observations already made) and expected variance (from static replication) - since in this example all price observations
have been made, the price comes purely from the realised variance. 

.. code-block:: java

 StaticReplicationDataBundle market = new StaticReplicationDataBundle(s_FlatVolSurf, s_DiscountCurve, s_FwdCurve);
 double pv = PRICER.presentValue(varSwap, market);

where *market* is a instance of ``StaticReplicationDataBundle`` which contains the *discount curve*, the *forward curve*
and the *volatility surface* (the last of these is not used in this example).


Pricing a Variance Swap with a Flat Volatility Surface
------------------------------------------------------

The code for this example is in the ``flatVolPrice`` method. 
In this example the expected variance is computed by static replication - integration over vanilla option prices. These prices are
derived from a volatility surface which is flat at 30% - hence we should recover (up to some numerical tolerance)
:math:`0.3^2=0.09` for the expected variance. We create a ``VarianceSwap`` as usual

  .. code-block:: java

    VarianceSwapDefinition def = new VarianceSwapDefinition(s_ObsStartTime, s_ObsEndTime, s_SettlementTime, s_Ccy, s_Calendar, s_AnnualizationFactor, s_VolStrike, s_VolNotional);
    ZonedDateTime valueDate = ZonedDateTime.of(2013, 7, 25, 12, 0, 0, 0, UTC); // before first observation
    VarianceSwap varSwap = def.toDerivative(valueDate);
    
 The volatility surface and the ``StaticReplicationDataBundle`` are given by

 .. code-block:: java
 
   BlackVolatilitySurfaceStrike s_FlatVolSurf = new BlackVolatilitySurfaceStrike(ConstantDoublesSurface.from(s_Vol));
   StaticReplicationDataBundle market = new StaticReplicationDataBundle(s_FlatVolSurf, s_DiscountCurve, s_FwdCurve);
 
Then we test that the pricer gives the expected value: 

 .. code-block:: java
 
    assertEquals(s_Vol * s_Vol, PRICER.expectedVariance(varSwap, market), 1e-10);
 
Next we consider pricing a seasoned trade; we choose a valuation date of 28\ :sup:`th` Jan 2014 (the observation start date 
is 16\ :sup:`th` Dec 2013), and fill in some randomly generated observations as in the first example. Then we compute the
price using the observations we have and the knowledge that volatility surface is flat (at 30%), and compare this with the
result of the calculator (which integrates over the vanilla option prices).

  .. code-block:: java

    double df = market.getDiscountCurve().getDiscountFactor(varSwap.getTimeToSettlement());
    double expVar = (s_AnnualizationFactor * sum2 + s_Vol * s_Vol * (varSwap.getObsExpected() - observationDays)) / (varSwap.getObsExpected() - 1);
    double expPV = df * s_VolNotional / 2 / s_VolStrike * (expVar - s_VolStrike * s_VolStrike);
    double pv = PRICER.presentValue(varSwap, market);
    assertEquals(expPV, pv, 1e-12 * s_VolNotional);
    
The static replication methods doesn't *know* that the volatility is flat (constant), so it is reassuring that it produces the correct answer to high accuracy. 
 

Pricing a Variance Swap with a Non-Flat Volatility Surface
----------------------------------------------------------

The code for this example is in the method ``testMixedLogNormalVolSurface``. 
This is another test that the static replication (eqn. :eq:`staticRep` or :eq:`staticRepVol`) is working correctly - this
time for a non-flat volatility surface. 

A `mixed log-normal model <http://developers.opengamma.com/quantitative-research/Mixed-Log-Normal-Volatility-Model-OpenGamma.pdf>`_
can give realistic looking smiles. It also allows a very simple analytic calculation of the
expected variance. This can be compared with the calculator that just *sees* a volatility surface. The surface is set up as follows:

  .. code-block:: java

   final double sigma1 = 0.2;
   final double sigma2 = 1.0;
   final double w = 0.9;
   Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
       public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        @SuppressWarnings("synthetic-access")
        final double fwd = s_FwdCurve.getForward(t);
        final boolean isCall = k > fwd;
        final double price = w * BlackFormulaRepository.price(fwd, k, t, sigma1, isCall) + (1 - w) * BlackFormulaRepository.price(fwd, k, t, sigma2, isCall);
        if (price < 1e-100) {
          return sigma2;
        }
        return BlackFormulaRepository.impliedVolatility(price, fwd, k, t, isCall);
      }
    };
   BlackVolatilitySurfaceStrike surfaceStrike = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surf));
    
With this model, the expected variance is trivially given, and the result from static replication agree to a high tolerance.

  .. code-block:: java 
 
    double expected = w * sigma1 * sigma1 + (1 - w) * sigma2 * sigma2;
    double strikeVal = PRICER.expectedVariance(varSwap, new StaticReplicationDataBundle(surfaceStrike, s_DiscountCurve, s_FwdCurve));
    assertEquals("strike", expected, strikeVal, 5e-12);   

We finish this example by converting the volatility surface, which is parameterised by strike, to one parameterised by delta 
(this is more common in FX). This is achieved using the ``BlackVolatilitySurfaceConverter``. The pricer can then calculate 
the expected variance from this delta surface

  .. code-block:: java

    BlackVolatilitySurfaceDelta surfaceDelta = BlackVolatilitySurfaceConverter.toDeltaSurface(surfaceStrike, s_FwdCurve);
    double deltaVal = PRICER.expectedVariance(varSwap, new StaticReplicationDataBundle(surfaceDelta, s_DiscountCurve, s_FwdCurve));
    assertEquals("delta", expected, deltaVal, 5e-8);
    
The conversion to a delta surface (from a strike surface) involves root-finding, so the delta surface is less accurate,
not the method using  the delta surface.


Pricing a Variance Swap from a Finite Set of Vanilla Option Prices
------------------------------------------------------------------ 

The code for this example is in the method ``discreteOptionPricesTest``. 


So far we have assumed that a volatility surface (valid for strikes from zero to infinity) is known. In practice we
will have a finite set of vanilla option prices. Assume initially that the expiry of these options coincides with the
expiry of the variance swap. We choose a set of nine strikes around the ATMF. 
  
  .. code-block:: Java 

   double[] strikes = new double[] {50.0, 60.0, 70.0, 80.0, fwd, 90.0, 100.0, 120.0, 150.0 };

We then use the SABR model to generate our implied volatilities  - with the parameters we use (beta = 1.0 & rho = 0.0) we know exactly the expected variance under SABR dynamics, which is given by 
   
   .. math:: \operatorname{RV}(T) = \frac{\alpha^2}{\nu^2T} \left(e^{\nu^2T}-1\right)

The code to generate the implied volatilities is: 

  .. code-block:: Java

    double alpha = 0.2;
    double beta = 1.0;
    double rho = -0.0;
    double nu = 0.5;
    SABRFormulaData sabr = new SABRFormulaData(alpha, beta, rho, nu);
    VolatilityFunctionProvider<SABRFormulaData> volFunPro = new SABRHaganVolatilityFunction();
    Function1D<SABRFormulaData, double[]> func = volFunPro.getVolatilityFunction(fwd, strikes, expiry);
    double[] blackVols = func.evaluate(sabr);


Once we have these *implied volatilities*, we treat them as market data, and do not assume any knowledge of the *volatility surface*. Instead we use this data to interpolate a volatility smile which is extrapolated to zero and high strikes.

Our first smile interpolator fits a spline (``DoubleQuadraticInterpolator1D``) through the volatility points, then used a ``ShiftedLogNormalTailExtrapolationFitter`` to extrapolate the smile to high and low strikes. The code is: 
  
  .. code-block:: java

    GeneralSmileInterpolator smileInterpolator = new SmileInterpolatorSpline();
    Function1D<Double, Double> smileFunc = smileInterpolator.getVolatilityFunction(fwd, strikes, expiry, blackVols);
    BlackVolatilitySurface<?> volSurface = makeSurfaceFromSmile(smileFunc);
    StaticReplicationDataBundle market = new StaticReplicationDataBundle(volSurface, s_DiscountCurve, s_FwdCurve);
    double expVar = PRICER.expectedVariance(varSwap, market);

The exact value is 0.04931, while the value calculated above is 0.04977 - this is acceptable given that we are interpolating from a finite set of option values. We now switch to a different smile interpolator; one based on the SABR model::

    smileInterpolator = new SmileInterpolatorSABR();

This fits the SABR model to triplets of consecutive volatilities and smoothly interpolates between the resultant smiles. The smile is extrapolated as above. The result is a calculated value of the expected variance of 0.05045. 

There are many (smile) interpolator/extrapolator combinations available, all of which will give slightly difference values of the expected variance and thus the theoretical value of the variance swap. 


.. [#] A log-contact has the payoff :math:`\log(S_T)` at expiry - these are not liquid but can be statically replicated with strips of Europeans puts and calls.
.. [#] Black's formula is given by: 

    .. math::
      \operatorname{Black}(F,K,T,\sigma,\chi) = \chi\left(F\Phi(\chi d_1) - K\Phi(\chi d_2)\right)\\
      d_1 = \frac{\ln\left(\frac{F}{K}\right) + \frac{\sigma^2 T}{2}} {\sigma \sqrt{T}} \quad d_2 = d_1 -\sigma \sqrt{T}
where :math:`\chi` = +1 for calls and -1 for puts.

.. [#] In practice an upper cut-off is used, which will depend on the required accuracy. 
.. [#] Fitting an arbitrage free volatility surface from a collection of vanilla option prices is a complex topic in its own
 right; we do not discuss the details here. 

.. [Black1976] Fischer Black. The pricing of commodity contracts. Journal of Financial Economics, 3:167--179, 1976.
.. [GAT.2006.1] J Gatheral. *The Volatility Surface: a Practitioner's Guide}.* Finance Series. Wiley, 2006.
.. [Neuberger1992] A Neuberger. Volatility trading. Technical report, London Business School working paper, 1992.
