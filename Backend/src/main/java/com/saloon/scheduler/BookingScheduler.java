package com.saloon.scheduler;

import com.saloon.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingService bookingService;

    /**
     * Runs every 5 minutes to auto-cancel expired pending bookings
     */
    @Scheduled(fixedDelay = 300000)
    public void autoExpireBookings() {
        log.debug("Running booking auto-expiry check...");
        bookingService.autoExpireBookings();
    }
}
