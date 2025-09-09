package ma.digitalia.systemalert.service.impl;

import ma.digitalia.systemalert.exception.AlerteNotFoundException;
import ma.digitalia.systemalert.model.dto.AlerteDTO;
import ma.digitalia.systemalert.model.entity.Alerte;
import ma.digitalia.systemalert.model.enums.StatusAlerte;
import ma.digitalia.systemalert.model.enums.TypeAlerte;
import ma.digitalia.systemalert.model.mapper.AlerteMapper;
import ma.digitalia.systemalert.repository.AlerteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AlerteServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class AlerteServiceImplTest {

    @Mock
    private AlerteRepository alerteRepository;

    @Mock
    private AlerteMapper alerteMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AlerteServiceImpl alerteService;

    private AlerteDTO alerteDTO;
    private Alerte alerte;

    @BeforeEach
    void setUp() {
        alerteDTO = new AlerteDTO();
        alerteDTO.setId(1L);
        alerteDTO.setTitre("Test Alerte");
        alerteDTO.setMessage("Message de test");
        alerteDTO.setType(TypeAlerte.INFO);
        alerteDTO.setStatus(StatusAlerte.UNREAD);
        alerteDTO.setUserId(100L);
        alerteDTO.setDateCreation(LocalDateTime.now());

        alerte = new Alerte();
        alerte.setId(1L);
        alerte.setTitre("Test Alerte");
        alerte.setMessage("Message de test");
        alerte.setType(TypeAlerte.INFO);
        alerte.setStatus(StatusAlerte.UNREAD);
        alerte.setUserId(100L);
        alerte.setDateCreation(LocalDateTime.now());
    }

    @Test
    void creerAlerte_Success() {
        // Given
        when(alerteMapper.toEntity(alerteDTO)).thenReturn(alerte);
        when(alerteRepository.save(any(Alerte.class))).thenReturn(alerte);
        when(alerteMapper.toDTO(alerte)).thenReturn(alerteDTO);

        // When
        AlerteDTO result = alerteService.creerAlerte(alerteDTO);

        // Then
        assertNotNull(result);
        assertEquals(alerteDTO.getTitre(), result.getTitre());
        verify(alerteRepository).save(any(Alerte.class));
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(AlerteDTO.class));
    }

    @Test
    void getAlertesParEmploye_Success() {
        // Given
        List<Alerte> alertes = Arrays.asList(alerte);
        List<AlerteDTO> alerteDTOs = Arrays.asList(alerteDTO);
        when(alerteRepository.findByUserIdOrderByDateCreationDesc(100L)).thenReturn(alertes);
        when(alerteMapper.toDTOList(alertes)).thenReturn(alerteDTOs);

        // When
        List<AlerteDTO> result = alerteService.getAlertesParEmploye(100L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(alerteDTO.getTitre(), result.get(0).getTitre());
    }

    @Test
    void marquerCommeLue_Success() {
        // Given
        when(alerteRepository.findById(1L)).thenReturn(Optional.of(alerte));
        when(alerteRepository.save(alerte)).thenReturn(alerte);
        when(alerteMapper.toDTO(alerte)).thenReturn(alerteDTO);

        // When
        AlerteDTO result = alerteService.marquerCommeLue(1L);

        // Then
        assertNotNull(result);
        assertEquals(StatusAlerte.READ, alerte.getStatus());
        verify(alerteRepository).save(alerte);
    }

    @Test
    void marquerCommeLue_AlerteNotFound() {
        // Given
        when(alerteRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AlerteNotFoundException.class, () -> {
            alerteService.marquerCommeLue(1L);
        });
    }

    @Test
    void supprimerAlerte_Success() {
        // Given
        when(alerteRepository.existsById(1L)).thenReturn(true);

        // When
        alerteService.supprimerAlerte(1L);

        // Then
        verify(alerteRepository).deleteById(1L);
    }

    @Test
    void supprimerAlerte_AlerteNotFound() {
        // Given
        when(alerteRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(AlerteNotFoundException.class, () -> {
            alerteService.supprimerAlerte(1L);
        });
    }

    @Test
    void getAlerteParId_Success() {
        // Given
        when(alerteRepository.findById(1L)).thenReturn(Optional.of(alerte));
        when(alerteMapper.toDTO(alerte)).thenReturn(alerteDTO);

        // When
        AlerteDTO result = alerteService.getAlerteParId(1L);

        // Then
        assertNotNull(result);
        assertEquals(alerteDTO.getTitre(), result.getTitre());
    }

    @Test
    void compterAlertesNonLues_Success() {
        // Given
        when(alerteRepository.countByUserIdAndStatus(100L, StatusAlerte.UNREAD)).thenReturn(5L);

        // When
        long result = alerteService.compterAlertesNonLues(100L);

        // Then
        assertEquals(5L, result);
    }
}
