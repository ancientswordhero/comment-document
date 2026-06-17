package com.library.controller;

import com.library.dto.*;
import com.library.service.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/books/{bookId}/notes")
    public ApiResponse<NoteResponse> createNote(@PathVariable Long bookId,
            @Valid @RequestBody NoteRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.createNote(bookId, userId, request));
    }

    @GetMapping("/notes/mine")
    public ApiResponse<PageResult<NoteResponse>> getMyNotes(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.getMyNotes(userId, type, page, size));
    }

    @GetMapping("/books/{bookId}/notes/mine")
    public ApiResponse<List<NoteResponse>> getMyNotesForBook(@PathVariable Long bookId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.getMyNotesForBook(userId, bookId));
    }

    @PutMapping("/notes/{id}")
    public ApiResponse<NoteResponse> updateNote(@PathVariable Long id,
            @Valid @RequestBody NoteRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.updateNote(id, userId, request));
    }

    @DeleteMapping("/notes/{id}")
    public ApiResponse<Void> deleteNote(@PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        noteService.deleteNote(id, userId, false);
        return ApiResponse.success(null);
    }

    @PostMapping("/notes/{id}/publish")
    public ApiResponse<NoteResponse> publishNote(@PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.publishNote(id, userId));
    }

    @PostMapping("/notes/{id}/unpublish")
    public ApiResponse<NoteResponse> unpublishNote(@PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.unpublishNote(id, userId));
    }

    @GetMapping("/notes/public")
    public ApiResponse<PageResult<NoteResponse>> getPublicNotes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.getPublicNotes(userId, page, size));
    }

    @GetMapping("/books/{bookId}/notes/public")
    public ApiResponse<PageResult<NoteResponse>> getPublicNotesForBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.getPublicNotesForBook(bookId, userId, page, size));
    }

    @PostMapping("/notes/{id}/reply")
    public ApiResponse<NoteResponse> createReply(@PathVariable Long id,
            @Valid @RequestBody NoteRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.createReply(id, userId, request));
    }

    @PostMapping("/notes/{id}/like")
    public ApiResponse<Boolean> toggleLike(@PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.toggleLike(id, userId));
    }
}
