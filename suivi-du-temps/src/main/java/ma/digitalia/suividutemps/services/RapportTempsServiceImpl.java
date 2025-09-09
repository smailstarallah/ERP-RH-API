package ma.digitalia.suividutemps.services;

import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.repositories.EmployeRepository;
import ma.digitalia.suividutemps.entities.PlanningTravail;
import ma.digitalia.suividutemps.entities.Pointage;
import ma.digitalia.suividutemps.entities.RapportTemps;
import ma.digitalia.suividutemps.repositories.PlanningTravailRepository;
import ma.digitalia.suividutemps.repositories.PointageRepository;
import ma.digitalia.suividutemps.repositories.RapportTempsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;

@Slf4j
@Service
public class RapportTempsServiceImpl implements RapportTempsService {

    private final RapportTempsRepository rapportTempsRepository;
    private final PointageRepository pointageRepository;
    private final EmployeRepository employeRepository;
    private final PlanningTravailRepository planningTravailRepository;


    public RapportTempsServiceImpl(RapportTempsRepository rapportTempsRepository, PointageRepository pointageRepository
    , EmployeRepository employeRepository, PlanningTravailRepository planningTravailRepository) {
        this.rapportTempsRepository = rapportTempsRepository;
        this.pointageRepository = pointageRepository;
        this.employeRepository = employeRepository;
        this.planningTravailRepository = planningTravailRepository;
    }

    @Override
    public String generateTimeReport(Long userId, String startDate, String endDate) {
        return "";
    }

