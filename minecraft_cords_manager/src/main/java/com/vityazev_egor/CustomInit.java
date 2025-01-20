package com.vityazev_egor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.vityazev_egor.Modules.Shared;

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

    private void setUpInitTask(String fxmlName, int interval) {
        this.fxmlName = fxmlName;
        executorService.scheduleWithFixedDelay(
            new InitTask(), 
            interval, 
            interval, 
            java.util.concurrent.TimeUnit.MILLISECONDS
        );
    }

    public CustomInit(String fxmlName) {
        setUpInitTask(fxmlName, 100);
    }
}
