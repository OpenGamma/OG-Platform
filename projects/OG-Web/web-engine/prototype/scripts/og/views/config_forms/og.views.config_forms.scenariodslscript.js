/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_forms.scenariodslscript',
    dependencies: ['og.api.rest', 'og.common.util.ui'],
    obj: function () {
        var Form = og.common.util.ui.Form, api = og.api.rest, constructor;
        constructor = function (config) {
            var strip_xml = function (config) {
                var xmlDoc, $xml, output = '';
                if (config.data.template_data.configXML) {
                    xmlDoc = $.parseXML(config.data.template_data.configXML);
                    $xml = $(xmlDoc);
                    output = $xml.find('script').text();
                }
                return output;
            };
            var rebuild_xml = function (groovy) {
                var xml = '<fudgeEnvelope><script type="string">';
                groovy = groovy.replace(/&/g, '&#38;').replace(/</g, '&#60;').replace(/>/g, '&#62;');
                xml += groovy;
                xml += '</script>';
                xml += '<fudgeField0 ordinal="0" type="string">';
                xml += 'com.opengamma.integration.marketdata.manipulator.dsl.ScenarioDslScript';
                xml += '</fudgeField0></fudgeEnvelope>';
                return xml;
            };
            var load_handler = config.handler || $.noop, selector = config.selector, editor,
                master = strip_xml(config), config_type = config.type,
                loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
                orig_name = config.data.template_data.name, resource_id = config.data.template_data.object_id,
                save_new_handler = config.save_new_handler, save_handler = config.save_handler, new_name = '',
                form = new Form({module: 'og.views.forms.config_default_tash', data: {name: null}, selector: selector,
                    extras: {name: orig_name, raw: is_new ? '' : master},
                    processor: function (data) {
                        new_name = data.name;
                    }
                    }), form_id = '#' + form.id;
            var save_resource = function (result) {
                var as_new = result.extras.as_new;
                if (!deleted && !is_new && as_new && (orig_name === new_name)) {
                    return window.alert('Please select a new name.');
                }
                api.configs.put({id: as_new ? void 0 : resource_id, name: new_name,
                    xml: rebuild_xml(editor.getSession().getValue()),
                    type: config_type, loading: loading}).pipe(as_new ? save_new_handler : save_handler);
            };
            form.on('form:load', function () {
                var textarea, id = og.common.id(), header = '\
                <header class="OG-header-generic">\
                  <div class="OG-tools"></div><h1 class="og-js-name">' + orig_name + '</h1>\
                </header>';
                $('.OG-layout-admin-details-center .ui-layout-header').html(header);
                setTimeout(load_handler.partial(form));
                $(selector).css({'overflow': 'hidden'}).find('.OG-config').addClass('og-expand');
                textarea = $('textarea[name=raw]').addClass(id).hide();
                editor = ace.edit('og-js-editor');
                editor.getSession().setMode('ace/mode/groovy');
                editor.getSession().setValue(textarea.val());
                og.common.gadgets.manager.register({
                    alive: function () {
                        return !!$('.' + id).length;
                    },
                    resize: function () {
                        editor.resize();
                    }
                });
            }).on('keyup', form_id + ' [name=name]', function (event) {
                $('.OG-layout-admin-details-center .og-js-name').text($(event.target).val());
            }).on('form:submit', save_resource).dom();
        };
        constructor.is_default = true;
        return constructor;
    }
});