CrossGammaSingleCurveCalculator
==============

Computes the cross-gamma to the curve parameters for a single curve. The single curve is represented by a **MulticurveProviderDiscount** containing a unique curve.

The curve should be represented by a YieldCurve with an InterpolatedDoublesCurve on the zero-coupon rates. The computation is done by finite difference (bump-and-recompute) and is supported only for those types of curves.

By default the gamma is computed using a one basis-point shift. This default can be change in a constructor. The results themselves are not scaled (the represent the second order derivative).\

Methods
-------

The main method in this calculator is **calculateCrossGamma**. It computes the cross-gamma matrix of the single curve with respect to the zero-coupon rates used in the interpolated curve. The computation is done by computing for each node the first order derivative or delta at the initial curve with the node rate shifted. The initial delta is subtracted from shifted delta to compute the partial second order derivative. The first order derivatives are computed using the :doc:`ParameterSensitivityParameterCalculator` on a curve sensitivity calculator.

The underlying curve sensitivity calculator can be provided when the gamma calculator is constructed. The standard curve sensitivity calculators using in OG-Analytics uses Algorithmic Differentiation. The final gamma results thus use a combination of finite difference and algorithmic differentiation to produce the final result.

.. rubric:: Reference

Interest rate cross-gamma for single curve. *OpenGamma quantitative research* 15, Version 1.0, July 14}