    @Override
    public void generateMonthlyReport(Long employeId, Month month) {

        try {
            RapportTemps rapportTemps = new RapportTemps();

            Employe employe = (Employe) employeRepository.findById(employeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employé non trouvé avec l'ID: " + employeId));

            int currentYear = LocalDate.now().getYear();
            Month moisPrecedent = month.minus(1);
            int anneeMoisPrecedent = currentYear;
            if (month == Month.JANUARY) {
                moisPrecedent = Month.DECEMBER;
                anneeMoisPrecedent = currentYear - 1;
            }

            rapportTemps.setEmploye(employe);
            YearMonth yearMonth = YearMonth.of(currentYear, month);
            rapportTemps.setPeriode(yearMonth.toString());

            LocalDate dateDebut = LocalDate.of(anneeMoisPrecedent, moisPrecedent, 15);
            LocalDate dateFin = LocalDate.of(currentYear, month, 15);

            List<Pointage> pointages = pointageRepository.findByEmployeAndDateBetween(employe, dateDebut, dateFin);
            int totalJoursTravail = pointages.size();
            if (pointages.isEmpty()) {
                log.error("Aucun pointage trouvé pour l'employé avec l'ID: " + employeId
                        + " entre les dates " + dateDebut + " et " + dateFin);
                return;
            }
            for (Pointage pointage : pointages) {
                DayOfWeek dayOfWeek = pointage.getDate().getDayOfWeek();
                PlanningTravail planningTravail = planningTravailRepository.findByJourSemaine(dayOfWeek);
                //gestion des weekends
                if (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY) {
                    totalJoursTravail--;
                    if (planningTravail.getHeureDebutMatin() == null) {
                        Duration totalActuel = rapportTemps.getTotalHeuresSupplementaires();
                        rapportTemps.setTotalHeuresSupplementaires(totalActuel.plus(pointage.getHeuresTravaillees()));
                        continue;
                    }
                }
                if (pointage.getHeureEntree() == null) {
                    rapportTemps.setNombreJoursAbsence(rapportTemps.getNombreJoursAbsence() + 1);
                    continue;
                }
                if (planningTravail.getHeureDebutMatin().isBefore(LocalTime.from(pointage.getHeureEntree()))) {
                    Duration retardMatin = Duration.between(planningTravail.getHeureDebutMatin(), pointage.getHeureEntree());

                    if (retardMatin.toMinutes() > 10) {
                        log.info("Employé en retard de {} minutes pour l'entrée du matin. Prévu: {}, Réel: {}",
                                retardMatin.toMinutes(),
                                planningTravail.getHeureDebutMatin(),
                                pointage.getHeureEntree());
                        rapportTemps.setNombreRetards(rapportTemps.getNombreRetards() + 1);
                    }
                }

//                if (planningTravail.getHeureDebutApresMidi().isBefore(pointage.getPauseTerminee())) {
//                    Duration retardApresMidi = Duration.between(planningTravail.getHeureDebutApresMidi(), pointage.getPauseTerminee());
//
//                    if (retardApresMidi.toMinutes() > 10) {
//                        log.info("Employé en retard de {} minutes pour l'entrée après-midi. Prévu: {}, Réel: {}",
//                                retardApresMidi.toMinutes(),
//                                planningTravail.getHeureDebutApresMidi(),
//                                pointage.getPauseTerminee());
//                        rapportTemps.setNombreRetards(rapportTemps.getNombreRetards() + 1);
//                    }
//                }
                if (!(planningTravail.getHeuresParJour().toHours() == pointage.getHeuresTravaillees().toHours())) {
                    Duration heuresTravaillees = pointage.getHeuresTravaillees();
                    Duration heuresPreves = planningTravail.getHeuresParJour();
                    Duration difference = heuresTravaillees.minus(heuresPreves);

                    if (!difference.isNegative() && !difference.isZero()) {
                        Duration totalActuel = rapportTemps.getTotalHeuresSupplementaires();
                        rapportTemps.setTotalHeuresSupplementaires(totalActuel.plus(difference));
                        rapportTemps.setTotalHeuresTravaillees(planningTravail.getHeuresParJour().plus(rapportTemps.getTotalHeuresTravaillees()));
                    }
                    else if (difference.isNegative()) {
                        Duration heuresManquantes = difference.abs();
                        // à gérer
                        rapportTemps.setTotalHeuresTravaillees(planningTravail.getHeuresParJour().minus(heuresManquantes).plus(rapportTemps.getTotalHeuresTravaillees()));
                    }
                } else {
                    rapportTemps.setTotalHeuresTravaillees(planningTravail.getHeuresParJour().plus(rapportTemps.getTotalHeuresTravaillees()));
                }
            }
            int joursAbsence = rapportTemps.getNombreJoursAbsence();
            int joursPresence = totalJoursTravail - joursAbsence;
            rapportTemps.setNombreJoursTravail(totalJoursTravail);
            if (totalJoursTravail > 0) {
                BigDecimal tauxPresence = BigDecimal.valueOf(joursPresence)
                        .divide(BigDecimal.valueOf(totalJoursTravail), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

                rapportTemps.setTauxPresence(tauxPresence);
            } else {
                rapportTemps.setTauxPresence(BigDecimal.ZERO);
            }
            rapportTempsRepository.save(rapportTemps);
            log.info("Rapport mensuel généré pour l'employé avec l'ID: {} pour le mois de {} de l'année {}", employeId, month, currentYear);
            log.info("rapport de tempt: {}", rapportTemps);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la génération du rapport mensuel pour l'employé avec l'ID: {}", employeId, e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du rapport mensuel pour l'employé avec l'ID: " + employeId, e);
        }
    }

    @Override
    public RapportTemps getMonthlyReport(Employe employe, YearMonth yearMonth) {
        if(employe == null && yearMonth == null) {
            throw new IllegalArgumentException("L'identifiant de l'employé et le mois ne peuvent pas être nuls");
        }
        RapportTemps rapportTemps = rapportTempsRepository.findByEmployeAndPeriode(employe, yearMonth.toString());
        if (rapportTemps == null) {
            generateMonthlyReport(employe.getId(), yearMonth.getMonth());
            rapportTemps = rapportTempsRepository.findByEmployeAndPeriode(employe, yearMonth.toString());
        }

        return rapportTemps;
    }

    @Override
    public BigDecimal getNombreAbsences(Employe employe, YearMonth yearMonth) {
        RapportTemps rapportTemps = rapportTempsRepository.findByEmployeAndPeriode(employe, yearMonth.toString());
        return BigDecimal.valueOf(rapportTemps.getNombreJoursAbsence());
    }


}
