PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator
=======================================

The calculator computes the present value curve sensitivity using data comprising a multi-curve provider
and the Black implied volatility for STIR futures at
expiry/strike/delay points. 

The present value curve sensitivity is the point sensitivity, i.e. the sensitivity at each discount factor and each forward rate used in the computation. 

The result of this calculator can be used as an input to **ParameterSensitivityParameterCalculator** to obtain the sensitivity to the internal parameters representing the curves.

The result above can be used as an input to **fromParameterSensitivity** method in **MarketQuoteSensitivityBlockCalculator** to compute the sensitivity with respect to the market quotes. The sensitivity to market quotes uses the Jacobian/transition matrices computed in the curve calibration and stored in the **CurveBuildingBlockBundle** object.
