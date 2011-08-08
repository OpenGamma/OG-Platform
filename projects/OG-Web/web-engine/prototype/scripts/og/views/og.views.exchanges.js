/*
 * @copyright 2009 - present by OpenGamma Inc
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
            search,
            ui = common.util.ui,
            module = this,
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name),
            exchanges,
            options = {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': 'exchanges',
                    'columns': [
                        {
                            id: 'name', field: 'name', width: 300, cssClass: 'og-link', filter_type: 'input',
                            name: '<input type="text" placeholder="Name" '
                                + 'class="og-js-name-filter" style="width: 280px;">',
                            toolTip: 'name'
                        }
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'new', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-toolbar'
                    },
                    active: {
                        buttons: [
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'new', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-toolbar'
                    }
                }
            },
            default_details_page = function () {
                api.text({module: 'og.views.default', handler: function (template) {
                    var $html = $.tmpl(template, {
                        name: 'Exchanges',
                        recent_list: history.get_html('history.exchanges.recent') || 'no recently viewed exchanges'
                    }), layout = og.views.common.layout;
                    $('.ui-layout-inner-center .ui-layout-header').html($html.find('> header').html());
                    $('.ui-layout-inner-center .ui-layout-content').html($html.find('> section').html());
                    layout.inner.close('north'), $('.ui-layout-inner-north').empty();
                    ui.toolbar(options.toolbar['default']);
                    layout.inner.resizeAll();
                }});
            },
            details_page = function(args) {
                api.rest.exchanges.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var json = result.data;
                        history.put({
                            name: json.template_data.name,
                            item: 'history.exchanges.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name, handler: function (template) {
                            var $html = $.tmpl(template, json), layout = og.views.common.layout;
                            $('.ui-layout-inner-center .ui-layout-header').html($html.find('> header').html());
                            $('.ui-layout-inner-center .ui-layout-content').html($html.find('> section').html());
                            layout.inner.close('north'), $('.ui-layout-inner-north').empty();
                            ui.toolbar(options.toolbar.active);
                            ui.message({location: '.ui-layout-inner-center', destroy: true});
                            layout.inner.resizeAll();
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({
                            location: '.ui-layout-inner-center',
                            message: {0: 'loading...', 3000: 'still loading...'}});
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
                    }}
                ]});
                if (args.id) return;
                default_details_page();
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
            search: function (args) {
                if (!search) search = common.search_results.core();
                search.load($.extend(options.slickgrid, {url: args}));
            },
            details: details_page,
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});