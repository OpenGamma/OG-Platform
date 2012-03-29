This tutorial covers yield curve construction and derivative pricing, and introduces some OpenGamma classes for working with matrices, defining functions and root finding.

## Matrices

The [com.opengamma.math.matrix](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/matrix/package-summary.html) package includes the [Matrix](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/matrix/Matrix.html) interface.

Classes which implement the Matrix interface are:

<ul>
{% set j = d['/jsondocs/javadoc-data.json|javadocs'] %}
{% set nested_subclasses = a['/jsondocs/javadoc-data.json|javadocs'].filter_class().nested_subclasses %}
{% for impl_class_name in d['/jsondocs/javadoc-data.json|javadocs']['packages']['com.opengamma.math.matrix']['classes']['Matrix'].implementers %}
{% for indent, class_name in nested_subclasses(j, impl_class_name) %}
{% set class_path = class_name.replace(".", "/") %}
<li><a href="/{{ OG_VERSION }}/java/javadocs/{{ class_path }}.html">{{ class_name }}</a></li>
{% endfor -%}
{% endfor -%}
</ul>

Instances of the DoubleMatrix1D and DoubleMatrix2D classes can be created by passing an array of double or Double elements.

{{ d['MatrixExample.java|idio']['initMatrixDemo'] }}

<pre>
{{ d['matrix-output.json']['initMatrixDemo'] }}
</pre>

The [ColtMatrixAlgebra](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/matrix/ColtMatrixAlgebra.html) class provides methods to perform calculations on Matrix objects.

{{ d['MatrixExample.java|idio']['matrixAlgebraDemo'] }}

<pre>
{{ d['matrix-output.json']['matrixAlgebraDemo'] }}
</pre>


## Functions

The [com.opengamma.math.function](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/function/package-summary.html) package includes the [Function](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/function/Function.html) interface. We will look at some examples of using this interface.

Here is the polynomial we will work with:

\\begin{eqnarray}
f(x) &=& (x-5)^3 \\\\
&=& x^3 - 15x^2 + 75x - 125
\\end{eqnarray}

We create a [RealPolynomialFunction1D](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/function/RealPolynomialFunction1D.html) with these coefficients, verify that 5 is a zero, and take a derivative:

{{ d['FunctionExample.java|idio']['polyDerivativeDemo'] }}

Here are the coefficients of the derivative:

<pre>
{{ d['function-output.json']['polyDerivativeDemo'] }}
</pre>

## Rootfinding

The [com.opengamma.math.rootfinding](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/rootfinding/package-summary.html) package has classes which implement various methods for finding roots of functions. In the previous section we defined a polynomial with roots at $x=5$. In this section we will try to apply the rootfinding classes to this polynomial.

Here is the class comment for the [CubicRealRootFinder](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/function/CubicRealRootFinder.html):

{% set cubic_class = d['/jsondocs/javadoc-data.json|javadocs']['packages']['com.opengamma.math.rootfinding']['classes']['CubicRealRootFinder'] %}
{{ cubic_class['comment-text'] }}

We create an instance of the CubicRealRootFinder and call its getRoots() method:

{{ d['FunctionExample.java|idio']['cubicRealRootFindingDemo'] }}

It finds the 3 roots at $x=5$:

<pre>
{{ d['function-output.json']['cubicRealRootFindingDemo'] }}
</pre>

Next we use a [BrentSingleRootFinder](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/function/BrentSingleRootFinder.html), passing bracketing values which we know to be above and below the root:

{{ d['FunctionExample.java|idio']['brentSingleRootFinderDemo'] }}

It finds a root at approximately $x=5$:

<pre>
{{ d['function-output.json']['brentSingleRootFinderDemo'] }}
</pre>

If we don't correctly bracket the root, an IllegalArgumentException is raised:

{{ d['FunctionExample.java|idio']['brentSingleRootFinderNotBracketingDemo'] }}

<pre>
{{ d['function-output.json']['brentSingleRootFinderNotBracketingDemo'] }}
</pre>

## Curves

The [com.opengamma.math.curve](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/curve/package-summary.html) package includes the abstract [Curve](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/curve/Curve.html) class.

{% set curve_class = d['/jsondocs/javadoc-data.json|javadocs']['packages']['com.opengamma.math.curve']['classes']['Curve'] %}
{{ curve_class['comment-text'] }}

We begin with a simple constant curve.

{% set constant_curve_class = d['/jsondocs/javadoc-data.json|javadocs']['packages']['com.opengamma.math.curve']['classes']['ConstantDoublesCurve'] %}
{{ constant_curve_class['comment-text'] }}

We create a ConstantDoublesCurve and verify that it returns the constant $y$ value at several values for $x$:

{{ d['CurveExample.java|idio']['constantDoublesCurveDemo'] }}

<pre>
{{ d['curve-output.json']['constantDoublesCurveDemo'] }}
</pre>

