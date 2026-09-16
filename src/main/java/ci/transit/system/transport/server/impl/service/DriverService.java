package ci.transit.system.transport.server.impl.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.DriverDto;
import ci.transit.system.transport.server.impl.dto.DriverRequest;
import ci.transit.system.transport.server.impl.ennumerations.DriverStatus;
import ci.transit.system.transport.server.impl.ennumerations.RemunerationType;
import ci.transit.system.transport.server.impl.persistence.fleet.Driver;
import ci.transit.system.transport.server.impl.repository.DriverRepository;
import ci.transit.system.transport.server.impl.repository.StaffProfileRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.FleetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DriverService {

    @Inject
    DriverRepository driverRepository;

    @Inject
    StaffProfileRepository staffProfileRepository;

    public List<DriverDto> findAll(String status) {
        List<Driver> drivers = (status == null || status.isBlank())
            ? driverRepository.findAll()
            : driverRepository.findByStatus(parseStatus(status));
        return drivers.stream().map(FleetMapper::toDto).collect(Collectors.toList());
    }

    public List<DriverDto> findAvailable() {
        return driverRepository.findByStatus(DriverStatus.DISPONIBLE)
            .stream().map(FleetMapper::toDto).collect(Collectors.toList());
    }

    public List<DriverDto> findWithExpiringLicense(int days) {
        return driverRepository.findWithLicenseExpiringBefore(LocalDate.now().plusDays(days))
            .stream().map(FleetMapper::toDto).collect(Collectors.toList());
    }

    public BigDecimal computeMonthlyPayroll() {
        return driverRepository.sumMonthlySalary();
    }

    public DriverDto findById(UUID identifier) {
        return FleetMapper.toDto(requireDriver(identifier));
    }

    public DriverDto create(DriverRequest request) {
        if (driverRepository.findByPhone(request.phone.trim()).isPresent()) {
            throw ApiException.badRequest("Ce numero de telephone est deja utilise");
        }
        Driver driver = new Driver();
        apply(driver, request);
        return FleetMapper.toDto(driverRepository.save(driver));
    }

    public DriverDto update(UUID identifier, DriverRequest request) {
        Driver driver = requireDriver(identifier);
        driverRepository.findByPhone(request.phone.trim()).ifPresent(other -> {
            if (!other.getUuid().equals(identifier)) {
                throw ApiException.badRequest("Ce numero de telephone est deja utilise");
            }
        });
        apply(driver, request);
        driver.setUpdatedAt(Instant.now());
        return FleetMapper.toDto(driverRepository.save(driver));
    }

    public DriverDto updateStatus(UUID identifier, String status) {
        Driver driver = requireDriver(identifier);
        driver.setStatus(parseStatus(status));
        driver.setUpdatedAt(Instant.now());
        return FleetMapper.toDto(driverRepository.save(driver));
    }

    public void delete(UUID identifier) {
        Driver driver = requireDriver(identifier);
        if (driver.getStatus() == DriverStatus.EN_ROTATION) {
            throw ApiException.badRequest("Impossible de supprimer un chauffeur en rotation");
        }
        driverRepository.delete(identifier);
    }

    private Driver requireDriver(UUID identifier) {
        Driver driver = driverRepository.findById(identifier);
        if (driver == null) {
            throw ApiException.notFound("Chauffeur introuvable");
        }
        return driver;
    }

    private void apply(Driver driver, DriverRequest request) {
        driver.setFullName(request.fullName.trim());
        driver.setPhone(request.phone.trim());
        driver.setMatricule(request.matricule);
        if (request.staffProfileId != null && staffProfileRepository.findById(request.staffProfileId) == null) {
            throw ApiException.notFound("Profil personnel introuvable");
        }
        driver.setStaffProfileIdentifier(request.staffProfileId);
        driver.setLicenseNumber(request.licenseNumber.trim());
        if (request.licenseCategory != null && !request.licenseCategory.isBlank()) {
            driver.setLicenseCategory(request.licenseCategory.trim().toUpperCase());
        }
        driver.setLicenseExpiresOn(request.licenseExpiresOn);

        if (request.remunerationType != null && !request.remunerationType.isBlank()) {
            try {
                driver.setRemunerationType(
                    RemunerationType.valueOf(request.remunerationType.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw ApiException.badRequest(
                    "Mode de remuneration invalide : " + request.remunerationType);
            }
        }
        driver.setMonthlySalary(request.monthlySalary);
        driver.setTripRate(request.tripRate);
        driver.setCommissionRate(request.commissionRate);

        // Le montant renseigne doit correspondre au mode de remuneration choisi.
        switch (driver.getRemunerationType()) {
            case FIXE -> require(driver.getMonthlySalary(),
                "Un salaire mensuel est requis pour une remuneration fixe");
            case PAR_TRAJET -> require(driver.getTripRate(),
                "Un tarif par trajet est requis pour ce mode de remuneration");
            case COMMISSION -> require(driver.getCommissionRate(),
                "Un taux de commission est requis pour ce mode de remuneration");
        }

        if (request.status != null && !request.status.isBlank()) {
            driver.setStatus(parseStatus(request.status));
        }
        driver.setHiredOn(request.hiredOn);
    }

    private void require(BigDecimal value, String message) {
        if (value == null) {
            throw ApiException.badRequest(message);
        }
    }

    private DriverStatus parseStatus(String value) {
        try {
            return DriverStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Statut de chauffeur invalide : " + value);
        }
    }
}
