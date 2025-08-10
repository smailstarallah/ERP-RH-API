package ma.digitalia.suividutemps.services;

import ma.digitalia.suividutemps.repositories.PlanningTravailRepository;
import org.springframework.stereotype.Service;

@Service
public class PlanningTravailServiceImpl implements PlanningTravailService {

    PlanningTravailRepository planningTravailRepository;

    public PlanningTravailServiceImpl(PlanningTravailRepository planningTravailRepository) {
        this.planningTravailRepository = planningTravailRepository;
    }


    @Override
    public void startTimeTracking(Long empId) {

    }

    @Override
    public void stopTimeTracking(Long empId) {

    }

    @Override
    public void startPauseTime(Long empId) {

    }

    @Override
    public void stopPauseTime(Long empId) {

    }
}
