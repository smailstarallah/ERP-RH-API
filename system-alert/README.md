# Module System-Alert - Documentation

## 🎯 Vue d'ensemble

Le module `system-alert` est maintenant entièrement implémenté selon l'architecture Spring Boot modulaire demandée. Il fournit un système complet de gestion d'alertes avec notifications temps réel via WebSocket.

## 📁 Structure du module

```
system-alert/
├── src/main/java/ma/digitalia/systemalert/
│   ├── SystemAlertApplication.java          # Application principale
│   ├── controller/
│   │   ├── AlerteController.java            # API REST + WebSocket
│   │   └── AlerteDemoController.java        # Contrôleur de démonstration
│   ├── service/
│   │   ├── AlerteService.java               # Interface du service
│   │   ├── AlerteCleanupService.java        # Service de nettoyage automatique
│   │   └── impl/AlerteServiceImpl.java      # Implémentation du service
│   ├── repository/
│   │   └── AlerteRepository.java            # Repository Spring Data JPA
│   ├── model/
│   │   ├── entity/Alerte.java               # Entité JPA
│   │   ├── dto/AlerteDTO.java               # DTO pour transfert
│   │   ├── mapper/AlerteMapper.java         # Mapper MapStruct
│   │   └── enums/
│   │       ├── TypeAlerte.java              # Enum types (INFO, WARNING, ERROR)
│   │       └── StatusAlerte.java            # Enum statuts (UNREAD, READ)
│   ├── websocket/
│   │   └── WebSocketConfig.java             # Configuration WebSocket
│   └── exception/
│       ├── AlerteNotFoundException.java     # Exception personnalisée
│       └── GlobalExceptionHandler.java     # Gestionnaire global d'erreurs
└── src/test/java/
    └── AlerteServiceImplTest.java           # Tests unitaires
```

## 🚀 Fonctionnalités implémentées

### 1. API REST Complète

**Endpoints principaux :**
- `POST /api/alertes` - Créer une alerte
- `GET /api/alertes/employe/{id}` - Alertes d'un employé
- `PATCH /api/alertes/{id}/lu` - Marquer comme lue
- `DELETE /api/alertes/{id}` - Supprimer une alerte
- `GET /api/alertes/employe/{id}/non-lues/count` - Compter les non lues

### 2. WebSocket Temps Réel

**Configuration STOMP + SockJS :**
- Endpoint : `/ws/alertes`
- Topics :
  - `/topic/alertes/employe/{id}` - Notifications individuelles
  - `/topic/alertes/global` - Notifications globales (managers/RH)

### 3. Modèle de Données

**Table `alertes` :**
- `id` (Long, PK)
- `titre` (String, not null)
- `message` (String, not null)
- `type` (TypeAlerte: INFO, WARNING, ERROR)
- `status` (StatusAlerte: UNREAD, READ)
- `date_creation` (LocalDateTime, auto)
- `user_id` (Long, FK)

### 4. Services Métier

**AlerteService :**
- Création d'alertes avec notification WebSocket automatique
- Gestion des statuts (lu/non lu)
- Requêtes par employé, type, période
- Comptage des alertes non lues

**AlerteCleanupService :**
- Nettoyage automatique (cron : tous les jours à 2h)
- Suppression des alertes de plus de 30 jours

### 5. Validation et Gestion d'Erreurs

- Validation avec `@Valid` et annotations Jakarta
- Exceptions personnalisées avec `@ControllerAdvice`
- Réponses d'erreur standardisées (ApiError)

## 🔧 Configuration

**Base de données (H2 en mémoire pour le développement) :**
```properties
spring.datasource.url=jdbc:h2:mem:alertdb
spring.jpa.hibernate.ddl-auto=create-drop
```

**Port du service :**
```properties
server.port=8083
```

## 📊 Démonstration

**Contrôleur de démonstration inclus :**
- `POST /api/demo/alertes/exemples` - Crée 3 alertes d'exemple
- `GET /api/demo/alertes/stats/{userId}` - Statistiques des alertes

## 🧪 Tests

Tests unitaires complets avec Mockito pour :
- Création d'alertes
- Marquage comme lue
- Gestion des exceptions
- Comptage des alertes

## 🏃‍♂️ Utilisation

1. **Démarrer l'application :**
```bash
mvn spring-boot:run
```

2. **Accéder à la console H2 :**
```
http://localhost:8083/h2-console
```

3. **Tester l'API :**
```bash
# Créer des alertes d'exemple
curl -X POST http://localhost:8083/api/demo/alertes/exemples

# Récupérer les alertes d'un employé
curl http://localhost:8083/api/alertes/employe/1001
```

4. **WebSocket (côté client JavaScript) :**
```javascript
const socket = new SockJS('/ws/alertes');
const stompClient = Stomp.over(socket);

stompClient.subscribe('/topic/alertes/employe/1001', function(message) {
    const alerte = JSON.parse(message.body);
    // Afficher la nouvelle alerte
});
```

## ✅ Conformité aux exigences

- ✅ Architecture modulaire Spring Boot
- ✅ Entité JPA avec table `alertes`
- ✅ Repository Spring Data JPA avec méthodes personnalisées
- ✅ Service avec logique métier et `@Transactional`
- ✅ Controller REST avec tous les endpoints demandés
- ✅ WebSocket avec STOMP pour notifications temps réel
- ✅ DTO/Mapper avec MapStruct
- ✅ Validation avec `@Valid` et Jakarta Validation
- ✅ Gestion d'erreurs avec `@ControllerAdvice`
- ✅ Logging avec Slf4j
- ✅ Tests unitaires avec Mockito
- ✅ Respect des principes SOLID
- ✅ Best practices Spring Boot

Le module est maintenant **100% fonctionnel** et prêt pour l'intégration dans votre système ERP-RH !
