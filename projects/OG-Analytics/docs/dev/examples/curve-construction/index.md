This tutorial covers yield curve construction and derivative pricing, and introduces some OpenGamma classes for working with matrices, defining functions and root finding.

Instances of the DoubleMatrix1D and DoubleMatrix2D classes can be created by passing an array of double or Double elements.

{% set matrix_example = d['/shared/example-output.json']['com.opengamma.analytics.example.curveconstruction.MatrixExample'] -%}
{% set function_example = d['/shared/example-output.json']['com.opengamma.analytics.example.curveconstruction.FunctionExample'] -%}
{% set curve_example = d['/shared/example-output.json']['com.opengamma.analytics.example.curveconstruction.CurveExample'] -%}
{% set cash_example = d['/shared/example-output.json']['com.opengamma.analytics.example.curveconstruction.CashExample'] -%}
{% set annuity_example = d['/shared/example-output.json']['com.opengamma.analytics.example.curveconstruction.AnnuityExample'] -%}
{% set yield_curve_example = d['/shared/example-output.json']['com.opengamma.analytics.example.curveconstruction.YieldCurveExample'] -%}

{% set annuity_fields = d['/shared/example-fields.json']['com.opengamma.analytics.example.curveconstruction.AnnuityExample'] -%}

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.MatrixExample:initMatrixDemo(PrintStream):source'], 'java') }}

<pre>
{{ matrix_example['initMatrixDemo'] }}
</pre>

The ColtMatrixAlgebra class provides methods to perform calculations on Matrix objects.

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.MatrixExample:matrixAlgebraDemo(PrintStream):source'], 'java') }}

<pre>
{{ matrix_example['matrixAlgebraDemo'] }}
</pre>

## Functions

The `com.opengamma.math.function` package includes the Function interface. We will look at some examples of using this interface.

Here is the polynomial we will work with:

<div>
\begin{eqnarray}
f(x) &=& (x-5)^3 \\\\
&=& x^3 - 15x^2 + 75x - 125
\end{eqnarray}
</div>

We create a RealPolynomialFunction1D with these coefficients, verify that 5 is a zero, and take a derivative:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.FunctionExample:polyDerivativeDemo(PrintStream):source'], 'java') }}

Here are the coefficients of the derivative:

<pre>
{{ function_example['polyDerivativeDemo'] }}
</pre>

## Rootfinding

The `com.opengamma.math.rootfinding` package has classes which implement various methods for finding roots of functions. In the previous section we defined a polynomial with roots at $x=5$. In this section we will try to apply the rootfinding classes to this polynomial.

Here is the class comment for the CubicRealRootFinder.

{{ d['/shared/docs.sqlite3']['com.opengamma.analytics.math.rootfinding.CubicRealRootFinder::comment-text'] }}

We create an instance of the CubicRealRootFinder and call its getRoots() method:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.FunctionExample:cubicRealRootFindingDemo(PrintStream):source'], 'java') }}

It finds the 3 roots at $x=5$:

<pre>
{{ function_example['cubicRealRootFindingDemo'] }}
</pre>

Next we use a BrentSingleRootFinder, passing bracketing values which we know to be above and below the root:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.FunctionExample:brentSingleRootFinderDemo(PrintStream):source'], 'java') }}

It finds a root at approximately $x=5$:

<pre>
{{ function_example['brentSingleRootFinderDemo'] }}
</pre>

If we don't correctly bracket the root, an IllegalArgumentException is raised:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.FunctionExample:brentSingleRootFinderNotBracketingDemo(PrintStream):source'], 'java') }}

<pre>
{{ function_example['brentSingleRootFinderNotBracketingDemo'] }}
</pre>

## Curves

The `com.opengamma.math.curve` package includes the abstract Curve class.

We begin with a simple constant curve.

{{ d['/shared/docs.sqlite3']['com.opengamma.analytics.math.curve.ConstantDoublesCurve::comment-text'] }}

We create a ConstantDoublesCurve and verify that it returns the constant $y$ value at several values for $x$:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.CurveExample:constantDoublesCurveDemo(PrintStream):source'], 'java') }}

<pre>
{{ curve_example['constantDoublesCurveDemo'] }}
</pre>

