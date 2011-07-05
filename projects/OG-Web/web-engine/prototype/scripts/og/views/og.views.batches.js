/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.batches',
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
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {},
            batches,
            options = {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': 'batches',
                    'columns': [
                        {id: 'ob_date', field: 'date', width: 130, cssClass: 'og-link', filter_type: 'input',
                            name: '<input type="text" placeholder="observation date" class="og-js-ob_date-filter" style="width: 110px;">'},
                        {id: 'ob_time', field: 'time', width: 130, filter_type: 'input',
                            name: '<input type="text" placeholder="observation time" class="og-js-ob_time-filter" style="width: 110px;">'},
                        {id: 'status', field: 'status', width: 130,
                            name: 'Status'}
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
                    $.tmpl(template, {
                        name: 'Batches',
                        favorites_list: history.get_html('history.batches.favorites') || 'no favorited batches',
                        recent_list: history.get_html('history.batches.recent') || 'no recently viewed batches',
                        new_list: history.get_html('history.batches.new') || 'no new batches'
                    }).appendTo($('.OG-js-details-panel .OG-details').empty());
                    ui.toolbar(options.toolbar['default']);
                }});
            },
            details_page = function (args){
                api.rest.batches.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var f = details.batch_functions;
                        details_json = result.data;
                        history.put({
                            name: details_json.template_data.name,
                            item: 'history.batches.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name, handler: function (template) {
                            $.tmpl(template, details_json.template_data).appendTo($('.OG-js-details-panel .OG-details').empty());
                            f.results('.OG-batch .og-js-results', details_json.data.batch_results);
                            f.errors('.OG-batch .og-js-errors', details_json.data.batch_errors);
                            ui.message({location: '.OG-js-details-panel', destroy: true});
                            ui.toolbar(options.toolbar.active);
                            ui.expand_height_to_window_bottom({element: '.OG-details-container .og-details-content', offsetpx: -48});
                            details.favorites();
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({
                            location: '.OG-js-details-panel',
                            message: {0: 'loading...', 3000: 'still loading...'}});
                    }
                });
            };
        module.rules = {
            load: {route: '/' + page_name + '/ob_date:?/ob_time:?', method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/ob_date:?/ob_time:?',
                method: module.name + '.load_filter'},
            load_batches: {route: '/' + page_name + '/:id', method: module.name + '.load_batches'}
        };
        return batches = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function (args) {
                        batches.search(args);
                        masthead.menu.set_tab(page_name);
                        layout('default');
                    }}
                ]});
                if (args.id) return;
                default_details_page();
            },
            load_filter: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        state = {filter: true};
                        batches.load(args);
                        args.id
                            ? routes.go(routes.hash(module.rules.load_batches, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }}
                ]});
                delete args['filter'];
                search.filter($.extend(args, {filter: true}));
            },
            load_batches: function (args) {
                check_state({args: args, conditions: [{new_page: batches.load}]});
                batches.details(args);
            },
            search: function (args) {search.load($.extend(options.slickgrid, {url: args}));},
            details: details_page,
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});