package com.baseras.portal.dto;

import java.time.LocalTime;
import java.util.List;

public class TimetableDtos {

    public record EntryUpsert(
            String sessionId, int dayOfWeek, int period,
            LocalTime startTime, LocalTime endTime,
            String subjectId, String teacherId
    ) {}

    public record SetTimetableRequest(String classId, List<EntryUpsert> entries) {}
}
