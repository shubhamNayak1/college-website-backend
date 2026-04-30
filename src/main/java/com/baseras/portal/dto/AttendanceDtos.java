package com.baseras.portal.dto;

import java.time.LocalDate;
import java.util.List;

public class AttendanceDtos {

    public record EntryUpsert(String studentId, String status, String remarks) {}

    public record BulkRequest(String classId, LocalDate date, List<EntryUpsert> entries) {}
}
