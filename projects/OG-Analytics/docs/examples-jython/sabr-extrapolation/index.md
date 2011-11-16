{% set smile_class = d['/javadoc-data.json|javadocs']['packages']['com.opengamma.financial.model.option.pricing.analytic.formula']['classes']['SABRExtrapolationRightFunction'] %}
{% set sabr_formula_data_class = d['/javadoc-data.json|javadocs']['packages']['com.opengamma.financial.model.volatility.smile.function']['classes']['SABRFormulaData'] %}
{% set constructor = smile_class['constructors']['SABRExtrapolationRightFunction(double,SABRFormulaData,double,double,double)'] %}
{% set sabr_constructor = sabr_formula_data_class['constructors']['SABRFormulaData(double,double,double,double)'] %}

Let's explore an example of SMILE extrapolation. In this example we want to use the SABRExtrapolationRightFunction to generate some example data.

Here are the class comments describing the formula implemented:

{{ smile_class['fulltext'].replace("\\n", "<br />") }}

Here is the constructor used to initialize the class:

{{ constructor['source-html'] }}

As can be seen from the constructor, we need to create an instance of SABRFormulaData, which we do as follows:

{{ d['smile-multi-mu.py|fn|idio|jythoni|pyg']['sabr-data'] }}

Where these parameters are:

<pre>
{{ sabr_constructor['raw-comment-text'] }}
</pre>

Now we just need to define a few more constants and we can create an instance of SABRExtrapolationRightFunction:

{{ d['smile-multi-mu.py|fn|idio|jythoni|pyg']['sabr-function'] }}

The price() method of SABRExtrapolationRightFunction requires a EuropeanVanillaOption as its argument, we will create them with different strikes in the following way:

{{ d['smile-multi-mu.py|fn|idio|jythoni|pyg']['create-option'] }}

So finally we can calculate the price of this option using our extrapolation function:

{{ d['smile-multi-mu.py|fn|idio|jythoni|pyg']['calculate-price'] }}

Here is the source of the price method:

{{ smile_class['methods']['price(EuropeanVanillaOption)']['source-html'] }}

We also want to be able to calculate the implied volatility:

{{ d['smile-multi-mu.py|fn|idio|jythoni|pyg']['calculate-implied-vol'] }}

Now that we have this working, we can construct a table of extrapolated prices for various strikes and values for mu.

{{ d['smile-multi-mu.py|fn|idio|jythoni|pyg']['create-table'] }}

Here is what the data file generated looks like:
<pre>
{% for l in d['sabr-extrapolation-data.txt'].split("\\n")[0:10] -%}
{{ l }}
{% endfor -%}
</pre>

Now we switch to R where we are going to plot this generated data.

{{ d['smile-multi-mu.R|fn|idio|rint|pyg']['read-data'] }}

{{ d['smile-multi-mu.R|fn|idio|rint|pyg']['plot-data'] }}

![Extrapolation Price](extrapolation-price.png)

{{ d['smile-multi-mu.R|fn|idio|rint|pyg']['plot-implied-vol'] }}

![Extrapolation Smile](extrapolation-smile.png)

Also we want to calculate the price density:

{{ d['smile-multi-mu.R|fn|idio|rint|pyg']['calculate-density'] }}

And graph it:

{{ d['smile-multi-mu.R|fn|idio|rint|pyg']['plot-density'] }}

![Extrapolation Density](extrapolation-density.png)

