package ma.digitalia.gestionconges.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionconges.entities.TypeConge;
import ma.digitalia.gestionconges.repositories.TypeCongeRpository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TypeCongeServiceImpl implements TypeCongeService {

    TypeCongeRpository typeCongeRpository;

    public TypeCongeServiceImpl(TypeCongeRpository typeCongeRpository) {
        this.typeCongeRpository = typeCongeRpository;
    }

    @Override
    public TypeConge findById(Long id) {
        if (typeCongeRpository.existsById(id)) {
            return typeCongeRpository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Type de congés not found with id: " + id));
        } else {
            throw new EntityNotFoundException("Employe not found with id: " + id);
        }
    }

    @Override
    public List<Map<Long, String>> getTypeConges() {
        List<Object[]> results = typeCongeRpository.getIdAndNomAsArray();

        return results.stream()
                .map(row -> {
                    Map<Long, String> map = new HashMap<>();
                    map.put((Long) row[0], (String) row[1]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TypeConge> getAllTypeConges() {
        log.info("getAllTypeConges");
        return typeCongeRpository.findAll();
    }

    @Override
    public void save(TypeConge typeConge) {
        typeCongeRpository.save(typeConge);
    }

    @Override
    public void deleteById(Long id) {
        if(typeCongeRpository.existsById(id)) {
            typeCongeRpository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Type de congés not found with id: " + id);
        }
    }

}
