/**
 * view for securities section
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
            page_name = 'batches',
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {},
            batches,
            options = {
                slickgrid: {
                    'selector': '.og-js-results-slick', 'page_type': 'batches',
                    'columns': [
                        {id: 'ob_date', name: 'ObservationDate', field: 'date', width: 130, cssClass: 'og-link',
                            filter_type: 'input'},
                        {id: 'ob_time', name: 'ObservationTime', field: 'time', width: 130, filter_type: 'input'},
                        {id: 'status', name: 'Status', field: 'status', width: 130}
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
                        name: 'Batches',
                        favorites_list: history.get_html('history.batches.favorites') || 'no favorited batches',
                        recent_list: history.get_html('history.batches.recent') || 'no recently viewed batches',
                        new_list: history.get_html('history.batches.new') || 'no new batches'
                    }).appendTo($('#OG-details .og-main').empty());
                }});
            },
            details_page = function (args){
                ui.toolbar(options.toolbar.active);
                api.rest.batches.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var f = details.batch_functions;
                        details_json = result.data;
                        history.put({
                            name: details_json.templateData.name,
                            item: 'history.batches.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name, handler: function (template) {
                            $.tmpl(template, details_json.templateData).appendTo($('#OG-details .og-main').empty());
                            f.results('.OG-batch .og-js-results', details_json.data.batch_results);
                            f.errors('.OG-batch .og-js-errors', details_json.data.batch_errors);
                            ui.message({location: '#OG-details', destroy: true});
                            details.favorites();
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({location: '#OG-details', message: {0: 'loading...', 3000: 'still loading...'}});
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
                ui.toolbar(options.toolbar['default']);
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
            details: function (args) {details_page(args);},
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});