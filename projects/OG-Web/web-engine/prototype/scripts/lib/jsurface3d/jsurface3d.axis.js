/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.JSurface3D) throw new Error('JSurface3D.Axis requires JSurface3D');
    /**
     * Remove every nth item in Array keeping the first and last,
     * also specifically remove the second last (as we want to keep the last)
     * @private
     * @param {Array} arr
     * @param {Number} nth
     * @returns {Array}
     */
    var thin = function (arr, nth) {
        if (!nth || nth === 1) return arr;
        var len = arr.length;
        return arr.filter(function (val, i) {
            return ((i === 0) || !(i % nth) || (i === (len -1))) || (i === (len -2)) && false
        });
    };
    /**
     * Creates an Axis with labels for the bottom grid
     * @name JSurface3D.Axis
     * @namespace JSurface3D.Axis
     * @param {Object} config
     * <pre>
     *     axis     {String} x or z
     *     spacing  {Array}  Array of numbers adjusted to fit units of mesh
     *     labels   {Array}  Array of labels
     *     label    {String} Axis label
     * </pre>
     * @param {Object} js3d A JSurface3D instance
     * @private
     * @return {THREE.Object3D}
     */
    window.JSurface3D.Axis = function (config, js3d) {
        var axis = config.axis, spacing = js3d['adjusted_' + axis + 's'],
            values = js3d.data[config.axis + 's_labels'] || (axis === 'y' ? js3d[axis + 's'] : js3d.data[axis + 's']),
            lbl = axis === 'y' ? null : js3d.data[axis + 's_label'],
            mesh = new THREE.Object3D(), i, nth = Math.ceil(spacing.length / 6),
            lbl_arr = thin(values, nth), pos_arr = thin(spacing, nth), settings = js3d.settings,
            axis_len = settings['surface_' + axis];
        mesh.name = axis + ' Axis';
        (function () { // axis values
            var value, n;
            for (i = 0; i < lbl_arr.length; i++) {
                n = lbl_arr[i];
                n = n % 1 === 0 || isNaN(n % 1) ? n : (+n).toFixed(settings.precision_lbl).replace(/0+$/, '');
                value = js3d.text3d(n+'', settings.font_color);
                value.scale.set(0.1, 0.1, 0.1);
                if (config.axis === 'y') {
                    value.position.x = pos_arr[i] - 6;
                    value.position.z = config.right
                        ? -(THREE.FontUtils.drawText(n).offset * 0.2) + 18
                        : 2;
                    value.rotation.z = -Math.PI * 0.5;
                    value.rotation.x = -Math.PI * 0.5;
                } else {
                    value.rotation.x = -Math.PI * 0.5;
                    value.position.x = pos_arr[i] - ((THREE.FontUtils.drawText(n).offset * 0.2) / 2);
                    value.position.y = 0.1;
                    value.position.z = 12;
                }
                value.matrixAutoUpdate = false;
                value.updateMatrix();
                js3d.buffers.load.add(value);
                mesh.add(value);
            }
        }());
        (function () { // axis label
            if (!lbl) return;
            var label = js3d.text3d(lbl, settings.font_color_axis_labels, true, true);
            label.scale.set(0.2, 0.2, 0.1);
            label.rotation.x = -Math.PI * 0.5;
            label.position.x = -(axis_len / 2) -3;
            label.position.y = 1;
            label.position.z = 22;
            label.matrixAutoUpdate = false;
            label.updateMatrix();
            js3d.buffers.load.add(label);
            mesh.add(label);
        }());
        (function () { // axis ticks
            var canvas = document.createElement('canvas'),
                ctx = canvas.getContext('2d'),
                plane = new THREE.PlaneGeometry(axis_len, 5, 0, 0),
                axis = new THREE.Mesh(plane, js3d.matlib.texture(new THREE.Texture(canvas))),
                tick_stop_pos =  settings.tick_length + 0.5,
                labels = thin(spacing.map(function (val) {
                    // if not y axis offset half. y planes start at 0, x and z start at minus half width
                    var offset = config.axis === 'y' ? 0 : axis_len / 2;
                    return (val + offset) * (settings.texture_size / axis_len);
                }), nth);
            plane.applyMatrix(new THREE.Matrix4().makeRotationX(-Math.PI / 2));
            canvas.width = settings.texture_size;
            canvas.height = 32;
            ctx.beginPath();
            ctx.lineWidth = 2;
            for (i = 0; i < labels.length; i++)
                ctx.moveTo(labels[i] + 0.5, tick_stop_pos), ctx.lineTo(labels[i] + 0.5, 0);
            ctx.moveTo(0.5, tick_stop_pos);
            ctx.lineTo(0.5, 0.5);
            ctx.lineTo(canvas.width - 0.5, 0.5);
            ctx.lineTo(canvas.width - 0.5, tick_stop_pos);
            ctx.stroke();
            axis.material.map.needsUpdate = true;
            axis.doubleSided = true;
            if (config.axis === 'y') {
                if (config.right) axis.rotation.x = Math.PI, axis.position.z = 20;
                axis.position.x = (axis_len / 2) - 4;
            } else {
                axis.position.z = 5;
            }
            axis.matrixAutoUpdate = false;
            axis.updateMatrix();
            js3d.buffers.surface.add(axis);
            mesh.add(axis);
        }());
        return mesh;
    };
})();