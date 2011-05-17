/**
 * view for securities section
 */
$.register_module({
    name: 'og.views.batches',
    dependencies: [
        'og.api.rest', 'og.api.text',
        'og.common.routes', 'og.common.masthead.menu', 'og.common.search_results.core',
        'og.common.util.ui.message', 'og.views.common.layout', 'og.common.util.ui.toolbar', 'og.views.common.state',
        'og.common.util.history'
    ],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, batches,
            masthead = og.common.masthead, search = og.common.search_results.core(), details = og.common.details,
            ui = og.common.util.ui, layout = og.views.common.layout, history = og.common.util.history,
            page_name = 'batches',
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {}, // The returned json for the details area
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'batches',
                'columns': [
                    {id: 'ob_date', name: 'ObservationDate', field: 'id', width: 130, cssClass: 'og-link',
                        filter_type: 'input'},
                    {id: 'ob_time', name: 'ObservationTime', field: 'id_time', width: 130, filter_type: 'input'},
                    {id: 'status', name: 'Status', field: 'status', width: 130}
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
                        name: 'Batches',
                        favorites_list: history.get_html('history.batches.favorites') || 'no favorited batches',
                        recent_list: history.get_html('history.batches.recent') || 'no recently viewed batches',
                        new_list: history.get_html('history.batches.new') || 'no new batches'
                    }).appendTo($('#OG-details .og-main').empty());
                }});
            },
            new_page = function (args) {
                masthead.menu.set_tab(page_name);
                layout('default');
                ui.toolbar(default_toolbar_options);
                batches.search(args);
            };
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/id:?/id_time:?/ob_date:?/ob_time:?',
                method: module.name + '.load_filter'},
            load_batches: {route: '/' + page_name + '/id:/id_time:', method: module.name + '.load_batches'}
        };
        return batches = {
            details: function (args) {
                ui.toolbar(active_toolbar_options);
                api.batches.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var f = details.batch_functions;
                        details_json = result.data;
                        history.put({
                            name: details_json.templateData.name,
                            item: 'history.batches.recent',
                            value: routes.current().hash
                        });
                        og.api.text({module: module.name, handler: function (template) {
                            $.tmpl(template, details_json.templateData).appendTo($('#OG-details .og-main').empty());
                            f.results('.OG-batch .og-js-results', details_json.data.batch_results);
                            f.errors('.OG-batch .og-js-errors', details_json.data.batch_errors);
                            ui.message({location: '#OG-details', destroy: true});
                            details.favorites();
                        }});
                    },
                    observation_date: args.id,
                    observation_time: args.id_time,
                    loading: function () {
                        ui.message({location: '#OG-details', message: {0: 'loading...', 3000: 'still loading...'}});
                    }
                });
            },
            load: function (args) {
                check_state({args: args, conditions: [{new_page: new_page}]});
                if (!args.id) default_page();
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
            search: function (args) {search.load($.extend(search_options, {url: args}));},
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});