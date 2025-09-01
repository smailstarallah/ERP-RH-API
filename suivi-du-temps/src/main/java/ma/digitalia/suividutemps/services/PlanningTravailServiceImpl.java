package ma.digitalia.suividutemps.services;

import ma.digitalia.suividutemps.entities.PlanningTravail;
import ma.digitalia.suividutemps.repositories.PlanningTravailRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanningTravailServiceImpl implements PlanningTravailService {

    PlanningTravailRepository planningTravailRepository;

    public PlanningTravailServiceImpl(PlanningTravailRepository planningTravailRepository) {
        this.planningTravailRepository = planningTravailRepository;
    }

    @Override
    public void createPlanning(List<PlanningTravail> planningTravail) {
        planningTravail.forEach(PlanningTravail::calculateHeuresParJour);
        planningTravailRepository.saveAll(planningTravail);
    }
}
