package ci.transit.system.transport.server.impl.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.AttendanceDto;
import ci.transit.system.transport.server.impl.persistence.attendance.Attendance;
import ci.transit.system.transport.server.impl.repository.AttendanceRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.AttendanceMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AttendanceService {

    @Inject
    AttendanceRepository attendanceRepository;

    public List<AttendanceDto> findAll(UUID rotationIdentifier) {
        List<Attendance> attendances = rotationIdentifier == null
            ? attendanceRepository.findAll()
            : attendanceRepository.findByRotation(rotationIdentifier);
        return attendances.stream().map(AttendanceMapper::toDto).collect(Collectors.toList());
    }

    public List<AttendanceDto> findByRotation(UUID rotationIdentifier) {
        return attendanceRepository.findByRotation(rotationIdentifier)
            .stream().map(AttendanceMapper::toDto).collect(Collectors.toList());
    }

    public AttendanceDto findById(UUID identifier) {
        return AttendanceMapper.toDto(requireAttendance(identifier));
    }

    public void delete(UUID identifier) {
        requireAttendance(identifier);
        attendanceRepository.delete(identifier);
    }

    private Attendance requireAttendance(UUID identifier) {
        Attendance attendance = attendanceRepository.findById(identifier);
        if (attendance == null) {
            throw ApiException.notFound("Presence introuvable");
        }
        return attendance;
    }
}
