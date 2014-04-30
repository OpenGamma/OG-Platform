/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.dialog',
    dependencies: [],
    obj: function () {
        return function (obj) {
            var constructor = this, $obj, css_class, class_name, default_options;
            constructor.close = function () {
                $(css_class).dialog('close');
            };
            if (obj.type === 'confirm') css_class = '.og-js-dialog-confirm';
            if (obj.type === 'input') css_class = '.og-js-dialog-input';
            if (obj.type === 'error') css_class = '.og-js-error-input';
            if (obj.action === 'close') {$(css_class).dialog('close'); return;}
            if (obj.html) { // Just update the dialog contents and return
                $(css_class).html(obj.html);
                return;
            }
            // Return field values
            if (obj.return_field_value) return $('#og-js-dialog-' + obj.return_field_value).val();
            class_name = css_class.replace('.', '');
            default_options = {
                all: {
                    jquery: {
                        resizable: true, 'min-height': 140, modal: true,
                        position: 'center', dialogClass: 'OG-shadow',
                        width: '460', 'min-height': '200',
                        open: function () {
                            if (obj.type === 'input')
                                // Set the focus in the first form element
                                // Doest work without setTimeout!
                                var $first_input = $(css_class).find('[id^="og-js-dialog-"]')[0];
                                if ($first_input) setTimeout(function () {$first_input.focus();}, 1);
                        }
                    }
                },
                confirm: {html: '<div class="' + class_name + '"><p></p></div>'},
                input: {html: '<div class="' + class_name + '"></div>'},
                'error': {
                    html: '<div class="' + class_name + '"></div>',
                    jquery: {buttons: {'Dismiss': function () {$(this).dialog('close');}}}
                }
            };
            // Merge default_options.all with the options for each dialog
            $.each(default_options, function (key) {
                if (key === obj.type) $.extend(true, default_options[key], default_options.all);
            });

            // if the html isn't already in the dom, add it, else clear it
            $(css_class).length === 0 ? $('body').append(default_options[obj.type].html) : $(css_class).html('');

            /**
             * Create error dialog
             */
            if (obj.type === 'error') {
                // Check required data
                if (!obj.message) throw new Error('obj.message is required for an error dialog');
                $obj = $(css_class);
                $obj.attr('title', obj.title || 'Oops, something seems to have gone wrong');
                $obj.html(obj.message);
                $obj.dialog($.extend(true, default_options.error.jquery, obj));
            }
            /**
             * Create confirm dialog
             */
            if (obj.type === 'confirm') {
                // Check required data
                if (!obj.title) throw new Error('obj.title is required for a confirm dialog');
                if (!obj.message) throw new Error('obj.message is required for a confirm dialog');
                $obj = $(css_class);
                $obj.attr('title', obj.title);
                $obj.html(obj.message);
                $obj.dialog($.extend(true, default_options.confirm.jquery, obj));
            }
            /**
             * Create input dialog
             */
            if (obj.type === 'input') {
                // Check required data
                if (!obj.title) throw new Error('obj.title is required for an input dialog');
                $obj = $(css_class);
                $obj.attr('title', obj.title);
                /*
                 * Create basic form via obj.fields
                 */
                if (obj.fields) {
                    $obj.append(obj.fields.reduce(function (acc, val) {
                        return acc + (function () {
                            var str, value;
                            if (val.type === 'input') {
                                if (!val.name) throw new Error('val.name is required for an input field');
                                if (!val.id) throw new Error('val.id is required for an input field');
                                str = '<label for="[PLACEHOLDER]">' + val.name + '</label>'
                                    + '<input type="text" id="[PLACEHOLDER]" name="[PLACEHOLDER]"' +
                                          'value="' + (val.value ? val.value : '') + '" />';
                                str = str.replace(/\[PLACEHOLDER\]/g, 'og-js-dialog-' + val.id);
                            }
                            if (val.type === 'textarea') {
                                if (!val.name) throw new Error('val.name is required for a textarea');
                                if (!val.id) throw new Error('val.id is required for a textarea');
                                str = '<label for="[PLACEHOLDER]">' + val.name + '</label>'
                                    + '<textarea id="[PLACEHOLDER]" name="[PLACEHOLDER]" />';
                                str = str.replace(/\[PLACEHOLDER\]/g, 'og-js-dialog-' + val.id);
                            }
                            if (val.type === 'select') {
                                value = typeof val.value === 'function' ? val.value() : val.value;
                                if (!val.name) throw new Error('val.name is required for a select');
                                if (!val.id) throw new Error('val.id is required for a select');
                                if (!val.options) throw new Error('val.options is required for a select');
                                str = '<label for="[PLACEHOLDER]">' + val.name + '</label>';
                                str += '<select id="[PLACEHOLDER]" name="[PLACEHOLDER]">';
                                $.each(val.options, function (i, v) {
                                    var selected = value === v.value ? ' selected="selected"' : '';
                                    str += '<option value="' + v.value + '"' + selected + '>' + v.name + '</option>';
                                });
                                str += '</select>';
                                str = str.replace(/\[PLACEHOLDER\]/g, 'og-js-dialog-' + val.id);
                            }
                            if (val.type === 'optselect') {
                                value = typeof val.value === 'function' ? val.value() : val.value;
                                if (!val.name) throw new Error('val.name is required for a select');
                                if (!val.id) throw new Error('val.id is required for a select');
                                if (!val.options) throw new Error('val.options is required for a select');
                                str = '<label for="[PLACEHOLDER]">' + val.name + '</label>';
                                str += '<select id="[PLACEHOLDER]" name="[PLACEHOLDER]">';
                                $.each(val.options, function (gi, gv) {
                                    str += '<optgroup label="' + gv.group + '">';
                                    $.each(gv.types, function (i, v) {
                                        var selected = value === v.value ? ' selected="selected"' : '';
                                        str += '<option value="' + v.value + '"' + selected + '>' + v.name +'</option>';
                                    });
                                    str += '</optgroup>';
                                })
                                str += '</select>';
                                str = str.replace(/\[PLACEHOLDER\]/g, 'og-js-dialog-' + val.id);
                            }
                            return '<div>' + str + '</div>';
                        })();
                    }, '<form class="OG-dialog-form">') + '</form>').find('form').unbind().submit(function (e) {
                        e.preventDefault();
                        $obj.dialog('option', 'buttons')['OK']();
                    });
                }
                // Create advanced form via og.common.util.ui.Form
                if (obj.form) {obj.form(css_class);}
                // Create custom content
                if (obj.custom) {$obj.append(obj.custom);}
                $obj.dialog($.extend(true, default_options.input.jquery, obj));
            }
            $obj.parent('.ui-dialog').addClass(class_name + '-container');
        };
    }
});