The abstract [DoublesCurve](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/curve/DoublesCurve.html) class allows curves to be created by passing in arrays of x and y coordinates.

{% set doubles_curve_class = d['/jsondocs/javadoc-data.json|javadocs']['packages']['com.opengamma.math.curve']['classes']['DoublesCurve'] %}
{{ doubles_curve_class['comment-text'] }}

The [NodalDoublesCurve](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/curve/NodalDoublesCurve.html) creates a curve which is only defined for the $x$ values specified:

{{ d['CurveExample.java|idio']['nodalDoublesCurveDemo'] }}

<pre>
{{ d['curve-output.json']['nodalDoublesCurveDemo'] }}
</pre>

The [InterpolatedDoublesCurve](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/curve/InterpolatedDoublesCurve.html) creates a curve which is defined for all values of $x$ within the minimum and maximum specified values of $x$, with the $y$ values being interpolated where they are not defined, according to the chosen interpolator:

{{ d['CurveExample.java|idio']['interpolatedDoublesCurveDemo'] }}

<pre>
{{ d['curve-output.json']['interpolatedDoublesCurveDemo'] }}
</pre>

We can create a curve valid for all values of $x$ by choosing the [CombinedInterpolatorExtrapolator](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/interpolation/CombinedInterpolatorExtrapolator.html), which lets you specify an interpolator, a left extrapolator, and a right extrapolator:

{{ d['CurveExample.java|idio']['interpolatorExtrapolatorDoublesCurveDemo'] }}

<pre>
{{ d['curve-output.json']['interpolatorExtrapolatorDoublesCurveDemo'] }}
</pre>

There are many different interpolators and extrapolators available in the [com.opengamma.math.interpolation](/{{ OG_VERSION }}/java/javadocs/com/opengamma/math/interpolation/package-summary.html) package.

Curves can also be created based on functions, where the y coordinates are determined by applying the specified function to any x coordinate that is requested. There are also utilities for shifting curves by a constant or defining curves based on a mathematical operation on two or more other curves.

Curves are often given names, where these are not given explicitly then curves are automatically assigned a unique ID. It is common to refer to bundles of curves in the form of LinkedHashMaps where the curve names form the keys and the corresponding curve objects form the values in the hashmap.

## Yield Curves

The [com.opengamma.financial.model.interestrate.curve](/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/model/interestrate/curve/package-summary.html) package defines the following classes:

<ul>
{% for class_name, class_info in d['/jsondocs/javadoc-data.json|javadocs']['packages']['com.opengamma.financial.model.interestrate.curve']['classes'].iteritems() %}
<li><a href="/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/model/interestrate/curve/{{ class_name }}.html">{{ class_name }}</a></li>
{{ class_info['comment-text'] }}
{% endfor -%}
</ul>


Yield Curves and Discount Curves (which each inherit from YieldAndDiscountCurve) are created by passing a Curve to the constructor. The $x$ coordinate is interpreted as time $t$. Here is an example of a yield curve based on a constant interest rate of {{ d['yield-curve-fields.json']['y'] }}:

{{ d['YieldCurveExample.java|idio']['constantYieldCurveDemo'] }}

<pre>
{{ d['yield-curve-output.json']['constantYieldCurveDemo'] }}
</pre>

Yield curves are often grouped into a [YieldCurveBundle](/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/interestrate/YieldCurveBundle.html), with each curve in the bundle referred to by a unique name. In this way, an instrument can designate the name of the yield curve it should be priced with, and this name can be used to reference the curve wtihin the bundle.

Bundles can be initialized in many ways, in this example an empty bundle is created and then a yield curve is added to it:

{{ d['YieldCurveExample.java|idio']['yieldCurveBundleDemo'] }}

<pre>
{{ d['yield-curve-output.json']['yieldCurveBundleDemo'] }}
</pre>


## Instruments

The [com.opengamma.financial.interestrate](/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/interestrate/package-summary.html) package defines the [InstrumentDerivative](/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/interestrate/InstrumentDerivative.html) interface, which is implemented by many different types of financial instrument and derivative.

We will start by looking at the [Cash](/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/interestrate/cash/definition/Cash.html) class:

{% set cash_class = d['/jsondocs/javadoc-data.json|javadocs']['packages']['com.opengamma.financial.interestrate.cash.derivative']['classes']['Cash'] %}
{{ cash_class['comment-text'] }}

We pass the following parameters to the constructor:

<pre>
{{ cash_class['constructors']['Cash(Currency,double,double,double,double,double,String)']['raw-comment-text'] }}
</pre>

{{ d['CashExample.java|idio']['cashDemo'] }}

<pre>
{{ d['cash-output.json']['cashDemo'] }}
</pre>

We pass the name of a yield curve, {{ d['cash-fields.json']['yieldCurveName'] }}, as one of the parameters. In order to do calculations on this instrument, we will need to reference a YieldCurveBundle which contains a curve with this name. We define a convenience method which returns such a bundle:

