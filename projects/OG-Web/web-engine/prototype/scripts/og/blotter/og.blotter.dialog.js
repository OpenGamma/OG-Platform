/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.Dialog',
    dependencies: [],
    obj: function () {   
        return function (config) {
            var dialog = this, $selector, form_block = '.OG-blotter-form-block', form_wrapper, title, submit;
            dialog.load = function () {
                if(config) {
                    title = "Edit Trade", submit = "Update";
                    og.api.text({module: 'og.blotter.forms.blocks.form_edit_tash'}).pipe(function (template){
                        $selector = $(template);
                        dialog.create();
                        dialog.populate(config.data.security.type.toLowerCase(), config.data);
                    });
                }
                else {
                    title = "Add New Tade", submit = "Create";
                    og.api.text({module: 'og.blotter.forms.blocks.form_types_tash'}).pipe(function (template){
                        $selector = $(template)
                        .on('change', function (event) {
                            dialog.populate($(event.target).val());
                        }); 
                        dialog.create();
                    });
                }
            };
            dialog.populate = function (suffix, data) {
                var str, inner;
                str = 'og.blotter.forms.' + suffix;
                inner = str.split('.').reduce(function (acc, val) {
                    if (typeof acc[val] === 'undefined') dialog.clear();
                    else return acc[val];
                    }, window);
                if(inner) {
                    form_wrapper = new inner(data);
                    $('.ui-dialog-title').html(form_wrapper.title);
                }
            };
            dialog.create = function () {
                og.common.util.ui.dialog({
                    type: 'input', title: title, width: 530, height: 1200, custom: $selector,
                    buttons: {
                        'Create': function () {form_wrapper.submit(); $(this).dialog('close');},
                        'Cancel': function () {$(this).dialog('close');}
                    }
                });  
            };
            dialog.clear = function () {
                $(form_block).empty();
                $('.ui-dialog-title').html("Add New Trade");
            };
            dialog.load();
        };
    }
}); 