package com.baseras.portal.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class NoticeDtos {

    public record CreateRequest(
            @NotBlank String title,
            @NotBlank String body,
            @NotBlank String audience,
            String classId,
            List<String> attachmentUrls
    ) {}

    public record UpdateRequest(
            String title, String body, String audience, String classId,
            List<String> attachmentUrls
    ) {}
}
