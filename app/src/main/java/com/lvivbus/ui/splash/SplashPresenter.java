package com.lvivbus.ui.splash;

import android.content.Intent;
import com.lvivbus.model.db.BusDAO;
import com.lvivbus.model.event.NetworkChangedEvent;
import com.lvivbus.model.http.BusAPI;
import com.lvivbus.model.http.Converter;
import com.lvivbus.model.http.Internet;
import com.lvivbus.ui.R;
import com.lvivbus.ui.data.Bus;
import com.lvivbus.ui.map.MapActivity;
import com.lvivbus.utils.L;
import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SplashPresenter {

    private SplashActivity activity;
    private EventBus eventBus = EventBus.getDefault();

    public void onAttachActivity(SplashActivity mapActivity) {
        this.activity = mapActivity;
        eventBus.register(activity);

        if (BusDAO.getAllCount() == 0) {
            loadData();
        } else {
            launchMapActivity();
            activity.finish();
        }
    }

    public void onDetachActivity() {
        eventBus.unregister(activity);
        activity = null;
    }

    public void onEvent(NetworkChangedEvent event) {
        if (event.isConnected()) {
            loadData();
        }
    }

    private void loadData() {
        if (Internet.isOn(activity.getApplicationContext())) {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    List<Bus> busList = Converter.toBusList(BusAPI.getBusList());
                    BusDAO.save(busList);
                    L.v(String.format("Buses saved: %d", busList.size()));
                    onLoadingComplete();
                }
            });
        } else {
            activity.showMessage(activity.getString(R.string.no_connection));
        }
    }

    private void onLoadingComplete() {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    launchMapActivity();
                    activity.finish();
                }
            });
        }
    }

    private void launchMapActivity() {
        Intent intent = new Intent(activity, MapActivity.class);
        activity.startActivity(intent);
    }

}
