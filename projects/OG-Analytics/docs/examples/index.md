Here are some examples:

<ul>
    {% for subdir in subdirectories -%}
    <li><a href="{{ subdir }}">{{ subdir }}</a></li>
    {% endfor -%}
</ul>
