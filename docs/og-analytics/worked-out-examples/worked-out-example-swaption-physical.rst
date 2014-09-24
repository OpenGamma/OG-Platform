Worked-out example: swaption in SABR
====================================

The code of this example can be found in the test: **SwaptionPhysicalFixedIborSABRMethodE2ETest**

The swaption used in those examples are physical delivery European swaptions on fixed v Ibor swaps.

Curves and SABR parameters
--------------------------

To compute the risk measures, we need a multi-curve provider. In this
example we simply load an existing curves. For example on curve
calibration see :doc:`worked-out-examples-intro.rst` ::

    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR = StandardDataSetsMulticurveUSD.getCurvesUSDOisL1L3L6();

This load in a multi-curve provider and the building block of the Jacobian matrices. The second object is required for sensitivity to market quote computation.

The provider consists of three curves: a USD discounting curve (also
used for Fed Fund projection, but this is not used in this example), a
USD LIBOR 3M and a USD LIBOR 6M forward curve.

To price the swaption we also need model parameters. In this case, we
use a SABR model. The parameters of the model are represented by
expiry and tenor dependent SABR parameters. The parameters are stored
in the following object::

    SABRInterestRateParameters SABR_PARAMETER = StandardDataSetsSABRSwaptionUSD.createSABR1();


Instrument
----------

We create a simple swaption with data stored in the example. The
underlying swap is generated from standard conventions for USD swaps
(called USD6MLIBOR3M in the example), an expiry date and the
long/short flag.::

    SwapFixedIborDefinition SWAP_PAYER_DEFINITION =
      USD6MLIBOR3M.generateInstrument(EXPIRY_DATE, FIXED_RATE_3M, NOTIONAL, ATTRIBUTE_3M)
    SwaptionPhysicalFixedIborDefinition SWAPTION_P_2Yx7Y_DEFINITION = 
      SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, true)

This is the definition version of the instrument, i.e. it contains all the dates and data relevant to the trade.

The definition version is converted to the *derivative* version.::

    SwaptionPhysicalFixedIbor SWAPTION_P_2Yx7Y = SWAPTION_P_2Yx7Y_DEFINITION.toDerivative(REFERENCE_DATE);

Present value
-------------

The first output we produce is the *present value* of the instrument. 
The code to compute the present value simply read as::

    PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
    MultipleCurrencyAmount pvComputed = SWAPTION_P_2Yx7Y.accept(PVSSC, MULTICURVE_SABR);

The present value calculator is used through the visitor pattern. The result of the present value computation is a **MultipleCurrencyAmount**. It is not only the present value as a double but also the information about the currency in which it is expressed. In some cases, like for FX forward or cross-currency swaps, the output will contains multiple currencies. The output will be the present value of each leg in its original currency.
