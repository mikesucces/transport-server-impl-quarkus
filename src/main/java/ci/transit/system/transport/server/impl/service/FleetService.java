package ci.transit.system.transport.server.impl.service;

import ci.transit.system.transport.server.impl.dto.FleetAlertsDto;
import ci.transit.system.transport.server.impl.ennumerations.VehicleStatus;
import ci.transit.system.transport.server.impl.repository.VehicleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Cette classe fournit la synthese de la flotte affichee
 * sur le tableau de bord.
 *
 * @author Transit
 *
 */
@ApplicationScoped
public class FleetService {

    @Inject
    VehicleRepository vehicleRepository;

    @Inject
    DocumentService documentService;

    @Inject
    MaintenanceService maintenanceService;

    @Inject
    DriverService driverService;

    public FleetAlertsDto computeAlerts(int days) {
        FleetAlertsDto alerts = new FleetAlertsDto();
        alerts.expiringDocuments = documentService.findExpiring(days);
        alerts.expiringLicenses = driverService.findWithExpiringLicense(days);
        alerts.upcomingMaintenances = maintenanceService.findUpcoming(days);
        alerts.vehiclesTotal = vehicleRepository.count();
        alerts.vehiclesInService = vehicleRepository.countByStatus(VehicleStatus.EN_SERVICE);
        alerts.vehiclesInMaintenance = vehicleRepository.countByStatus(VehicleStatus.ENTRETIEN);
        alerts.monthlyPayroll = driverService.computeMonthlyPayroll();
        return alerts;
    }
}
