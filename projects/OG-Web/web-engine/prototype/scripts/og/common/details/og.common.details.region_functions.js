/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.details.region_functions',
    dependencies: ['og.common.routes'],
    obj: function () {
        var routes = og.common.routes, region_functions,
            render_keys = function (selector, json) {
                var html, lbl, data, i;
                html = lbl = data = '';
                for (i in json) lbl +=  '<div>' + i + '</div>';
                for (i in json)
                    data += '<div><strong class="OG-editable" og-edit="name">' + json[i] + '</strong></div>';
                html += '<div class="og-lbl">' + lbl + '</div>';
                html += '<div>' + data + '</div>';
                $(selector).html(html);
            },
            render_regions = function (selector, json) {
                $(selector).html(json.reduce(function (acc, val) {
                    acc.push('<tr><td><a class="og-js-live-anchor" href="',
                        routes.prefix() + routes.hash(og.views.regions.rules.load_item, {id: val.id}), '">', val.name,
                        '</a></td></tr>'
                    );
                    return acc;
                }, []).join('') || '<tr><td>No regions</td></tr>');
            };
        return region_functions = {render_keys: render_keys, render_regions: render_regions};
    }
});