/*
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.exchanges',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.masthead.menu',
        'og.common.routes',
        'og.common.search_results.core',
        'og.common.util.history',
        'og.common.util.ui.dialog',
        'og.common.util.ui.message',
        'og.common.util.ui.toolbar',
        'og.views.common.layout',
        'og.views.common.state'
    ],
    obj: function () {

        var api = og.api,
            common = og.common,
            details = common.details,
            history = common.util.history,
            masthead = common.masthead,
            routes = common.routes,
            search = common.search_results.core(),
            ui = common.util.ui,
            layout = og.views.common.layout,
            module = this,
            page_name = 'exchanges',
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {},
            exchanges,
            options = {
                slickgrid: {
                    'selector': '.og-js-results-slick', 'page_type': 'exchanges',
                    'columns': [
                        {id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link', filter_type: 'input'}
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {name: 'new', enabled: 'OG-disabled'},
                            {name: 'up', enabled: 'OG-disabled'},
                            {name: 'edit', enabled: 'OG-disabled'},
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'favorites', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-toolbar .og-js-buttons'
                    },
                    active: {
                        buttons: [
                            {name: 'new', enabled: 'OG-disabled'},
                            {name: 'up', handler: 'handler'},
                            {name: 'edit', enabled: 'OG-disabled'},
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'favorites', handler: 'handler'}
                        ],
                        location: '.OG-toolbar .og-js-buttons'
                    }
                }
            },
            default_details_page = function () {
                api.text({module: 'og.views.default', handler: function (template) {
                    $.tmpl(template, {
                        name: 'Exchanges',
                        favorites_list: history.get_html('history.exchanges.favorites') || 'no favorited exchanges',
                        recent_list: history.get_html('history.exchanges.recent') || 'no recently viewed exchanges'
                    }).appendTo($('#OG-details .og-main').empty());
                }});
            },
            details_page = function(args) {
                ui.toolbar(options.toolbar.active);
                api.rest.exchanges.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var f = details.exchange_functions;
                        details_json = result.data;
                        history.put({
                            name: details_json.template_data.name,
                            item: 'history.exchanges.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name, handler: function (template) {
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
            };
        module.rules = {
            load: {route: '/' + page_name + '/name:?', method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/name:?', method: module.name + '.load_filter'},
            load_exchanges: {route: '/' + page_name + '/:id/name:?', method: module.name + '.load_' + page_name}
        };
        return exchanges = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        exchanges.search(args);
                        masthead.menu.set_tab(page_name);
                        layout('default');
                    }}
                ]});
                if (args.id) return;
                default_details_page();
                ui.toolbar(options.toolbar['default']);
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
                check_state({args: args, conditions: [{new_page: exchanges.load}]});
                exchanges.details(args);
            },
            search: function (args) {search.load($.extend(options.slickgrid, {url: args}));},
            details: function (args) {details_page(args);},
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});