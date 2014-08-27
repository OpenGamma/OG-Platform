CrossGammaMultiCurveCalculator
==============

Computes the cross-gamma to the curve parameters for a multi-curve provider. The provider should be a **MulticurveProviderDiscount** with all the curves related to the same currency. 

The curves should be represented by a YieldCurve with an InterpolatedDoublesCurve on the zero-coupon rates. The computation is done by finite difference (bump-and-recompute) on the first order derivative (curve sensitivity) and is supported only for those types of curves.

By default the gamma is computed using a one basis-point shift. This default can be change in a constructor. The results themselves are not scaled (the represent the second order derivative).

Methods
-------

There are two methods 

* **calculateCrossGammaIntraCurve** 
* **calculateCrossGammaCrossCurve**



The underlying curve sensitivity calculator can be provided when the gamma calculator is constructed. The standard curve sensitivity calculators using in OG-Analytics uses Algorithmic Differentiation. The final gamma results thus use a combination of finite difference and algorithmic differentiation to produce the final result.

.. rubric:: Reference

Interest rate cross-gamma for single curve. *OpenGamma quantitative research* 15, Version 1.0, July 14}
