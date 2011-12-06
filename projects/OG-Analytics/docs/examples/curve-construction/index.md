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

Here are examples of using BrentSingleRootFinder and CubicRealRootFinder,
including attempting to call BrentSingleRootFinder with arguments that don't
bracket the root.

{{ d['CurveConstructionExample.java|idio']['rootFindingDemo'] }}

<pre>
{{ d['output.json']['rootFindingDemo'] }}
</pre>

## Interest Rate Derivatives and Calculators

Yield curves are built using a combinatino of interest rate instruments, including Swaps, Futures, Forward Rate Agreements and Cash Loans.

### Cash

{% set cash_class = d['/jsondocs/javadoc-data.json|javadocs']['packages']['com.opengamma.financial.interestrate.cash.definition']['classes']['Cash'] %}

A cash loan paying simple compounding interest. These are important as these simple deposit rates are often used at the short end of the curve, before the first available swap maturity.

{{ cash_class['comment-text'] }}

We pass the following parameters to the constructor:

<pre>
{{ cash_class['constructors']['Cash(Currency,double,double,double,String)']['raw-comment-text'] }}
</pre>

{{ d['CurveConstructionExample.java|idio']['interestRateDerivatives'] }}

With our derivative object defined, we can make calculations based on it. OpenGamma implements these calculations as [Visitors](http://en.wikipedia.org/wiki/Visitor_pattern).

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

## Curve Construction

TBD
