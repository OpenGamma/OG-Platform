/**
 * view for securities section
 */
$.register_module({
    name: 'og.views.batches',
    dependencies: [
        'og.api.rest', 'og.api.text',
        'og.common.routes', 'og.common.masthead.menu', 'og.common.search_results.core',
        'og.common.util.ui.message', 'og.views.common.layout', 'og.common.util.ui.toolbar', 'og.views.common.state'
    ],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, batches,
            masthead = og.common.masthead, search = og.common.search_results.core(), details = og.common.details,
            ui = og.common.util.ui, layout = og.views.common.layout,
            page_name = 'batches',
            check_state = og.views.common.state.check.partial('/' + page_name),
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'batches',
                'columns': [
                    {id: 'observationDate', name: 'ObservationDate', field: 'id', width: 130, cssClass: 'og-link',
                        filter_type: 'input'},
                    {id: 'observationTime', name: 'ObservationTime', field: 'observationTime', width: 130,
                        filter_type: 'input'},
                    {id: 'status', name: 'Status', field: 'status', width: 130, filter_type: 'input'}
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
            default_page = function () {$('#OG-details .og-main').html('default ' + page_name + ' page');},
            new_page = function (args) {
                masthead.menu.set_tab(page_name);
                layout('default');
                ui.toolbar(default_toolbar_options);
                batches.search(args);
            };
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_batches: {route: '/' + page_name + '/:id/:observation_time', method: module.name + '.load_batches'}
        };
        return batches = {
            details: function (args) {
                ui.toolbar(active_toolbar_options);
                api.batches.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var json = result.data, f = details.batch_functions;
                        og.api.text({module: module.name, handler: function (template) {
                            $.tmpl(template, json.templateData).appendTo($('#OG-details .og-main').empty());
                            f.results('.OG-batch .og-js-results', json.data.batch_results);
                            f.errors('.OG-batch .og-js-errors', json.data.batch_errors);
                            ui.message({location: '#OG-details', destroy: true});
                            details.favorites();
                        }});
                    },
                    observation_date: args.id,
                    observation_time: args.observation_time,
                    loading: function () {
                        ui.message({location: '#OG-details', message: {0: 'loading...', 3000: 'still loading...'}});
                    }
                });
            },
            load: function (args) {
                check_state({args: args, conditions: [{new_page: new_page}]});
                if (!args.id) default_page();
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