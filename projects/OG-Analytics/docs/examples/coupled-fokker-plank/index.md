
{{ d['CoupledFokkerPlankExample.java|pyg'] }}

<pre>
{{ d['CoupledFokkerPlankExample.java|fn|java'] }}
</pre>

Here are the first few lines of data for state 1:

<pre>
{% for l in d['state-1-density.txt'].split("\n")[0:10] -%}
{{ l }}
{% endfor -%}
</pre>

And for state 2:

<pre>
{% for l in d['state-2-density.txt'].split("\n")[0:10] -%}
{{ l }}
{% endfor -%}
</pre>

R is used to graph the data, here is the script:

{{ d['graph-density.R|fn|idio|rintbatch|pyg'] }}

![State 1 Plot](state-1-plot.png)

![State 2 Plot](state-2-plot.png)

