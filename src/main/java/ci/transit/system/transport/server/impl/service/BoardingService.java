package ci.transit.system.transport.server.impl.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ci.transit.system.transport.server.impl.dto.BoardingResultDto;
import ci.transit.system.transport.server.impl.dto.BoardingVerifyRequest;
import ci.transit.system.transport.server.impl.ennumerations.PassengerStatus;
import ci.transit.system.transport.server.impl.persistence.attendance.Attendance;
import ci.transit.system.transport.server.impl.persistence.identity.AccessCode;
import ci.transit.system.transport.server.impl.persistence.identity.LoginAttempt;
import ci.transit.system.transport.server.impl.persistence.identity.Passenger;
import ci.transit.system.transport.server.impl.persistence.rotation.Rotation;
import ci.transit.system.transport.server.impl.repository.AccessCodeRepository;
import ci.transit.system.transport.server.impl.repository.AttendanceRepository;
import ci.transit.system.transport.server.impl.repository.LoginAttemptRepository;
import ci.transit.system.transport.server.impl.repository.PassengerRepository;
import ci.transit.system.transport.server.impl.repository.RotationRepository;
import ci.transit.system.transport.server.impl.repository.SubscriptionRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.AttendanceMapper;
import ci.transit.system.transport.server.impl.utilities.CodeHasher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Cette classe verifie le code d'acces d'un usager a l'embarquement et
 * enregistre le resultat (tentative generique + presence validee en cas de succes).
 *
 * @author Transit
 *
 */
@ApplicationScoped
public class BoardingService {

    @Inject
    AccessCodeRepository accessCodeRepository;

    @Inject
    RotationRepository rotationRepository;

    @Inject
    PassengerRepository passengerRepository;

    @Inject
    LoginAttemptRepository loginAttemptRepository;

    @Inject
    AttendanceRepository attendanceRepository;

    @Inject
    SubscriptionRepository subscriptionRepository;

    public BoardingResultDto verify(BoardingVerifyRequest request) {
        Rotation rotation = rotationRepository.findById(request.rotationId);
        if (rotation == null) {
            throw ApiException.notFound("Rotation introuvable");
        }

        String submittedHash = CodeHasher.hash(request.code);
        AccessCode matchedCode = findMatchingActiveCode(rotation, submittedHash);
        Optional<Passenger> passenger = passengerRepository.findByPhone(request.phone.trim());
        boolean passengerActive = passenger.isPresent() && passenger.get().getStatus() == PassengerStatus.ACTIVE;
        boolean hasActiveSubscription = passenger.isPresent()
            && subscriptionRepository.findActiveForPassenger(passenger.get().getUuid(), LocalDate.now()).isPresent();
        boolean success = matchedCode != null && passengerActive && hasActiveSubscription;

        LoginAttempt attempt = new LoginAttempt();
        attempt.setIdentifier(request.phone.trim());
        attempt.setSuccess(success);
        passenger.ifPresent(attempt::setPassenger);
        LoginAttempt savedAttempt = loginAttemptRepository.save(attempt);

        BoardingResultDto result = new BoardingResultDto();
        result.loginAttemptId = savedAttempt.getId();

        if (!success) {
            result.success = false;
            if (matchedCode == null) {
                result.message = "Code invalide ou expire";
            } else if (!passengerActive) {
                result.message = "Usager introuvable ou non actif";
            } else {
                result.message = "Abonnement expire ou inexistant";
            }
            return result;
        }

        Attendance attendance = new Attendance();
        attendance.setRotation(rotation);
        attendance.setPassenger(passenger.get());
        attendance.setAccessCode(matchedCode);
        attendance.setControllerId(request.controllerId);
        Attendance savedAttendance = attendanceRepository.save(attendance);

        result.success = true;
        result.message = "Embarquement valide";
        result.attendance = AttendanceMapper.toDto(savedAttendance);
        return result;
    }

    private AccessCode findMatchingActiveCode(Rotation rotation, String submittedHash) {
        List<AccessCode> activeCodes = accessCodeRepository.findActive(
            rotation.getUuid(), rotation.getVehicle().getUuid());
        return activeCodes.stream()
            .filter(code -> code.getCodeHash().equals(submittedHash))
            .findFirst()
            .orElse(null);
    }
}
