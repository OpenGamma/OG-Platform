/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.Dialog',
    dependencies: [],
    obj: function () {   
        return function () {
            var dialog = this, form, close = '.OG-blotter-form-close', Form = og.common.util.ui.Form; 
            dialog.form_block = '.OG-blotter-form-block';
            dialog.generate = function (load_handler) {
                return function (css_class) {
                    $(css_class).html('Loading form...');
                    form = window.temp = new Form({
                        selector: css_class, 
                        data: {}, 
                        module: 'og.blotter.dialog_tash'})
                    .on('form:load', load_handler);
                    form.children.push(dialog.dropdown());
                    form.dom();
                };
            };
            dialog.dropdown = function () {
                var str, dropdown =  new form.Block({module: 'og.blotter.forms.blocks.form_types_tash'})
                    .on('change', 'select', function (event) {
                        str = 'og.blotter.forms.' + $(event.target).val();
                        var inner = str.split('.').reduce(function (acc, val) {
                            if (typeof acc[val] === 'undefined') 
                                $(dialog.form_block).empty(); 
                            else return acc[val];
                        }, window);
                        if(inner) new inner();
                     });
                return dropdown;
            },
            dialog.load = function () {
                og.common.util.ui.dialog({
                    type: 'input', title: 'Add New Trade', width: 530, height: 700,
                    form: dialog.generate(dialog.form_handler),
                    buttons: {
                        'Create': function () {$(this).dialog('close');},
                        'Cancel': function () {$(this).dialog('close');}
                    }
                });                  
            };
            dialog.form_handler = function() {
            };
            dialog.load();
        };
    }
});