package com.baseras.portal.repository;

import com.baseras.portal.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, String> {
    List<TimetableEntry> findByClassIdAndSessionIdOrderByDayOfWeekAscPeriodAsc(String classId, String sessionId);
    List<TimetableEntry> findByClassIdAndSessionIdAndDayOfWeek(String classId, String sessionId, int dayOfWeek);
    void deleteByClassIdAndSessionId(String classId, String sessionId);
}
