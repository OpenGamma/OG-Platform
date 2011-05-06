/**
 * view for securities section
 */
$.register_module({
    name: 'og.views.exchanges',
    dependencies: [
        'og.common.routes',
        'og.common.masthead.menu',
        'og.common.search_results.core',
        'og.common.util.ui.message',
        'og.views.common.layout',
        'og.common.util.ui.toolbar',
        'og.common.util.history'
    ],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, exchanges,
            masthead = og.common.masthead, search = og.common.search_results.core(), details = og.common.details,
            ui = og.common.util.ui, layout = og.views.common.layout, history = og.common.util.history,
            page_name = 'exchanges',
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {},
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'exchanges',
                'columns': [
                    {id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link', filter_type: 'input'}
                ]
            },
            default_toolbar_options = {
                buttons: [
                    {name: 'new', enabled: 'OG-disabled'},
                    {name: 'up', enabled: 'OG-disabled'},
                    {name: 'edit', enabled: 'OG-disabled'},
                    {name: 'delete', enabled: 'OG-disabled'},
                    {name: 'favorites', enabled: 'OG-disabled'}
                ],
                location: '.OG-toolbar .og-js-buttons'
            },
            active_toolbar_options = {
                buttons: [
                    {name: 'new', enabled: 'OG-disabled'},
                    {name: 'up', handler: 'handler'},
                    {name: 'edit', enabled: 'OG-disabled'},
                    {name: 'delete', enabled: 'OG-disabled'},
                    {name: 'favorites', handler: 'handler'}
                ],
                location: '.OG-toolbar .og-js-buttons'
            },
            default_page = function () {
                og.api.text({module: 'og.views.default', handler: function (template) {
                    $.tmpl(template, {
                        name: 'Exchanges',
                        favorites_list: history.get_html('history.exchanges.favorites') || 'no favorited exchanges',
                        recent_list: history.get_html('history.exchanges.recent') || 'no recently viewed exchanges'
                    }).appendTo($('#OG-details .og-main').empty());
                }});
            };
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/name:?', method: module.name + '.load_filter'},
            load_exchanges: {route: '/' + page_name + '/:id/name:?', method: module.name + '.load_' + page_name}
        };
        return exchanges = {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                layout('default');
                ui.toolbar(default_toolbar_options);
                search.load($.extend(search_options, {url: args}));
                default_page();
            },
            load_filter: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        state = {filter: true};
                        exchanges.load(args);
                        args.id
                            ? routes.go(routes.hash(module.rules.load_exchanges, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }}
                ]});
                delete args['filter'];
                search.filter($.extend(args, {filter: true}));
            },
            load_exchanges: function (args) {
                // Load search if changed
                if (routes.last()) {
                    if (routes.last().page !== module.rules.load.route) {
                        masthead.menu.set_tab(page_name);
                        ui.toolbar(active_toolbar_options);
                        search.load($.extend(search_options, {url: args}));
                    } else ui.toolbar(active_toolbar_options);
                } else {
                    masthead.menu.set_tab(page_name);
                    ui.toolbar(active_toolbar_options);
                    search.load($.extend(search_options, {url: args}));
                }
                // Setup details page
                layout('default');
                api.exchanges.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var f = details.exchange_functions;
                        console.log(result.data);
                        details_json = result.data;
                        history.put({
                            name: details_json.template_data.name,
                            item: 'history.exchanges.recent',
                            value: routes.current().hash
                        });
                        og.api.text({module: module.name, handler: function (template) {
                            $.tmpl(template, details_json.template_data).appendTo($('#OG-details .og-main').empty());
                            f.render_info('.OG-exchange .og-js-info', details_json);
                            details.favorites();
                            ui.message({location: '#OG-details', destroy: true});
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({location: '#OG-details', message: {0: 'loading...', 3000: 'still loading...'}});
                    }
                });
            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});