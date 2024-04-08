package com.vityazev_egor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// класс который используется для релизации собственного способа выполнять код при окрытие формы
public abstract class CustomInit {
    
    public abstract void init();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private String fxmlName;

    private class InitTask implements Runnable {
        @Override
        public void run() {
            String currentName = Shared.getLastMessage();
            if (currentName != null && currentName.equals(fxmlName)) {
                init();
                Shared.checkLastMessage();
            }
        }
    }

    public void setUpInitTask(String fxmlName, int interval) {
        this.fxmlName = fxmlName;
        executorService.scheduleAtFixedRate(new InitTask(), interval, interval, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
