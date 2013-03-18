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
             * @param {Object} config.portfolio the data of a node, places form in create mode (optional)
             * @param {Function} config.handler the endopoint that the form submits too
             * @param {Function} config.complete fired when the form closes after a sucessful edit/create (optional) 
             * @param {Boolean} config.save_as toggle if save as button is present, default false 
             */
            var constructor = this, $selector, form_block = '.OG-blotter-form-block', form_wrapper, title, submit,
            blotter, error_block = '.OG-blotter-error-block', complete = config.complete || $.noop;
            var validation_handler = function (result) {
                if(result.error) {
                    og.common.util.ui.message({css: {position: 'inherit', whiteSpace: 'normal'},
                        location: '.OG-blotter-error-block', message: result.message});
                    return;
                }
                blotter.close();
                complete(result);
            };
            constructor.load = function () {
                // security type tells which type of form to load
                if (config.details) {
                    title = 'Edit Trade', submit = 'Update';
                    og.api.text({module: 'og.blotter.forms.blocks.form_edit_tash'}).pipe(function (template){
                        var type = config.details.data.security ?
                            config.details.data.security.type.toLowerCase() : 'fungibletrade';
                        $selector = $(template);
                        constructor.create_dialog();
                        constructor.populate(type, config);
                    });
                } else {
                    title = 'Add New Tade', submit = 'Create';
                    og.api.text({module: 'og.blotter.forms.blocks.form_types_tash'}).pipe(function (template){
                        $selector = $(template)
                        .on('change', function (event) {
                            constructor.populate($(event.target).val(), config);
                        });
                        constructor.create_dialog();
                    });
                }
            };
            constructor.populate = function (suffix, config) {
                var str, inner;
                str = 'og.blotter.forms.' + suffix;
                inner = str.split('.').reduce(function (acc, val) {
                    if (typeof acc[val] === 'undefined') constructor.clear();
                    else return acc[val];
                    }, window);
                if(inner) {
                    form_wrapper = new inner(config);
                    $('.ui-dialog-title').html(form_wrapper.title);
                }
            };
            constructor.create_dialog = function () {
                var buttons = {
                        'Save': function () {form_wrapper.submit(validation_handler);},
                        'Save as new' : function () {form_wrapper.submit_new(validation_handler);},
                        'Cancel': function () {$(this).dialog('close');}
                    };
                if (!config.save_as) delete buttons['Save as new'];
                blotter = new og.common.util.ui.dialog({
                    type: 'input', title: title, width: 530, height: 700, custom: $selector,
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