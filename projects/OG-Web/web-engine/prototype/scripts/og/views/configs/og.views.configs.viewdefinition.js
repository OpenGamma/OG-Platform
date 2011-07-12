/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.configs.viewdefinition',
    dependencies: [
        'og.common.util.ui.message',
        'og.common.util.ui.Form',
        'og.views.forms.Constraints',
        'og.views.forms.Dropdown'
    ],
    obj: function () {
        var ui = og.common.util.ui, forms = og.views.forms,
            sets = 'calculationConfiguration',
            cols = 'portfolioRequirementsBySecurityType',
            reqs = 'portfolioRequirement';
        return function (data) {
            og.dev.log('data!', data.template_data.configJSON);
            var id_count = 0, prefix = 'view_def_', master = data.template_data.configJSON, column_set_tabs,
                form = new ui.Form({
                    module: 'og.views.forms.view-definition',
                    data: master,
                    selector: '#OG-details .og-main',
                    extras: {name: master.name},
                    processor: function (data) {
                        // remove undefineds that we added
                        if (data[sets]) data[sets].forEach(function (set, set_idx) {
                            if (set[cols]) set[cols].forEach(function (col, col_idx) {
                                if (col[reqs]) col[reqs] = col[reqs].filter(function (req) {return req !== undefined;});
                            });
                        });
                    },
                    handlers: [
                        {type: 'form:load', handler: function () {
                            ui.message({location: '#OG-details', destroy: true});
                        }},
                        {type: 'form:submit', handler: function (result) {
                            og.dev.log(result.data);
                        }}
                    ]
                });
            form.children = [
                new form.Block({ // form item_0
                    module: 'og.views.forms.view-definition-identifier-currency',
                    children: [
                        new forms.Dropdown({
                            form: form, resource: 'portfolios', index: 'identifier', value: master.identifier,
                            placeholder: 'Please choose a portfolio...', fields: [0, 2]
                        })
                    ],
                    handlers: [{type: 'form:load', handler: function () {
                        $('#' + form.id + ' select[name=currency]').val(master.currency);
                    }}]
                }),
                (function () { // form item_1
                    var level = 'resultModelDefinition', result_def = master[level],
                        fields = [
                            'primitiveOutputMode', 'securityOutputMode', 'positionOutputMode',
                            'aggregatePositionOutputMode', 'tradeOutputMode'
                        ],
                        ids = [
                            prefix + id_count++, prefix + id_count++, prefix + id_count++,
                            prefix + id_count++, prefix + id_count++
                        ];
                    return new form.Block({
                        module: 'og.views.forms.view-definition-result-model-definition',
                        extras: fields.reduce(function (acc, val, idx) {return acc[val] = ids[idx], acc;}, {}),
                        handlers: [{type: 'form:load', handler: function () {
                            fields.forEach(function (field, idx) {
                                $('#' + ids[idx]).attr('name', level + '.' + field).val(result_def[field]);
                            });
                        }}]
                    })
                })(),
                new form.Block({ // form item_2
                    module: 'og.views.forms.view-definition-execution-parameters',
                    handlers: [{type: 'form:load', handler: function () {
                        ['DeltaCalcPeriod', 'FullCalcPeriod'].forEach(function (suffix) {
                            ['min', 'max'].forEach(function (prefix) {
                                $('#' + prefix + suffix).val(master[prefix + suffix]);
                            });
                        });
                    }}]
                })
            ];
            (function () {
                var column_sets = new form.Block, set_ids = [], new_column_set;
                new_column_set = function () {
                    og.dev.log('add a column set');
                };
                form.children.push(
                    new form.Block({ // form item_3
                        module: 'og.views.forms.tabs',
                        extras: {
                            tabs: master[sets].reduce(function (acc, set, idx) {
                                return acc + '<a class="og-tab og-js-tab' + (idx ? '' : ' og-active') +
                                    '" href="#"><span>' + set.name + '</span></a>';
                            }, '')
                        },
                        handlers: [
                            {type: 'form:load', handler: function () {
                                $('#' + form.id + ' a.og-js-tab').each(function (idx, tab) {
                                    $(tab).data('idx', idx);
                                    set_ids.slice(1).forEach(function (id) {$('#' + id).hide();});
                                });
                            }},
                            {type: 'click', selector: 'a.og-js-tab', handler: function (e) {
                                var $target = $(e.target), active_cl = 'a.og-active', new_cl = 'a.og-js-new',
                                    is_active = $target.is(active_cl) || $target.parent(active_cl).length,
                                    is_new = $target.is(new_cl) || $target.parent(new_cl).length;
                                if (is_active) return false;
                                if (is_new) return new_column_set(), false;
                                if (!$target.is('a.og-js-tab')) $target = $target.parent('a.og-js-tab');
                                set_ids.forEach(function (id, idx) {
                                    $('#' + id)[idx === $target.data('idx') ? 'show' : 'hide']();
                                });
                                $target.addClass('og-active').siblings('.og-active').removeClass('og-active');
                                return false;
                            }}
                        ]
                    }),
                    column_sets // form item_4
                );
                set_ids = master[sets].map(function (set, set_idx) {
                    var id = prefix + id_count++, column_set = new form.Block({
                        wrap: '<div id="' + id + '">{{html html}}</div>',
                        handlers: [
                            {
                                type: 'click', selector: 'div#' + id + ' .og-js-add.og-js-port-req',
                                handler: function (e) {
                                    og.dev.log('add a portfolio requirement');
                                    return false;
                                }
                            },
                            {
                                type: 'click', selector: 'div#' + id + ' .og-js-remove.og-js-port-req',
                                handler: function (e) {
                                    var $li = $(e.target).parents('li'),
                                        reqs = $li.find('select').attr('name').split('.').slice(0, -1),
                                        index = reqs.pop();
                                    reqs.reduce(function (a, v) {return a[v];}, master)[index] = undefined;
                                    $li.remove();
                                    return false;
                                }
                            },
                            {
                                type: 'click', selector: 'div#' + id + ' .og-js-remove.og-js-column-set',
                                handler: function (e) {
                                    og.dev.log('remove a column set', id, set_idx);
                                    return false;
                                }
                            }
                        ]
                    });
                    column_set.children = [
                        new form.Field({ // column set name
                            module: 'og.views.forms.view-definition-column-set-name',
                            extras: {name: sets + '.' + set_idx + '.name', value: set.name},
                            handlers: [{
                                selector: 'input[name=' + sets + '.' + set_idx + '.name]', type: 'keyup',
                                handler: function (e) {
                                    $('#' + form.id + ' a.og-js-tab.og-active span').text($(e.target).val());
                                }
                            }]
                        })
                    ];
                    if (set[cols]) set[cols].forEach(function (col, col_idx) {
                        var reqs_block;
                        column_set.children.push(new form.Block({
                            module: 'og.views.forms.view-definition-column-values',
                            children: [
                                new forms.Dropdown({
                                    form: form, resource: 'securities', rest_options: {meta: true},
                                    index: [sets, set_idx, cols, col_idx, 'securityType'].join('.'),
                                    value: col.securityType,
                                    placeholder: 'Please select...'
                                }),
                                (reqs_block = new form.Block({
                                    wrap: '<ul class="og-portfolio-requirements">{{html html}}</ul>'
                                }))
                            ]
                        }));
                        if (col[reqs]) col[reqs].forEach(function (req, req_idx) {
                            var sel_name = [sets, set_idx, cols, col_idx, reqs, req_idx, 'requiredOutput'].join('.'),
                                cons_name = [sets, set_idx, cols, col_idx, reqs, req_idx, 'constraints'].join('.');
                            reqs_block.children.push(new form.Block({
                                module: 'og.views.forms.view-definition-portfolio-requirement',
                                extras: {title: 'Portfolio Requirement ' + (req_idx + 1), name: sel_name},
                                children: [
                                    new forms.Dropdown({
                                        form: form, resource: 'valuerequirementnames', index: sel_name,
                                        value: req.requiredOutput, rest_options: {meta: true},
                                        placeholder: 'Please select...'
                                    }),
                                    new forms.Constraints({form: form, data: req.constraints, index: cons_name})
                                ]
                            }));
                        });
                    });
                    column_set.children.push(new form.Field({
                        generator: function (handler) {
                            handler('<span class="OG-icon og-icon-add og-js-add og-js-port-req">' +
                                'Add portfolio requirement</span>');
                        }
                    }));
                    column_sets.children.push(column_set);
                    return id;
                });
            })()
            // new form.Block({module: 'og.views.forms.view-definition-specific-requirements-fields'}),
            // new form.Block({module: 'og.views.forms.constraints'}),
            // new form.Block({module: 'og.views.forms.constraints'}),
            // new form.Block({
            //     module: 'og.views.forms.view-definition-resolution-rule-transform-fields'
            // })
            form.dom();
        };
    }
});