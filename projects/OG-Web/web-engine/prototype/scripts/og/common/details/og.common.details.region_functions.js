/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.details.region_functions',
    dependencies: ['og.common.routes'],
    obj: function () {
        var routes = og.common.routes;
        var render_regions = function (selector, json) {
                $(selector).html(json.reduce(function (acc, val) {
                    acc.push('<tr><td colspan="2"><a class="og-js-live-anchor" href="',
                        routes.prefix() + routes.hash(og.views.regions.rules.load_item, {id: val.id}), '">', val.name,
                        '</a></td></tr>');
                    return acc;
                }, []).join('') || '<tr><td colspan="2">No regions</td></tr>');
            };
        return {render_regions: render_regions};
    }
});