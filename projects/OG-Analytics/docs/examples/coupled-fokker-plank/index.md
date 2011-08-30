
{{ d['CoupledFokkerPlankExample.java|pyg'] }}

Output:

<pre>
{{ d['CoupledFokkerPlankExample.java|fn|java'] }}
</pre>

Data:

<pre>
{% for l in d['state-1-density'].split("\n")[0:10] -%}
{{ l }}
{% endfor -%}
</pre>

