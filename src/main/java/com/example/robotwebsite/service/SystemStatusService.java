package com.example.robotwebsite.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SystemStatusService {
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);

    public boolean isUpdating() {
        return isUpdating.get();
    }

    public void setUpdating(boolean updating) {
        isUpdating.set(updating);
    }
}