The abstract DoublesCurve class allows curves to be created by passing in arrays of x and y coordinates.

{{ d['/shared/docs.sqlite3']['com.opengamma.analytics.math.curve.DoublesCurve::comment-text'] }}

The NodalDoublesCurve creates a curve which is only defined for the $x$ values specified:

{{ d['/shared/docs.sqlite3']['com.opengamma.analytics.math.curve.NodalDoublesCurve::comment-text'] }}

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.CurveExample:nodalDoublesCurveDemo(PrintStream):source'], 'java') }}

<pre>
{{ curve_example['nodalDoublesCurveDemo'] }}
</pre>

The InterpolatedDoublesCurve creates a curve which is defined for all values of $x$ within the minimum and maximum specified values of $x$, with the $y$ values being interpolated where they are not defined, according to the chosen interpolator:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.CurveExample:interpolatedDoublesCurveDemo(PrintStream):source'], 'java') }}

<pre>
{{ curve_example['interpolatedDoublesCurveDemo'] }}
</pre>

We can create a curve valid for all values of $x$ by choosing the CombinedInterpolatorExtrapolator which lets you specify an interpolator, a left extrapolator, and a right extrapolator:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.CurveExample:interpolatorExtrapolatorDoublesCurveDemo(PrintStream):source'], 'java') }}

<pre>
{{ curve_example['interpolatorExtrapolatorDoublesCurveDemo'] }}
</pre>

There are many different interpolators and extrapolators available in the com.opengamma.math.interpolation package.

Curves can also be created based on functions, where the y coordinates are determined by applying the specified function to any x coordinate that is requested. There are also utilities for shifting curves by a constant or defining curves based on a mathematical operation on two or more other curves.

Curves are often given names, where these are not given explicitly then curves are automatically assigned a unique ID. It is common to refer to bundles of curves in the form of LinkedHashMaps where the curve names form the keys and the corresponding curve objects form the values in the hashmap.

## Yield Curves

Yield Curves and Discount Curves (which each inherit from YieldAndDiscountCurve) are created by passing a Curve to the constructor. The $x$ coordinate is interpreted as time $t$. Here is an example of a yield curve based on a constant interest rate of {{ d['/shared/example-fields.json']['com.opengamma.analytics.example.curveconstruction.YieldCurveExample']['y'] }}:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.YieldCurveExample:constantYieldCurveDemo(PrintStream):source'], 'java') }}

<pre>
{{ yield_curve_example['constantYieldCurveDemo'] }}
</pre>

Yield curves are often grouped into a YieldCurveBundle with each curve in the bundle referred to by a unique name. In this way, an instrument can designate the name of the yield curve it should be priced with, and this name can be used to reference the curve wtihin the bundle.

Bundles can be initialized in many ways, in this example an empty bundle is created and then a yield curve is added to it:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.YieldCurveExample:yieldCurveBundleDemo(PrintStream):source'], 'java') }}

<pre>
{{ yield_curve_example['yieldCurveBundleDemo'] }}
</pre>

## Instruments

The `com.opengamma.financial.interestrate` package defines the InstrumentDerivative interface, which is implemented by many different types of financial instrument and derivative.

We will start by looking at the Cash class:

{{ d['/shared/docs.sqlite3']['com.opengamma.analytics.financial.interestrate.cash.derivative.Cash::comment-text'] }}

We pass the following parameters to the constructor:

<pre>
{{ d['/shared/docs.sqlite3']['com.opengamma.analytics.financial.interestrate.cash.derivative:Cash(Currency,double,double,double,double,double,String):raw-comment-text'] }}
</pre>

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.CashExample:cashDemo(PrintStream):source'], 'java') }}

<pre>
{{ cash_example['cashDemo'] }}
</pre>

We pass the name of a yield curve, {{ d['/shared/example-fields.json']['com.opengamma.analytics.example.curveconstruction.CashExample']['yieldCurveName'] }}, as one of the parameters. In order to do calculations on this instrument, we will need to reference a YieldCurveBundle which contains a curve with this name. We define a convenience method which returns such a bundle:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.CashExample:getBundle():source'], 'java') }}

