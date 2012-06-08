    /**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.common.versions',
    dependencies: ['og.common.util.ui.message', 'og.views.common.layout'],
    obj: function () {
        var PANEL = '.OG-layout-admin-details-south',
            HEADER = PANEL + ' .ui-layout-header',
            CONTENT = PANEL + ' .ui-layout-content',
            FOOTER = PANEL + ' .ui-layout-footer',
            versions;
        return versions = {
            load: function () {
                versions.setup();
                var ui = og.common.util.ui, routes = og.common.routes, cur = routes.current();
                if (!routes.current().args.id) versions.clear();
                og.api.rest[cur.page.substring(1)].get({
                    id: cur.args.id, version: '*',
                    handler: function (result) {
                        var args = routes.current().args, page = routes.current().page.substring(1),
                            view = og.views[page], $list, table_header = '<div class="og-container"><table>' +
                                '<colgroup></colgroup><colgroup></colgroup><colgroup></colgroup>',
                            table_footer = '</table></div>';
                        if (result.error) {
                            view.error(result.message);
                            return routes.go(routes.hash(view.rules.load_item, args, {del: ['version']}));
                        }
                        $list = $(result.data.data.reduce(function (acc, val, idx) {
                            var arr = val.split('|'), ver = routes.current().args.version,
                                cur = !idx ? '<span>&nbsp;Latest</span>' : '',
                                sel = ver === arr[0] || cur && ver === '*' ? ' class="og-selected"' : '',
                                extras = {add: {version: cur ? '*' : arr[0]}, del: ['node']};
                            return acc + '<tr' + sel + '><td><a class="og-js-live-anchor" href="' +
                                routes.prefix() + routes.hash(view.rules.load_item, args, extras) + '">' +
                                arr[0] + '</a>' + cur + '</td><td>' + arr[1] + '</td><td>' +
                                og.common.util.date(arr[2]) + '</td></tr>';
                        }, table_header) + table_footer)
                        .click(function (e) {
                            if ($(e.target).is('a')) return; // just follow the link
                            // all click handlers should fire (include og-js-live-anchor)
                            routes.go($(e.target).parents('tr:first').find('td:first a').trigger('click').attr('href'));
                        });
                        $(CONTENT).html($list);
                        ui.message({location: '.OG-layout-admin-details-south', destroy: true});
                        og.views.common.layout.main.resizeAll();
                    },
                    loading: function () {
                        ui.message({
                            location: '.OG-layout-admin-details-south',
                            css: {bottom: '1px'},
                            message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    }
                });

            },
            clear: function () {$(CONTENT).empty();},
            setup: function () {
                var layout = og.views.common.layout, routes = og.common.routes,
                    header_html = '\
                        <div><header><h2>Version History</h2></header></div>\
                        <div class="og-version-header">\
                          <table>\
                            <colgroup></colgroup><colgroup></colgroup><colgroup></colgroup>\
                            <thead><tr><th>Reference</th><th>Name</th><th>Valid from</th></tr></thead>\
                          </table>\
                        </div>\
                        <div class="og-divider"></div>'
                    ;
                if (!$(HEADER).length || (routes.current() && !routes.current().args.version)) $(PANEL).html( '\
                    <div class="ui-layout-header">' + header_html + '</div>\
                    <div class="ui-layout-content"></div>'
                ).removeClass('OG-sync').addClass('OG-versions');
                og.views.common.layout.inner.initContent('south');
            }
        }
    }
});