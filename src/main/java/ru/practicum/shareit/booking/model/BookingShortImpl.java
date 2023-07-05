package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class BookingShortImpl implements BookingShort {

    private Long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long id;

}
