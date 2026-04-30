package com.baseras.portal.dto;

import java.util.List;

public class MarksDtos {

    public record EntryUpsert(String studentId, Double marksObtained, Double maxMarks) {}

    public record BulkRequest(
            String classId, String subjectId, String examTypeId, String sessionId,
            List<EntryUpsert> entries
    ) {}

    public record BulkResponse(boolean ok, int count) {}

    public record SubmitRequest(String classId, String subjectId, String examTypeId, String sessionId) {}

    public record SubmitResponse(boolean ok, int count, String publicationStatus) {}
}
