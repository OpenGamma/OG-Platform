This document automatically displays source code and output of all examples run by the RunExamples.java script.

[TOC]

{% for k in d['/shared/docs.sqlite3'].kv_storage().keys() -%}
{% if "example" in k and k.startswith("com.opengamma") and k.endswith("(PrintStream):source") -%}
### {{ k }}
{{ highlight(d['/shared/docs.sqlite3'][ k ], 'java') }}

{% set class_name = k.split(":")[0] -%}
{% set function_name = k.split(":")[1].split("(")[0] -%}

*Output:*

<pre>
{{ d['/shared/example-output.json'][class_name][function_name] }}
</pre>

*Fields:*

<table>
{% for k in  sorted(d['/shared/example-fields.json'][class_name]) -%}
<tr><th>{{ k }}</th><td>{{ d['/shared/example-fields.json'][class_name][k] }}</td></tr>
{% endfor -%}
</table>

{% endif -%}
{% endfor -%}
