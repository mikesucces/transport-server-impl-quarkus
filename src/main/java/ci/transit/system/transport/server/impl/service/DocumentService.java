package ci.transit.system.transport.server.impl.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.transit.system.transport.server.impl.dto.DocumentDto;
import ci.transit.system.transport.server.impl.dto.DocumentRequest;
import ci.transit.system.transport.server.impl.ennumerations.DocumentType;
import ci.transit.system.transport.server.impl.persistence.fleet.VehicleDocument;
import ci.transit.system.transport.server.impl.repository.VehicleDocumentRepository;
import ci.transit.system.transport.server.impl.utilities.ApiException;
import ci.transit.system.transport.server.impl.utilities.FleetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DocumentService {

    @Inject
    VehicleDocumentRepository documentRepository;

    @Inject
    VehicleService vehicleService;

    public List<DocumentDto> findByVehicle(UUID vehicleIdentifier) {
        vehicleService.requireVehicle(vehicleIdentifier);
        return documentRepository.findByVehicle(vehicleIdentifier)
            .stream().map(FleetMapper::toDto).collect(Collectors.toList());
    }

    /** Documents expires ou arrivant a echeance dans les prochains jours. */
    public List<DocumentDto> findExpiring(int days) {
        return documentRepository.findExpiringBefore(LocalDate.now().plusDays(days))
            .stream().map(FleetMapper::toDto).collect(Collectors.toList());
    }

    public DocumentDto create(UUID vehicleIdentifier, DocumentRequest request) {
        VehicleDocument document = new VehicleDocument();
        document.setVehicle(vehicleService.requireVehicle(vehicleIdentifier));
        apply(document, request);
        return FleetMapper.toDto(documentRepository.save(document));
    }

    public DocumentDto update(UUID identifier, DocumentRequest request) {
        VehicleDocument document = documentRepository.findById(identifier);
        if (document == null) {
            throw ApiException.notFound("Document introuvable");
        }
        apply(document, request);
        document.setUpdatedAt(Instant.now());
        return FleetMapper.toDto(documentRepository.save(document));
    }

    public void delete(UUID identifier) {
        if (documentRepository.findById(identifier) == null) {
            throw ApiException.notFound("Document introuvable");
        }
        documentRepository.delete(identifier);
    }

    private void apply(VehicleDocument document, DocumentRequest request) {
        try {
            document.setDocType(DocumentType.valueOf(request.docType.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Type de document invalide : " + request.docType);
        }
        if (request.issuedOn != null && request.expiresOn.isBefore(request.issuedOn)) {
            throw ApiException.badRequest("La date d'expiration precede la date d'emission");
        }
        document.setReference(request.reference);
        document.setIssuer(request.issuer);
        document.setIssuedOn(request.issuedOn);
        document.setExpiresOn(request.expiresOn);
        document.setFileUrl(request.fileUrl);
    }
}
