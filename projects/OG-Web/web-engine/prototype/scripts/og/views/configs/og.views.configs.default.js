/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.configs.default',
    dependencies: [
        'og.api',
        'og.common.util.ui'
    ],
    obj: function () {
        var module = this, ui = og.common.util.ui, api = og.api;
        return function (config) {
            var load_handler = config.handler || $.noop, selector = config.selector, json = config.data.template_data,
                loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
                orig_name = config.data.template_data.name, submit_type, resource_id = json.object_id,
                save_new_handler = config.save_new_handler, save_handler = config.save_handler;
            api.text({module: module.name, handler: function (template, error) {
                var header, content;
                header = '\
                    <header class="OG-header-generic">\
                      <div class="OG-toolbar"></div>\
                      <h1 class="og-js-name">' + json.name + '</h1>\
                    </header>\
                ';
                $('.ui-layout-inner-center .ui-layout-header').html(header);
                json.config_data = is_new ? '' :
                    json.configJSON ? JSON.stringify(json.configJSON, null, 4)
                        : json.configXML ? json.configXML : '';
                content = $.outer($.tmpl(template, json)[0]);
                $(selector).html(content);
                if (deleted || is_new)
                    $(selector + ' .og-js-submit[value=save]').remove(), submit_type = 'save_as_new';
                if (is_new) $(selector + ' .og-js-submit[value=save_as_new]').html('Save');
                $(selector + ' [name=name]').bind('keyup', function (e) {
                    $('.ui-layout-inner-center .og-js-name').text($(e.target).val());
                });
                $(selector + ' .og-js-submit').click(function (e) {submit_type = $(e.target).val();});
                $(selector + ' form').bind('submit', function (e) {
                    var as_new = submit_type === 'save_as_new',
                        name = $(selector + ' [name=name]').val(),
                        data = $(selector + ' .OG-config [data-og=config-data]').val(),
                        rest_options = {
                            id: as_new ? undefined : resource_id,
                            name: name,
                            loading: loading,
                            handler: as_new ? save_new_handler : save_handler
                        };
                    if (!deleted && !is_new && as_new && (orig_name === data.name))
                        return window.alert('Please select a new name.'), false;
                    rest_options[data.charAt(0) === '<' ? 'xml' : 'json'] = data;
                    api.rest.configs.put(rest_options);
                    return false;
                });
                load_handler();
            }});
        };
    }
});