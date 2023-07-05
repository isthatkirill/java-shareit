package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDtoResponse create(@RequestBody @Valid ItemRequestDto itemRequestDto,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.create(itemRequestDto, userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoResponse> getAll(@RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                               @RequestParam(defaultValue = "10") @Positive Integer size,
                                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAll(from, size, userId);
    }

    @GetMapping
    public List<ItemRequestDtoResponse> getOwn(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getOwn(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoResponse getById(@PathVariable Long requestId,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getById(requestId, userId);
    }

}
