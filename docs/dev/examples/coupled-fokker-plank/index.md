{{ highlight(d['/shared/docs.sqlite3']['com.opengamma.analytics.example.coupledfokkerplank.CoupledFokkerPlankExample:runCoupledFokkerPlank(PrintStream):source'], 'java') }}

{% set example_output = d['/shared/example-output.json']['com.opengamma.analytics.example.coupledfokkerplank.CoupledFokkerPlankExample']['runCoupledFokkerPlank'] -%}
{% set example_json = json.loads(example_output) -%}
<pre>
{{ example_output }}
</pre>

Here are the first few lines of data for state 1:

<pre>
{% for l in example_json['state_1_data'].splitlines()[0:10] -%}
{{ l }}
{% endfor -%}
</pre>

And for state 2:

<pre>
{% for l in example_json['state_2_data'].splitlines()[0:10] -%}
{{ l }}
{% endfor -%}
</pre>

R is used to graph the data, here is the script:

{{ d['graph-density.R|fn|rintbatch|pyg'] }}

![State 1 Plot](state-1-plot.png)

![State 2 Plot](state-2-plot.png)
