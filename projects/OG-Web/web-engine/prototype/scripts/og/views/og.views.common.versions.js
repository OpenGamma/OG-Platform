/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.versions',
    dependencies: ['og.common.util.ui.message'],
    obj: function () {
        var SELECTOR = '.ui-layout-inner-south .ui-layout-content', versions;
        return versions = {
            load: function () {
                var cur = og.common.routes.current(), ui = og.common.util.ui, routes = og.common.routes;
                if (!routes.current().args.id) {versions.clear()}
                og.api.rest[cur.page.substring(1)].get({
                    id: cur.args.id, version: '*',
                    handler: function (r) {
                        var thead = '<thead><tr>\
                                       <th>Reference</th><th>Name</th><th>Valid from</th>\
                                     </tr></thead>',
                        cols = '<colgroup align="left"></colgroup>\
                                <colgroup align="left"></colgroup>',
                        build_url = function (version) {
                            var current = routes.current().args,
                                page = routes.current().page.substring(1);
                            delete current.node; // not supported yet
                            return routes.hash(
                                og.views[page].rules['load_' + page], $.extend({}, current, {version: version})
                            );
                        },
                        $list = $(r.data.data.reduce(function (acc, val, i) {
                            var arr = val.split('|'), cur, sel, ver = routes.current().args.version;
                            cur = !i ? '<span> Latest</span>' : '';
                            sel = ver === arr[0] ? ' class="og-selected"' : '';
                            return acc +
                                '<tr' + sel + '>' +
                                    '<td><a href="#' + build_url(arr[0]) + '">' + arr[0] + '</a>' + cur + '</td>' +
                                    '<td>' + arr[1] + '</td>' +
                                    '<td>' + arr[2] + '</td>' +
                                '</tr>';
                        }, '<div class="og-container"><table>' + thead) + '</table></div>')
                        .click(function (e) {
                            var version = $(e.target).parents('tbody tr').find('td:first-child a').text();
                            if (version) routes.go(build_url(version));
                        });
                        $(SELECTOR).html($list);
                        ui.message({location: '.ui-layout-inner-south', destroy: true});
                    },
                    loading: function () {
                        ui.message({
                            location: '.ui-layout-inner-south',
                            message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    }
                });
            },
            clear: function () {$(SELECTOR).html('History not available for this view')}
        }
    }
});