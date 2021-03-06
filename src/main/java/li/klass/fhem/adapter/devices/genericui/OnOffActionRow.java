/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Map;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

public class OnOffActionRow<T extends ToggleableDevice> {

    public static final String HOLDER_KEY = "OnOffActionRow";
    public static final int LAYOUT_DETAIL = R.layout.device_detail_onoffbuttonrow;
    public static final int LAYOUT_OVERVIEW = R.layout.device_overview_onoffbuttonrow;
    private TableRow tableRow;
    private TextView descriptionView;
    private Button onButton;
    private Button offButton;


    public OnOffActionRow(LayoutInflater inflater, int layoutId) {
        tableRow = (TableRow) inflater.inflate(layoutId, null);
        descriptionView = ((TextView) tableRow.findViewById(R.id.description));
        onButton = (Button) tableRow.findViewById(R.id.onButton);
        offButton = (Button) tableRow.findViewById(R.id.offButton);
    }

    @SuppressWarnings("unchecked")
    public void fillWith(final T device, Context context) {
        descriptionView.setText(device.getAliasOrName());
        Map<String, String> eventMap = device.getEventMap();

        String onStateName = device.getOnStateName();
        onButton.setOnClickListener(createListener(context, device, onStateName));
        if (eventMap.containsKey(onStateName)) {
            onButton.setText(eventMap.get(onStateName));
        } else {
            onButton.setText(context.getString(R.string.on));
        }


        String offStateName = device.getOffStateName();
        offButton.setOnClickListener(createListener(context, device, offStateName));
        if (eventMap.containsKey(offStateName)) {
            offButton.setText(eventMap.get(offStateName));
        } else {
            offButton.setText(context.getString(R.string.off));
        }

        if (device.isOnRespectingInvertHook()) {
            onButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.theme_toggle_on_normal));
            offButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.theme_toggle_default_normal));
        } else {
            onButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.theme_toggle_default_normal));
            offButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.theme_toggle_off_normal));
        }

        switch (device.getButtonHookType()) {
            case ON_DEVICE:
                offButton.setVisibility(View.GONE);
                onButton.setVisibility(View.VISIBLE);
                break;
            case OFF_DEVICE:
                onButton.setVisibility(View.GONE);
                offButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    public TableRow getView() {
        return tableRow;
    }

    private ToggleButton.OnClickListener createListener(final Context context, final T device, final String targetState) {
        return new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(context, device, targetState);
            }
        };
    }

    public void onButtonClick(final Context context, T device, String targetState) {
        Intent intent = new Intent(Actions.DEVICE_SET_STATE);
        intent.setClass(context, DeviceIntentService.class);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, targetState);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context));

        context.startService(intent);
    }
}
