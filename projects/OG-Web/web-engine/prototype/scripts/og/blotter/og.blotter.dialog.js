/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.Dialog',
    dependencies: [],
    obj: function () {   
        return function (config) {
            var constructor = this, $selector, form_block = '.OG-blotter-form-block', form_wrapper, title, submit,
            blotter, error_block = '.OG-blotter-error-block';
            var validation_handler = function (result) {
                if(result.error) {
                    og.common.util.ui.message({css: {position: 'inherit', whiteSpace: 'normal'}, 
                        location: '.OG-blotter-error-block', message: result.message});
                    return;   
                }   
                blotter.close();
            };            
            constructor.load = function () {
                if(config.details) {
                    title = "Edit Trade", submit = "Update";
                    og.api.text({module: 'og.blotter.forms.blocks.form_edit_tash'}).pipe(function (template){
                        var type = config.details.data.security ? 
                            config.details.data.security.type.toLowerCase() : "fungibletrade";
                        $selector = $(template);
                        constructor.create();
                        constructor.populate(type, config);
                    });
                } else {
                    title = "Add New Tade", submit = "Create";
                    og.api.text({module: 'og.blotter.forms.blocks.form_types_tash'}).pipe(function (template){
                        $selector = $(template)
                        .on('change', function (event) {
                            constructor.populate($(event.target).val(), config);
                        }); 
                        constructor.create();
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
            constructor.create = function () {
                var buttons = {
                        'Save': function () {
                            form_wrapper.submit(validation_handler); 
                        },
                        'Save as new' : function () {form_wrapper.submit_new(); $(this).dialog('close');},
                        'Cancel': function () {$(this).dialog('close');}
                    };
                if(!config.details) delete buttons['Save as new'];
                blotter = new og.common.util.ui.dialog({
                    type: 'input', title: title, width: 530, height: 800, custom: $selector,
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