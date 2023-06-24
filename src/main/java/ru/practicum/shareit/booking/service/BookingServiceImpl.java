package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.exception.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserService userService;
    private final ItemService itemService;
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public BookingDtoResponse create(BookingDtoRequest bookingDtoRequest, Long userId) {
        User user = userService.checkUserExistentAndGet(userId);
        Long itemId = bookingDtoRequest.getItemId();
        itemService.checkItemExistentAndGet(itemId);
        Item item = itemService.checkItemAvailabilityAndGet(itemId);
        if (item.getOwner().getId().equals(userId)) {
            throw new BookYourOwnItemException("Cannot book your own item");
        }
        Booking booking = bookingMapper.toBooking(bookingDtoRequest, user, item);
        booking.setStatus(Status.WAITING);
        log.info("Booking created: itemId={}, userId={}", itemId, userId);
        return bookingMapper.toBookingDtoResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoResponse approve(Long userId, Long bookingId, boolean isApproved) {
        Booking booking = checkBookingExistentAndGet(bookingId);
        isOwner(booking, userId);
        if (booking.getStatus().equals(Status.APPROVED) || booking.getStatus().equals(Status.REJECTED)) {
            throw new ChangeBookingStatusException("Cannot change approved or rejected status");
        }
        booking.setStatus(isApproved ? Status.APPROVED : Status.REJECTED);
        log.info("User id={} set booking id={} status equal to {}", userId, booking, booking.getStatus().name());
        return bookingMapper.toBookingDtoResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDtoResponse getById(Long bookingId, Long userId) {
        Booking booking = checkBookingExistentAndGet(bookingId);
        isBookerOrOwner(booking, userId);
        log.info("User id={} requested information about booking id={}", userId, booking);
        return bookingMapper.toBookingDtoResponse(booking);
    }

    @Override
    public List<BookingDtoResponse> getByBookerId(Long bookerId, String state) {
        userService.checkUserExistentAndGet(bookerId);
        List<BookingDtoResponse> bookings = new ArrayList<>();
        switch (state) {
            case "ALL":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllByBookerIdOrderByStartDesc(bookerId));
                break;
            case "FUTURE":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findFutureBookingsByBooker(bookerId));
                break;
            case "PAST":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findPastBookingsByBooker(bookerId));
                break;
            case "CURRENT":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findCurrentBookingsByBooker(bookerId));
                break;
            case "WAITING":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findWaitingBookingsByBooker(bookerId));
                break;
            case "REJECTED":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findRejectedBookingsByBooker(bookerId));
                break;
            default:
                throw new UnsupportedStatusException("Unknown state: " + state);
        }
        log.info("[booker] User id={} requested information about his bookings with state={}", bookerId, state);
        return bookings;
    }

    @Override
    public List<BookingDtoResponse> getByOwnerId(Long ownerId, String state) {
        userService.checkUserExistentAndGet(ownerId);
        List<BookingDtoResponse> bookings = new ArrayList<>();
        switch (state) {
            case "ALL":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllBookingsByOwner(ownerId));
                break;
            case "FUTURE":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findFutureBookingsByOwner(ownerId));
                break;
            case "PAST":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findPastBookingsByOwner(ownerId));
                break;
            case "CURRENT":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findCurrentBookingsByOwner(ownerId));
                break;
            case "WAITING":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findWaitingBookingsByOwner(ownerId));
                break;
            case "REJECTED":
                bookings = bookingMapper.toBookingDtoResponse(bookingRepository
                        .findRejectedBookingsByOwner(ownerId));
                break;
            default:
                throw new UnsupportedStatusException("Unknown state: " + state);
        }

        log.info("[owner] User id={} requested information about his bookings with state={}", ownerId, state);
        return bookings;
    }

    @Override
    public Booking checkBookingExistentAndGet(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Booking.class, "Id:" + id));
    }


    private void isBookerOrOwner(Booking booking, Long userId) {
        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        Long itemId = booking.getItem().getId();
        if (bookerId.equals(userId) && ownerId.equals(userId)) {
            log.info("User id={} is not booker or owner of item id={}", userId, itemId);
            throw new IncorrectOwnerException(String.format("User id=%s is not booker or owner of item id=%s",
                    userId, itemId));
        }
    }

    private void isOwner(Booking booking, Long userId) {
        Long ownerId = booking.getItem().getOwner().getId();
        Long itemId = booking.getItem().getId();
        if (ownerId.equals(userId)) {
            log.info("User id={} is not owner of item id={}", userId, itemId);
            throw new IncorrectOwnerException(String.format("User id=%s is not owner of item id=%s", userId, itemId));
        }
    }

}
