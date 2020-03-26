package me.confuser.banmanager.webenhancer.runnables;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.j256.ormlite.dao.CloseableIterator;

import lombok.NonNull;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.runnables.BmRunnable;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.webenhancer.data.PlayerPinData;
import me.confuser.banmanager.webenhancer.storage.PlayerPinStorage;

public class ExpiresSync extends BmRunnable {
    @NonNull private final PlayerPinStorage pinStorage;

    public ExpiresSync(final PlayerPinStorage pinStorage) {
        super(BanManagerPlugin.getInstance(), "pinCheck");
        this.pinStorage = pinStorage;
    }

    @Override public void run() {
        long now = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + DateUtils.getTimeDiff();

        CloseableIterator<PlayerPinData> pins = null;
        try {
            pins = pinStorage.queryBuilder()
                    .where().le("expires", now)
                    .iterator();

            while (pins.hasNext()) {
                PlayerPinData pin = pins.next();

                pinStorage.delete(pin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (pins != null)
                pins.closeQuietly();
        }
    }
}
