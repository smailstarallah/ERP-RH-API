package ma.digitalia.generationfichepaie.services;

import ma.digitalia.generationfichepaie.dto.dashboard.DashboardResponseDto;

public interface DashboardService {
    
    /**
     * Récupère les données pour le tableau de bord des fiches de paie
     * @return les données du tableau de bord
     */
    DashboardResponseDto getDashboardData();
}