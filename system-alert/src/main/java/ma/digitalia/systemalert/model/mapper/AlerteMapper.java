package ma.digitalia.systemalert.model.mapper;

import ma.digitalia.systemalert.model.dto.AlerteDTO;
import ma.digitalia.systemalert.model.entity.Alerte;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper MapStruct pour la conversion entre Alerte et AlerteDTO
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AlerteMapper {

    /**
     * Convertit une entité Alerte en AlerteDTO
     */
    AlerteDTO toDTO(Alerte alerte);

    /**
     * Convertit un AlerteDTO en entité Alerte
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    Alerte toEntity(AlerteDTO alerteDTO);

    /**
     * Convertit une liste d'entités Alerte en liste d'AlerteDTO
     */
    List<AlerteDTO> toDTOList(List<Alerte> alertes);

    /**
     * Met à jour une entité Alerte avec les données d'un AlerteDTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    void updateEntity(@MappingTarget Alerte alerte, AlerteDTO alerteDTO);
}
