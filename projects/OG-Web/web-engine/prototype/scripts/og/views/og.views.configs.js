/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.configs',
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
            page_name = 'configs',
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {},
            configs,
            toolbar_buttons = {
                'new': function () {ui.dialog({
                    type: 'input',
                    title: 'Add configuration',
                    fields: [
                        {type: 'input', name: 'Name', id: 'name'},
                        {type: 'textarea', name: 'XML', id: 'xml'}
                    ],
                    buttons: {
                        'Ok': function () {
                            api.rest.configs.put({
                                handler: function (r) {
                                    if (r.error) return ui.dialog({type: 'error', html: r.message});
                                    ui.dialog({type: 'input', action: 'close'});
                                    routes.go(routes.hash(module.rules.load_new_configs,
                                            $.extend({}, routes.last().args, {id: r.meta.id, 'new': true})
                                    ));
                                },
                                name: ui.dialog({return_field_value: 'name'}),
                                xml: ui.dialog({return_field_value: 'xml'})
                            });
                        }
                    }
                })},
                'delete': function () {ui.dialog({
                    type: 'confirm',
                    title: 'Delete configuration?',
                    message: 'Are you sure you want to permanently delete this configuration?',
                    buttons: {
                        'Delete': function () {
                            $(this).dialog('close');
                            api.rest.configs.del({
                                handler: function (r) {
                                    if (r.error) return ui.dialog({type: 'error', message: r.message});
                                    routes.go(routes.hash(module.rules.load_delete,
                                            $.extend({}, routes.last().args, {deleted: true})
                                    ));
                                }, id: routes.last().args.id
                            });
                        }
                    }
                })}
            },
            options = {
                slickgrid: {
                    'selector': '.og-js-results-slick', 'page_type': 'configs',
                    'columns': [
                        {
                            id: 'type', name: 'Type', field: 'type', width: 160,
                            filter_type: 'select',
                            filter_type_options: [
                                'CurrencyMatrix',
                                'CurveSpecificationBuilderConfiguration',
                                'SimpleCurrencyMatrix',
                                'TimeSeriesMetaDataConfiguration',
                                'ViewDefinition',
                                'VolatilitySurfaceSpecification',
                                'VolatilitySurfaceDefinition',
                                'YieldCurveDefinition'
                            ]
                        },
                        {
                            id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link',
                            filter_type: 'input'
                        }
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {name: 'new', handler: toolbar_buttons['new']},
                            {name: 'up', enabled: 'OG-disabled'},
                            {name: 'edit', enabled: 'OG-disabled'},
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'favorites', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-toolbar .og-js-buttons'
                    },
                    active: {
                        buttons: [
                            {name: 'new', handler: toolbar_buttons['new']},
                            {name: 'up', handler: 'handler'},
                            {name: 'edit', handler: 'handler'},
                            {name: 'delete', handler: toolbar_buttons['delete']},
                            {name: 'favorites', handler: 'handler'}
                        ],
                        location: '.OG-toolbar .og-js-buttons'
                    }
                }
            },
            load_configs_without = function (field, args) {
                check_state({args: args, conditions: [{new_page: configs.load, stop: true}]});
                delete args[field];
                configs.search(args);
                routes.go(routes.hash(module.rules.load_configs, args));
            },
            default_details_page = function () {
                api.text({module: 'og.views.default', handler: function (template) {
                    $.tmpl(template, {
                        name: 'Configs',
                        favorites_list: history.get_html('history.configs.favorites') || 'no favorited configs',
                        recent_list: history.get_html('history.configs.recent') || 'no recently viewed configs',
                        new_list: history.get_html('history.configs.new') || 'no new configs'
                    }).appendTo($('#OG-details .og-main').empty());
                }});
            },
            details_page = function (args) {
                ui.toolbar(options.toolbar.active);
                api.rest.configs.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        details_json = result.data;
                        history.put({
                            name: details_json.templateData.name,
                            item: 'history.configs.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name + '.' + args.type, handler: function (template) {
                            var json = details_json.templateData, $warning,
                                warning_message = 'This configuration has been deleted';
                            json.configData = json.configJSON ? JSON.stringify(json.configJSON, null, 4)
                                    : json.configXML ? json.configXML : '';
                            $.tmpl(template, json).appendTo($('#OG-details .og-main').empty());
                            $warning = $('#OG-details .OG-warning-message');
                            if (json.deleted) $warning.html(warning_message).show(); else $warning.empty().hide();
                            details.favorites();
                            ui.message({location: '#OG-details', destroy: true});
                            ui.content_editable({
                                attribute: 'data-og-editable',
                                handler: function () {
                                    routes.go(routes.hash(module.rules.load_edit_configs, $.extend(args, {
                                        edit: 'true'
                                    })));
                                }
                            });
                            $('.OG-config .og-js-save-config').click(function () {
                                var data_obj, data = $('.OG-config [data-og=config-data]').val(),
                                    rest_obj = {
                                    handler: function (e) {
                                        if (e.error) return alert(e.message);
                                        $('.og-js-msg').html('saved');
                                        ui.message({location: '#OG-details', message: 'saved'});
                                        setTimeout(function () {
                                            ui.message({location: '#OG-details', destroy: true});
                                            editing = false;
                                        }, 250);
                                        routes.go(routes.hash(module.rules.load_edit_configs, $.extend(args, {
                                            edit: 'true'
                                        })));
                                    },
                                    id: routes.current().args.id,
                                    loading: function () {
                                        $('.og-js-msg').html('saving...');
                                        ui.message({location: '#OG-details', message: 'saving...'});
                                    },
                                    name: $('[data-og-editable=name]').html()
                                };
                                data_obj = data.charAt(0) === '<' ? {xml: data} : {json: data};
                                api.rest.configs.put($.extend(rest_obj, data_obj));
                            });
                            /* TMP work on configs form */
                            /*
                            var form = new ui.Form({
                                module: 'og.views.forms.view-definition',
                                selector: '#OG-details .og-form-container'
                            });
                            form.children = [
                                new form.Block({module: 'og.views.forms.view-definition-main'}),
                                new form.Block({module: 'og.views.forms.view-definition-result-model-definition'}),
                                new form.Block({module: 'og.views.forms.view-definition-execution-parameters'}),
                                new form.Block({module: 'og.views.forms.tabs'}),
                                new form.Block({module: 'og.views.forms.view-definition-column-set-name'}),
                                new form.Block({module: 'og.views.forms.view-definition-column-values'}),
                                new form.Block({module: 'og.views.forms.view-definition-specific-requirements-fields'}),
                                new form.Block({module: 'og.views.forms.constraints'}),
                                new form.Block({module: 'og.views.forms.constraints'}),
                                new form.Block({
                                    module: 'og.views.forms.view-definition-resolution-rule-transform-fields'
                                })
                            ];
                            form.dom();
                            */
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({location: '#OG-details', message: {0: 'loading...', 3000: 'still loading...'}});
                    }
                });
            },
            state = {};
        module.rules = {
            load: {route: '/' + page_name + '/name:?/type:?', method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/name:?/type:?', method: module.name + '.load_filter'},
            load_delete: {route: '/' + page_name + '/deleted:/name:?/type:?', method: module.name + '.load_delete'},
            load_configs: {route: '/' + page_name + '/:id/name:?/type:?', method: module.name + '.load_' + page_name},
            load_new_configs: {
                route: '/' + page_name + '/:id/new:/name:?/type:?', method: module.name + '.load_new_' + page_name
            },
            load_edit_configs: {
                route: '/' + page_name + '/:id/edit:/name:?/type:?', method: module.name + '.load_edit_' + page_name
            }
        };
        return configs = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function (args) {
                        configs.search(args);
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
                        configs.load(args);
                        args.id
                            ? routes.go(routes.hash(module.rules.load_configs, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }}
                ]});
                delete args['filter'];
                search.filter($.extend(args, {filter: true}));
            },
            load_delete: function (args) {
                configs.search(args);
                routes.go(routes.hash(module.rules.load, {name: args.name}));
            },
            load_new_configs: load_configs_without.partial('new'),
            load_edit_configs: load_configs_without.partial('edit'),
            load_configs: function (args) {
                check_state({args: args, conditions: [{new_page: configs.load}]});
                configs.details(args);
            },
            search: function (args) {search.load($.extend(options.slickgrid, {url: args}));},
            details: function (args) {details_page(args);},
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});