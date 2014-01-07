/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.Dialog',
    dependencies: [],
    obj: function () {
        return function (config) {
            /**
             * launches a trade entry dialog
             * @param {Object} config.details the data of a current trade, places form in edit mode (optional)
             * @param {Object} config.node the data of a node, places form in create mode (optional)
             * @param {Function} config.handler the endpoint that the form submits too
             * @param {Function} config.complete fired when the form closes after a successful edit/create (optional)
             * @param {Boolean} config.save_as toggle if save as button is present, default false 
             */
            var constructor = this, $selector, form_block = '.OG-blotter-form-block', form_wrapper, title, submit,
                blotter, error_block = '.OG-blotter-error-block', complete = config.complete || $.noop;
            constructor.load = function () {
                // security type specifies which form to create
                if (config.details) {
                    title = 'Edit Trade';
                    submit = 'Update';
                    og.api.text({module: 'og.blotter.forms.blocks.form_edit_tash'}).pipe(function (template) {
                        var type = config.details.data.security ?
                                config.details.data.security.type.toLowerCase() : 'fungibletrade';
                        $selector = $(template);
                        constructor.create_dialog();
                        constructor.populate(type, config);
                    });
                } else {
                    title = 'Add Trade';
                    submit = 'Create';
                    og.api.text({module: 'og.blotter.forms.blocks.form_types_tash'}).pipe(function (template) {
                        $selector = $(template);
                        $selector.find('a').on('click', function (event) {
                            constructor.populate($(event.target).attr('data-type'), config);
                            $('.OG-blotter-trade-select').hide();
                        });
                        constructor.create_dialog();
                        setup_existing();
                    });
                }
            };
            /** validation_handler is passed to each form via their submit or submit_new,
             *  The form api submit listener in each form attempts to submit the form
             *  If an error exists the message is displayed otherwise the form is closed
             */
            var validation_handler = function (result) {
                if (result.error) {
                    display_error(result.message);
                    return;
                }
                blotter.close();
                complete(result);
            };
            var display_error = function (message) {
                og.common.util.ui.message({css: {position: 'inherit', whiteSpace: 'normal'},
                    location: '.OG-blotter-error-block', message: message});
            };
            var setup_existing = function () {
                $('#OG-blotter-existing-trade').autocomplete({
                    source: function (obj, handler) {
                        og.api.rest.positions.get({
                            handler: function (result) {
                                var arr = result.data.data.map(function (val) {
                                    var arr = val.split('|');
                                    return {value: arr[0], label: arr[1], id: arr[0], node: arr[1]};
                                });
                                handler(arr);
                            },
                            loading: '',
                            page_size: 10,
                            page: 1,
                            identifier: '*' + obj.term.replace(/\s/g, '*') + '*'
                        });
                    },
                    minLength: 1,
                    select: function (e, data) {
                        get_portfolio(e, data);
                    }
                });
            };
            var get_portfolio = function (e, data) {
                if (og.analytics) {
                    og.api.rest.configs.get({id: og.analytics.url.last.main.viewdefinition}).pipe(function (result) {
                        add_existing(result.data.template_data.configJSON.data.identifier, data);
                    });
                } else if (config.node && config.node.portfolio) {
                    add_existing(config.node.portfolio, data);
                } else {
                    display_error('Unable to add position/trade. Portfolio ID is not available');
                }

            };
            var add_existing = function (portfolio, data) {
                og.api.rest.portfolios.put({
                    position: data ? data.item.value : $('#OG-blotter-existing-trade').val(),
                    id: portfolio,
                    node: config.node.id
                }).pipe(function (result) {
                    validation_handler(result);
                });
            };
            constructor.populate = function (suffix, config) {
                var str, Inner;
                str = 'og.blotter.forms.' + suffix;
                Inner = str.split('.').reduce(function (acc, val) {
                    if (acc[val] === 'undefined') {
                        constructor.clear();
                    } else {
                        return acc[val];
                    }
                }, window);
                if (Inner) {
                    form_wrapper = new Inner(config);
                    $('.OG-blotter-trade-save').show();
                    $('.ui-dialog-title').html(form_wrapper.title);
                } else {
                    if (config.details && config.details.data) {
                        display_error('No trade entry form available for ' + str + "<br/> " +
                                          JSON.stringify(config.details.data || {}, null, '\t'));
                    } else {
                        display_error('No trade entry form available for ' + str);
                    }
                }
            };
            constructor.create_dialog = function () {
                var buttons = {
                    'Save': {
                        text: 'Save',
                        'class' : 'OG-blotter-trade-save',
                        click: function () {form_wrapper.submit(validation_handler); }
                    },
                    'Save as new' : {
                        text: 'Save as new',
                        'class': 'OG-blotter-trade-saveasnew',
                        click: function () {form_wrapper.submit_new(validation_handler); }
                    },
                    'Cancel': {
                        text: 'Cancel',
                        'class': 'OG-blotter-trade-cancel',
                        click: function () {$(this).dialog('close'); }
                    }
                };
                if (!config.save_as) {
                    delete buttons['Save as new'];
                }
                blotter = new og.common.util.ui.dialog({
                    type: 'input',
                    title: title,
                    width: 530,
                    height: 700,
                    custom: $selector,
                    buttons: buttons
                });
            };
            constructor.clear = function () {
                $(form_block).empty();
                $('.ui-dialog-title').html("Add New Trade");
            };
            constructor.load();
        };
    }
});