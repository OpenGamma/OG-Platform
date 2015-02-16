Worked-out example: volatility smile for equity index options
==========================================

The code of this example can be found in the test: **EquityIndexOptionSmileAndPriceGreeksE2ETest**. 

The test class contains three methodologies for volatility smile construction from a sparse and incomplete data set of European-type equity index options. 
Also prices and Greeks of a complete set of the options are computed and examples of volatility shock scenarios are presented based on the constructed volatility smile. 



Theory
------

In the end-to-end test three methodologies are used: global SABR model method, local SABR method and spline interpolation with extrapolation by shifted lognormal model. 
Details on these methodologies are given in [OG.2014]. 


Option Data
-------

Given a set of information on the underlying,  

.. code-block:: java 

    double SHARES = 10.0;
    int DAYS_TO_MAT = 23;
    double FORWARD = 3360;
    double RATE = 0.204 * 0.01;
    double DIVIDEND = 0.333 * 0.01;

we assume the following options are available for the specific time to expiry, 

.. code-block:: java 

  double[] PUT_STRIKES = new double[] {3050, 3100, 3125, 3300, 3350, 3550 };
  double[] CALL_STRIKES = new double[] {3075, 3325, 3450, 3525 };
  double[] PUT_PRICES = new double[] {7.57640856221836, 10.4492292389527, 12.3810949922475, 43.5400250456128, 61.8776592568553, 197.744462599924 };
  double[] CALL_PRICES = new double[] {292.512814702669, 85.6868660157612, 26.1322435193456, 9.48267272813102 };

The prices are converted to implied volatilities via Black formula. For example, for put options 

.. code-block:: java 

  double[] PUT_IV;
  PUT_IV[i] = BlackFormulaRepository.impliedVolatility(PUT_PRICES[i] / DF, FORWARD, PUT_STRIKES[i], EXPIRY, false);

Then the smile construction methodologies are applied to the implied volatilities. 

In general market prices of call options are inconsistent with those of put options in the sense call and put with the same strike lead to different values of implied volatility, which is mainly a consequence of liquidity difference
between the call and put options, and also between options and underlying. 
To capture this feature, we straightforwardly build two volatility smiles, one for call options and the other for put options.


Volatility Smile 
------------------


The global SABR method for the call options is called in the method **fitSabrSmileCall**. First we instantiate a **BitSet**

.. code-block:: java 

    BitSet fixed = new BitSet();

which is used for fixing a specific SABR parameter. In order to fix the beta parameter, we set
    
.. code-block:: java 

    fixed.set(1); 

None of the parameters are fixed if we use the default **BitSet**. Next initial guess values are defined

.. code-block:: java 

    double atmVol = 0.19;
    double beta = 0.5;
    double rho = -0.7;
    double nu = 1.8;
    double alpha = atmVol * Math.pow(FORWARD, 1 - beta);
    DoubleMatrix1D start = new DoubleMatrix1D(alpha, beta, rho, nu);

Then the calibration is conducted by 

.. code-block:: java 

    SmileModelFitter<SABRFormulaData> sabrFitter = new SABRModelFitter(FORWARD, CALL_STRIKES, EXPIRY, CALL_IV, CALL_ERRORS, SABR);
    Function1D<Double, Double> smile = fitSmile(sabrFitter, start, fixed);

The output is a function **Function1D<Double, Double>** which takes strike value and returns volatility. Note that we set **CALL_ERRORS** to be 10 basis points and this only affect the resulting chi-square value. 

The local SABR method for the call options is called in the method **sabrInterpolationCallTest**. The SABR model calibration is conducted by 

.. code-block:: java 

    GeneralSmileInterpolator sabr_interpolator = new SmileInterpolatorSABR();
    Function1D<Double, Double> smile = sabr_interpolator.getVolatilityFunction(FORWARD, PUT_STRIKES, EXPIRY, PUT_IV);

Note that the code calls the global SABR fitter if the local SABR fails. Thus the local and global fits end up with the same
result in this case.

The spline interpolation and shifted lognormal model extrapolation are used in **splineInterpolationCallTest** and **splineInterpolationFlatCallTest** for call options. The two methods correspond to two distinct behaviours for calibration failure of the shifted lognormal model. 

For example, the gradient values of the smile at the data endpoints are reduced to zero such that the calibration always success by using "Flat",

.. code-block:: java 

     GeneralSmileInterpolator spline = new SmileInterpolatorSpline(new DoubleQuadraticInterpolator1D(), "Flat");
     Function1D<Double, Double> smile = spline.getVolatilityFunction(FORWARD, CALL_STRIKES, EXPIRY, CALL_IV);

Here **DoubleQuadraticInterpolator1D** is used as the spline, but **SmileInterpolatorSpline** accepts any interpolator in **Interpolator1D**.

In this document we have discussed the call options. The exactly the same argument is true for the put options.


Price and Greeks
----------------

We consider the following strikes as a complete set, 

.. code-block:: java 

  double LOWER_STRIKE = 3000.;
  double UPPER_STRIKE = 3600.;
  double STRIKE_STEP = 25.;
  double[] DISPLAY_STRIKES;
  int n = (int) ((UPPER_STRIKE - LOWER_STRIKE) / STRIKE_STEP + 1);
  DISPLAY_STRIKES = new double[n];
  for (int i = 0; i < n; i++) {
    DISPLAY_STRIKES[i] = LOWER_STRIKE + i * STRIKE_STEP;
  }
  DISPLAY_STRIKES[n - 1] = UPPER_STRIKE;


With the smile as **Function1D<Double, Double>**, the price and Greeks are printed, e.g., by

.. code-block:: java 

    printDetails(smile, DISPLAY_STRIKES, true);


Our approach to the volatility shock scenarios is to construct a volatility smile first and add a parallel shift to the constructed curve. 
New prices and Greeks are computed with the shifted volatility smile.  
The result is printed by 

.. code-block:: java 

    double[] VOL_SHOCKS = new double[] {-0.1, -0.05, -0.01, 0.0, 0.01, 0.05, 0.1 };
    printDetailsWithShift(smile, DISPLAY_STRIKES, true, VOL_SHOCKS);


.. [OG.2014] Yukinori Iwashita. Smile Interpolation and Extrapolation. OpenGamma Quantitative Research, 2014.
