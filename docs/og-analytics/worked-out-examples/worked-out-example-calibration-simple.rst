Curve calibration: simple example
===============
In this example we construct a simple set of curve composed of only two curves in USD. The code related to the example can be found in the demo test **MulticurveBuildingDiscountingDiscountUSD2DemoTest**.

Curve descriptions
------------------
The first curve will be used for discounting and for projecting Fed Fund rates. The calibration is done on one overnight (deposit) rate and on Overnight Indexed Swaps (OISs).

The second curve is used for projecting USD LIBOR 3M rates. The calibration is done on one LIBOR 3M fixing and on interst rate swaps (IRSs) fixed v LIBOR 3M.

A curve is described by the instrument used to calibrate it. The instrument are constructed using a *market quote* (swap rate, futures price, ...), a *generator* (contains the conventions) and an *attribute* (usually with the tenor). In the platform, the generator and attributes are stored in *nodes* and in *conventions*.

In the discounting and Fed Fund forward curve called::

    String CURVE_NAME_DSC_USD = "USD Dsc"
the instruments are 1 overnight deposit and 10 OIS. The generators of those instrument (the market standard conventions) are stored in a separate file (to use the same conventions for the different examples) and called through::

    GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = CurveCalibrationConventionDataSets.generatorUsdOnOis(1, 11, 0)
The market data for the different instruments are::

    double[] DSC_USD_MARKET_QUOTES = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 }
We use constant market rate for this very simple first example.

The tenors of the different deposits and swaps are stored in::

    Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), ...

In the USD LIBOR 3M forward curve called::

    String CURVE_NAME_FWD3_USD = "USD Fwd 3M"
the instruments are 1 overnight deposit and 7 IRS. The generators of those instrument (the market standard conventions) are stored in a separate file (to use the same conventions for the different examples) and called through::

    GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS = CurveCalibrationConventionDataSets.generatorUsdIbor3Irs3(1, 7)
The market data for the different instruments are::
    double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 }
The data is selected to show some features of the interpolators and are not representative of the current market levels. The numbers are the one used Andersen and Pieterbarg book.
    
The tenors of the different deposits and swaps are stored in::

    Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), ...

The indexes used in the curves are::

    IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
    IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");

Calibration 
-----------

In this example we calibrate the curve in two units::

    int[] NB_UNITS = new int[] {2 }

The calibrated curve will be used for specific financial purposes. Those descriptions are stored in three maps::

    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {FEDFUND });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_USD, new IborIndex[] {USDLIBOR3M });
The discounting in USD is done with the curve CURVE_NAME_DSC_USD, the forward of the overnight index FEDFUND are computed using the same curve and the forward for the Ibor index USDLIBOR3M are computed using the curve CURVE_NAME_FWD3_USD.

The calibration code is stored in the::

    MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY =
      CurveCalibrationConventionDataSets.curveBuildingRepository();

The calculator used to compute the function for which the root is find and its derivatives are::

    ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance()
    ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

The rest of the calibration process is the transformation of the *Definition* versions of the instruments to the *Derivative* version. This is done in the method **makeCurvesFromDefinitions*. The final part is the calibration itself::

    CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, calculator, sensitivityCalculator)

Calibration test
----------------

Once the calibration is finished we can check that it produced the expected results. 

We first check that the present value of all the instrument is 0. This is done in the test method::

    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      curveConstructionTest(DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), false, loopblock);
    }

We may want to export the (forward) curve in a csv file to graph the results. This can be done using the method::

    public void forwardAnalysis() {
      CurveCalibrationTestsUtils.exportForwardCurve(CALIBRATION_DATE, CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst(), USDLIBOR3M, NYC, "fwd-usd-libor3m.csv", 0, 2500, 1);
    }
It will export the result in the csv file with the given name at the root folder of the library. The csv file can be used in Excel or Matlab to graph the result.
