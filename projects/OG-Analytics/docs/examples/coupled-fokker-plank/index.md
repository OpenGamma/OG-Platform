
{{ d['CoupledFokkerPlankExample.java|pyg'] }}

Data:

<pre>
{% for l in d['state-1-density'].split("\n")[0:10] -%}
{{ l }}
{% endfor -%}
</pre>

{{ d['graph-density.R|fn|idio|rint|pyg'] }}

<img src="/{{ OG_VERSION }}/analytics/state-1-plot.png" />
<img src="/{{ OG_VERSION }}/analytics/state-2-plot.png" />