{{ d['CashExample.java|idio']['yieldCurveBundle'] }}

Now we can do some calculations. First, let's calculate the Par Rate of the loan using a [ParRateCalculator](/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/interestrate/ParRateCalculator.html):

{% set par_rate_class = d['/jsondocs/javadoc-data.json|javadocs']['packages']['com.opengamma.financial.interestrate']['classes']['ParRateCalculator'] %}
{{ par_rate_class['comment-text'] }}

Here is the calculation and the result:

{{ d['CashExample.java|idio']['parRateDemo'] }}

<pre>
{{ d['cash-output.json']['parRateDemo'] }}
</pre>

The ParRateCalculator is a subclass of the [AbstractInstrumentDerivativeVisitor](/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/interestrate/AbstractInstrumentDerivativeVisitor.html) class. There are several other subclasses which define calculations on interest-rate-related instruments.

Next we calculate the present value of this instrument:

{{ d['CashExample.java|idio']['presentValueDemo'] }}

<pre>
{{ d['cash-output.json']['presentValueDemo'] }}
</pre>

## Fixed Annuity

A cash loan with a single repayment is a very straightforward instrument. Next we look at the [AnnuityCouponFixed](/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/interestrate/annuity/definition/AnnuityCouponFixed.html) class which defines an annuity which pays a fixed coupon:

{% set annuity_class = d['/jsondocs/javadoc-data.json|javadocs']['packages']['com.opengamma.financial.interestrate.annuity.definition']['classes']['AnnuityCouponFixed'] %}
{{ annuity_class['comment-text'] }}

We will pass the following parameters to the constructor:

<pre>
{{ annuity_class['constructors']['AnnuityCouponFixed(Currency,double[],double,String,boolean)']['comment-text'] }}
</pre>

First we define constants that will be used in these examples:

{{ d['AnnuityExample.java|idio']['constants'] }}

This annuity will make annual payments until the maturity is reached. We start by creating a utility which will return an array of doubles representing the payment times:

{{ d['AnnuityExample.java|idio']['fixedPaymentTimes'] }}

Here is a simple example:

{{ d['AnnuityExample.java|idio']['fixedPaymentTimesDemo'] }}

<pre>
{{ d['annuity-output.json']['fixedPaymentTimesDemo'] }}
</pre>

Now we create our annuity, retrieve a list of payments, and calculate the present value in the same way as we did for the Cash loan:

{{ d['AnnuityExample.java|idio']['annuityFixedDemo'] }}

<pre>
{{ d['annuity-output.json']['annuityFixedDemo'] }}
</pre>

## Annuity Floating

Next we will create an annuity based on a variable coupon using the [AnnuityCouponIbor](/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/interestrate/annuity/definition/AnnuityCouponIbor.html) class.

{% set annuity_class = d['/jsondocs/javadoc-data.json|javadocs']['packages']['com.opengamma.financial.interestrate.annuity.definition']['classes']['AnnuityCouponIbor'] %}
{{ annuity_class['comment-text'] }}

This annuity will pay a variable coupon twice a year until the maturity, rather than once a year as in the case of the fixed rate annuity. So, we first create a method to return the times at which the annuity will pay:

{{ d['AnnuityExample.java|idio']['floatingPaymentTimes'] }}

Here is a simple example:

{{ d['AnnuityExample.java|idio']['floatingPaymentTimesDemo'] }}

<pre>
{{ d['annuity-output.json']['floatingPaymentTimesDemo'] }}
</pre>

Our yield curve bundle will need to contain Libor data also:

{{ d['AnnuityExample.java|idio']['yieldCurveBundle'] }}

Next we need to create an [IborIndex](/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/instrument/index/IborIndex.html) which will determine the coupon rate. In this case we will use a [EURIBOR6M](/{{ OG_VERSION }}/java/javadocs/com/opengamma/financial/instrument/index/EURIBOR6M.html).

{{ d['AnnuityExample.java|idio']['annuityFloatingDemo'] }}

<pre>
{{ d['annuity-output.json']['annuityFloatingDemo'] }}
</pre>

{% if False -%}

We will compute the present value of the loan. To do this, we need some market data. We bundle the derivative's market data into its own class. For the Cash loan, this is just the yield curve. Assume a yield curve with fixed rate of 2%. We create the curve, then assign the curve to a bundle of yield curves.

{{ d['CurveConstructionExample.java|idio']['interestRateDerivatives-presentValue'] }}

<pre>
{{ d['output.json']['interestRateDerivativeDemo'] }}
</pre>

## Annuity

{{ d['CurveConstructionExample.java|idio']['annuityDerivatives'] }}

<pre>
{{ d['output.json']['annuityDerivativeDemo'] }}
</pre>

{{ d['CurveConstructionExample.java|idio']['annuitySwaps'] }}

<pre>
{{ d['output.json']['annuitySwapDemo'] }}
</pre>

{% endif -%}
