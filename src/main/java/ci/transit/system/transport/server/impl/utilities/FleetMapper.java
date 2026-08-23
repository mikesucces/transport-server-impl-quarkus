package ci.transit.system.transport.server.impl.utilities;

import ci.transit.system.transport.server.impl.dto.DocumentDto;
import ci.transit.system.transport.server.impl.dto.DriverDto;
import ci.transit.system.transport.server.impl.dto.MaintenanceDto;
import ci.transit.system.transport.server.impl.dto.VehicleDto;
import ci.transit.system.transport.server.impl.persistence.fleet.Driver;
import ci.transit.system.transport.server.impl.persistence.fleet.Maintenance;
import ci.transit.system.transport.server.impl.persistence.fleet.Vehicle;
import ci.transit.system.transport.server.impl.persistence.fleet.VehicleDocument;

/**
 * Cette classe assure la conversion des entites en objets de transfert.
 *
 * @author Transit
 *
 */
public final class FleetMapper {

    private FleetMapper() {
    }

    public static VehicleDto toDto(Vehicle entity) {
        VehicleDto dto = new VehicleDto();
        dto.identifier = entity.getUuid();
        dto.plateNumber = entity.getPlateNumber();
        dto.brand = entity.getBrand();
        dto.model = entity.getModel();
        dto.year = entity.getYear();
        dto.capacity = entity.getCapacity();
        dto.mileageKm = entity.getMileageKm();
        dto.status = entity.getStatus().name();
        dto.commissionedAt = entity.getCommissionedAt();
        dto.notes = entity.getNotes();
        return dto;
    }

    public static DocumentDto toDto(VehicleDocument entity) {
        DocumentDto dto = new DocumentDto();
        dto.identifier = entity.getUuid();
        dto.vehicleIdentifier = entity.getVehicle().getUuid();
        dto.plateNumber = entity.getVehicle().getPlateNumber();
        dto.docType = entity.getDocType().name();
        dto.reference = entity.getReference();
        dto.issuer = entity.getIssuer();
        dto.issuedOn = entity.getIssuedOn();
        dto.expiresOn = entity.getExpiresOn();
        dto.daysUntilExpiry = entity.getDaysUntilExpiry();
        dto.expired = entity.isExpired();
        dto.fileUrl = entity.getFileUrl();
        return dto;
    }

    public static MaintenanceDto toDto(Maintenance entity) {
        MaintenanceDto dto = new MaintenanceDto();
        dto.identifier = entity.getUuid();
        dto.vehicleIdentifier = entity.getVehicle().getUuid();
        dto.plateNumber = entity.getVehicle().getPlateNumber();
        dto.maintenanceType = entity.getMaintenanceType().name();
        dto.status = entity.getStatus().name();
        dto.scheduledOn = entity.getScheduledOn();
        dto.performedOn = entity.getPerformedOn();
        dto.mileageKm = entity.getMileageKm();
        dto.nextDueKm = entity.getNextDueKm();
        dto.nextDueOn = entity.getNextDueOn();
        dto.costAmount = entity.getCostAmount();
        dto.garage = entity.getGarage();
        dto.description = entity.getDescription();
        return dto;
    }

    public static DriverDto toDto(Driver entity) {
        DriverDto dto = new DriverDto();
        dto.identifier = entity.getUuid();
        dto.fullName = entity.getFullName();
        dto.phone = entity.getPhone();
        dto.matricule = entity.getMatricule();
        dto.licenseNumber = entity.getLicenseNumber();
        dto.licenseCategory = entity.getLicenseCategory();
        dto.licenseExpiresOn = entity.getLicenseExpiresOn();
        dto.daysUntilLicenseExpiry = entity.getDaysUntilLicenseExpiry();
        dto.remunerationType = entity.getRemunerationType().name();
        dto.monthlySalary = entity.getMonthlySalary();
        dto.tripRate = entity.getTripRate();
        dto.commissionRate = entity.getCommissionRate();
        dto.status = entity.getStatus().name();
        dto.hiredOn = entity.getHiredOn();
        dto.hasAccount = entity.getStaffProfileIdentifier() != null;
        return dto;
    }
}
