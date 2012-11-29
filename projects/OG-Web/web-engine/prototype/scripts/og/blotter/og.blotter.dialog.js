/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.Dialog',
    dependencies: [],
    obj: function () {   
        return function () {
            var dialog = this, form_block = '.OG-blotter-form-block', form;
            dialog.load = function () {
                og.api.text({module: 'og.blotter.forms.blocks.form_types_tash'}).pipe(function (template){
                    var $selector = $(template)
                    .on('change', function (event) {
                        var str, inner;
                        str = 'og.blotter.forms.' + $(event.target).val();
                        inner = str.split('.').reduce(function (acc, val) {
                            if (typeof acc[val] === 'undefined') dialog.clear();
                            else return acc[val];
                            }, window);
                        if(inner) {
                            form = new inner();
                            $('.ui-dialog-title').html(form.title);
                        }
                    }); 
                    og.common.util.ui.dialog({
                        type: 'input', title: 'Add New Trade', width: 530, height: 700, custom: $selector,
                        buttons: {
                            'Create': function () {$(this).dialog('close');},
                            'Cancel': function () {$(this).dialog('close');}
                        }
                    });  
                });
            };
            dialog.clear = function () {
                $(form_block).empty();
                $('ui-dialog-titlebar').html("Add New Trade");
            };
            dialog.load();
        };
    }
});