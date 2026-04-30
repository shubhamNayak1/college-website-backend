package com.baseras.portal.controller;

import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.common.IdGen;
import com.baseras.portal.dto.AuthDtos.OkResponse;
import com.baseras.portal.dto.TimetableDtos.*;
import com.baseras.portal.entity.TimetableEntry;
import com.baseras.portal.entity.User;
import com.baseras.portal.repository.TimetableEntryRepository;
import com.baseras.portal.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/timetable")
public class TimetableController {

    private final TimetableEntryRepository tt;
    private final CurrentUser current;
    private final AuditService audit;

    public TimetableController(TimetableEntryRepository tt, CurrentUser current, AuditService audit) {
        this.tt = tt; this.current = current; this.audit = audit;
    }

    @GetMapping
    public List<TimetableEntry> list(@RequestParam(required = false) String classId,
                                     @RequestParam(defaultValue = "sess_2025_26") String sessionId) {
        if (classId == null) return tt.findAll();
        return tt.findByClassIdAndSessionIdOrderByDayOfWeekAscPeriodAsc(classId, sessionId);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL')")
    @Transactional
    public OkResponse set(@RequestBody SetTimetableRequest req) {
        User u = current.require();
        String sessionId = req.entries().isEmpty() ? "sess_2025_26" : req.entries().get(0).sessionId();
        tt.deleteByClassIdAndSessionId(req.classId(), sessionId);
        for (EntryUpsert e : req.entries()) {
            tt.save(TimetableEntry.builder().id(IdGen.of("tt"))
                    .classId(req.classId()).sessionId(e.sessionId())
                    .dayOfWeek(e.dayOfWeek()).period(e.period())
                    .startTime(e.startTime()).endTime(e.endTime())
                    .subjectId(e.subjectId()).teacherId(e.teacherId())
                    .build());
        }
        audit.log(u.getId(), "UPDATE_TIMETABLE", "Timetable", req.classId(),
                Map.of("entries", req.entries().size()));
        return OkResponse.OK;
    }
}
