/**
 * view for portfolios section
 */
$.register_module({
    name: 'og.views.portfolios',
    dependencies: [
        'og.common.routes', 'og.common.masthead.menu', 'og.common.search_results.core',
        'og.common.util.ui.message', 'og.common.util.ui.toolbar',
        'og.api.rest', 'og.api.text',
        'og.views.common.layout', 'og.views.common.state'
    ],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, portfolios,
            masthead = og.common.masthead, search = og.common.search_results.core(), details = og.common.details,
            ui = og.common.util.ui, layout = og.views.common.layout,
            page_name = 'portfolios',
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {}, // The returned json for the details area
            /**
             * Options for SlickGrid.
             * Generate the search results columns.
             */
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'portfolios',
                'columns': [
                    {id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link', filter_type: 'input'}
                ]
            },
            /**
             * Options for dialog boxes
             */
            dialog_new = {
                type: 'input',
                title: 'Add New Portfolio',
                fields: [{type: 'input', name: 'Portfolio Name', id: 'name'}],
                buttons: {
                    "Ok": function () {
                        $(this).dialog('close');
                        api.portfolios.put({
                            handler: function (r) {
                                if (r.error) return ui.dialog({type: 'error', message: r.message});
                                routes.go(routes.hash(module.rules.load_new_portfolios,
                                        $.extend({}, routes.last().args, {id: r.meta.id, 'new': true})
                                ));
                            },
                            name: ui.dialog({return_field_value: 'name'})
                        });
                    }
                }
            },
            dialog_delete = {
                type: 'confirm',
                title: 'Delete portfolio?',
                message: 'Are you sure you want to permanently delete this portfolio?',
                buttons: {
                    'Delete': function () {
                        var obj = {
                            id: routes.last().args.id,
                            handler: function (r) {
                                var last = routes.last(), args_obj = {};
                                if (r.error) return ui.dialog({type: 'error', message: r.message});
                                if (details_json.templateData.parentNodeId) {
                                    args_obj.node = details_json.templateData.parentNodeId;
                                    args_obj.id = details_json.templateData.id;
                                }
                                routes.go(routes.hash(module.rules.load_delete,
                                        $.extend(true, {}, last.args, {deleted: true}, args_obj)
                                ));
                            }
                        };
                        if (routes.last().args.node) obj.node = routes.last().args.node;
                        $(this).dialog('close');
                        api.portfolios.del(obj);
                    }
                }
            },
            buttons = {
                'new': function () {ui.dialog(dialog_new)},
                'delete': function () {ui.dialog(dialog_delete)}
            },
            /**
             * Options for the toolbar
             */
            default_toolbar_options = {
                buttons: [
                    {name: 'new', handler: buttons['new']},
                    {name: 'up', enabled: 'OG-disabled'},
                    {name: 'edit', enabled: 'OG-disabled'},
                    {name: 'delete', enabled: 'OG-disabled'},
                    {name: 'favorites', enabled: 'OG-disabled'}
                ],
                location: '.OG-toolbar .og-js-buttons'
            },
            active_toolbar_options = {
                buttons: [
                    {name: 'new', handler: buttons['new']},
                    {name: 'up', handler: 'handler'},
                    {name: 'edit', handler: 'handler'},
                    {name: 'delete', handler: buttons['delete']},
                    {name: 'favorites', handler: 'handler'}
                ],
                location: '.OG-toolbar .og-js-buttons' 
            },
            load_portfolios_without = function (field, args) {
                check_state({args: args, conditions: [{new_page: portfolios.load}]});
                delete args[field];
                portfolios.search(args);
                routes.go(routes.hash(module.rules.load_portfolios, args));
            },
            default_page = function () {
                $('#OG-details .og-main').html('default ' + page_name + ' page');
            };
        module.rules = {
            load: {route: '/' + page_name + '/name:?', method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/:node?/name:?', method: module.name + '.load_filter'},
            load_delete: {
                route: '/' + page_name + '/deleted:/name:?/:id?/:node?', method: module.name + '.load_delete'
            },
            load_portfolios: {
                route: '/' + page_name + '/:id/:node?/name:?', method: module.name + '.load_' + page_name
            },
            load_new_portfolios: {
                route: '/' + page_name + '/:id/:node?/new:/name:?', method: module.name + '.load_new_' + page_name
            },
            load_edit_portfolios: {
                route: '/' + page_name + '/:id/:node?/edit:/name:?', method: module.name + '.load_edit_' + page_name
            }
        };
        return portfolios = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function (args) {
                        portfolios.search(args);
                        masthead.menu.set_tab(page_name);
                        layout('default');
                    }}
                ]});
                if (args.id) return;
                default_page();
                ui.toolbar(default_toolbar_options);
            },
            load_filter: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        state = {filter: true};
                        portfolios.load(args);
                        args.id
                            ? routes.go(routes.hash(module.rules.load_portfolios, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }}
                ]});
                delete args['filter'];
                search.filter($.extend(args, {filter: true}));
            },
            load_delete: function (args) {
                portfolios.search(args);
                portfolios.load($({name: args.name}, args));
            },
            load_new_portfolios: load_portfolios_without.partial('new'),
            load_edit_portfolios: load_portfolios_without.partial('edit'),
            load_portfolios: function (args) {
                check_state({args: args, conditions: [{new_page: portfolios.load}]});
                portfolios.details(args);
            },
            search: function (args) {
                search.load($.extend(search_options, {url: args}));
            },
            details: function (args) {
                ui.toolbar(active_toolbar_options);
                api.portfolios.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var f = details.portfolio_functions;
                        details_json = result.data; // global within this view
                        og.api.text({module: module.name, handler: function (template) {
                            var stop_loading = (function () {
                                var total_parts = 2, parts_loaded = 0;
                                return function () {
                                    if (++parts_loaded !== total_parts) return;
                                    ui.message({location: '#OG-details', destroy: true})
                                };
                            })();
                            $.tmpl(template, details_json.templateData).appendTo($('#OG-details .og-main').empty());
                            f.render_breadcrumb('.OG-portfolio .og-js-breadcrumb', details_json);
                            f.hook_up_portfolio_button(details_json);
                            f.hook_up_position_add();
                            f.render_portfolio_rows('.OG-portfolio .og-js-portfolios', details_json, stop_loading);
                            f.render_position_rows('.OG-portfolio .og-js-positions', details_json, stop_loading);
                            ui.toggle_text_on_focus.set_selector('.OG-portfolio .og-js-create-portfolio-node');
                            ui.toggle_text_on_focus.set_selector('.OG-portfolio .og-js-add-position input');
                            ui.content_editable({
                                attribute: 'data-og-editable',
                                handler: function () {
                                    routes.go(routes.hash(module.rules.load_edit_portfolios, $.extend(args, {
                                        edit: 'true'
                                    })));
                                }
                            });
                            details.favorites();
                        }});
                    },
                    id: args.id,
                    node: args.node,
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