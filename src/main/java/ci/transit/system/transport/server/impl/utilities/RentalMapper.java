package ci.transit.system.transport.server.impl.utilities;

import ci.transit.system.transport.server.impl.dto.RentalDto;
import ci.transit.system.transport.server.impl.dto.RentalPaymentDto;
import ci.transit.system.transport.server.impl.persistence.rental.Rental;
import ci.transit.system.transport.server.impl.persistence.rental.RentalPayment;

/**
 * Cette classe convertit les entites du module Location en DTOs.
 *
 * @author Transit
 *
 */
public final class RentalMapper {

    private RentalMapper() {
    }

    public static RentalDto toDto(Rental entity) {
        RentalDto dto = new RentalDto();
        dto.identifier = entity.getUuid();
        dto.passengerIdentifier = entity.getPassenger().getUuid();
        dto.passengerFullName = entity.getPassenger().getFullName();
        dto.vehicleIdentifier = entity.getVehicle().getUuid();
        dto.plateNumber = entity.getVehicle().getPlateNumber();
        dto.status = entity.getStatus().name();
        dto.startDate = entity.getStartDate();
        dto.endDate = entity.getEndDate();
        dto.actualReturnDate = entity.getActualReturnDate();
        dto.startMileageKm = entity.getStartMileageKm();
        dto.endMileageKm = entity.getEndMileageKm();
        dto.totalAmount = entity.getTotalAmount();
        dto.depositAmount = entity.getDepositAmount();
        dto.notes = entity.getNotes();
        return dto;
    }

    public static RentalPaymentDto toDto(RentalPayment entity) {
        RentalPaymentDto dto = new RentalPaymentDto();
        dto.identifier = entity.getUuid();
        dto.rentalIdentifier = entity.getRental().getUuid();
        dto.paymentAccountId = entity.getPaymentAccount() != null ? entity.getPaymentAccount().getUuid() : null;
        dto.amount = entity.getAmount();
        dto.method = entity.getMethod().name();
        dto.collectedBy = entity.getCollectedBy();
        dto.paidAt = entity.getPaidAt();
        return dto;
    }
}