Now we can do some calculations. First, let's calculate the Par Rate of the loan using a ParRateCalculator:

{{ d['/shared/docs.sqlite3']['com.opengamma.analytics.financial.interestrate.ParRateCalculator::comment-text'] }}

Here is the calculation and the result:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.CashExample:parRateDemo(PrintStream):source'], 'java') }}

<pre>
{{ cash_example['parRateDemo'] }}
</pre>

The ParRateCalculator is a subclass of the AbstractInstrumentDerivativeVisitor class. There are several other subclasses which define calculations on interest-rate-related instruments.

Next we calculate the present value of this instrument:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.CashExample:presentValueDemo(PrintStream):source'], 'java') }}

<pre>
{{ cash_example['presentValueDemo'] }}
</pre>

## Fixed Annuity

A cash loan with a single repayment is a very straightforward instrument. Next we look at the AnnuityCouponFixed class which defines an annuity which pays a fixed coupon:

{{ d['/shared/docs.sqlite3']['com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponFixed::comment-text'] }}

We will pass the following parameters to the constructor:

<pre>
{{ d['/shared/docs.sqlite3']['com.opengamma.analytics.financial.interestrate.annuity.definition:AnnuityCouponFixed(Currency,double[],double,double,double[],String,boolean):raw-comment-text'] }}
</pre>

We have constants defined that will be used in these examples:

<table>
{% for k in sorted(d['/shared/example-fields.json']['com.opengamma.analytics.example.curveconstruction.AnnuityExample']) -%}
<tr><td>{{ k }}</td><td>{{ d['/shared/example-fields.json']['com.opengamma.analytics.example.curveconstruction.AnnuityExample'][k] }}</td></tr>
{% endfor -%}
</table>

This annuity will make annual payments until the maturity is reached. We start by creating a utility which will return an array of doubles representing the payment times:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.AnnuityExample:fixedPaymentTimes(int):source'], 'java') }}

Here is a simple example:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.AnnuityExample:fixedPaymentTimesDemo(PrintStream):source'], 'java') }}

<pre>
{{ annuity_example['fixedPaymentTimesDemo'] }}
</pre>

Now we create our annuity, retrieve a list of payments, and calculate the present value in the same way as we did for the Cash loan:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.AnnuityExample:annuityFixedDemo(PrintStream):source'], 'java') }}

<pre>
{{ annuity_example['annuityFixedDemo'] }}
</pre>

{% if False -%}

TODO develop annuity floating example again...

## Annuity Floating

Next we will create an annuity based on a variable coupon using the AnnuityCouponIbor class.

{{ d['/shared/docs.sqlite3']['com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponIbor::comment-text'] }}

This annuity will pay a variable coupon twice a year until the maturity, rather than once a year as in the case of the fixed rate annuity. So, we first create a method to return the times at which the annuity will pay:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.AnnuityExample:floatingPaymentTimes(int):source'], 'java') }}

Here is a simple example:

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.AnnuityExample:floatingPaymentTimesDemo(PrintStream):source'], 'java') }}

<pre>
{{ annuity_example['floatingPaymentTimesDemo'] }}
</pre>

{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.curveconstruction.AnnuityExample:annuityFloatingDemo(PrintStream):source'], 'java') }}

<pre>
{{ annuity_example['annuityFloatingDemo'] }}
</pre>

We will compute the present value of the loan. To do this, we need some market data. We bundle the derivative's market data into its own class. For the Cash loan, this is just the yield curve. Assume a yield curve with fixed rate of 2%. We create the curve, then assign the curve to a bundle of yield curves.

{{ d['CurveConstructionExample.java|idio']['interestRateDerivatives-presentValue'] }}

<pre>
{{ d['/shared/example-output.json']['interestRateDerivativeDemo'] }}
</pre>

## Annuity

{{ d['CurveConstructionExample.java|idio']['annuityDerivatives'] }}

<pre>
{{ d['/shared/example-output.json']['annuityDerivativeDemo'] }}
</pre>

{{ d['CurveConstructionExample.java|idio']['annuitySwaps'] }}

<pre>
{{ d['/shared/example-output.json']['annuitySwapDemo'] }}
</pre>

{% endif -%}
