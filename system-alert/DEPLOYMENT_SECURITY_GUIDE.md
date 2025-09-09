# 🛡️ GUIDE DE DÉPLOIEMENT SÉCURISÉ - Module System-Alert

## 📋 CHECKLIST PRÉ-DÉPLOIEMENT

### ✅ Configuration de Production

1. **Base de Données**
```properties
# REMPLACER H2 par une base de données de production
spring.datasource.url=jdbc:postgresql://localhost:5432/erp_alerts
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.h2.console.enabled=false
```

2. **HTTPS/TLS**
```properties
# Configuration SSL obligatoire
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.port=8443
```

3. **CORS Production**
```java
// Dans SecurityConfig.java
configuration.setAllowedOrigins(Arrays.asList(
    "https://votre-domaine-production.com",
    "https://app.votre-entreprise.com"
));
```

4. **Variables d'Environnement**
```bash
export DB_USERNAME=alerts_user
export DB_PASSWORD=super_secure_password
export JWT_SECRET=your_jwt_secret_key
export SSL_KEYSTORE_PATH=/path/to/keystore.p12
export SSL_KEYSTORE_PASSWORD=keystore_password
```

### 🔒 Sécurité Runtime

1. **Authentification JWT**
```java
// Implémenter JwtAuthenticationFilter
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Validation des tokens JWT
}
```

2. **Rate Limiting**
```properties
# Limiter les requêtes par utilisateur
bucket4j.enabled=true
bucket4j.filters.rate-limit.url=.*
bucket4j.filters.rate-limit.rate-limits.limit=100
bucket4j.filters.rate-limit.rate-limits.duration=PT1M
```

3. **Monitoring de Sécurité**
```properties
# Activation des métriques de sécurité
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

### 🚨 Alertes de Sécurité

1. **Détection d'Intrusion**
```java
@Component
public class SecurityEventListener {
    @EventListener
    public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        // Log et alertes pour échecs d'authentification répétés
    }
}
```

2. **Audit des Accès**
```properties
# Logs d'audit complets
logging.level.org.springframework.security=INFO
logging.file.name=/var/log/alerts/security-audit.log
```

## 🔍 TESTS DE SÉCURITÉ

### Tests Automatisés
```bash
# Tests de sécurité
mvn test -Dtest=SecurityTest
mvn verify -Pintegration-tests

# Scan de vulnérabilités
mvn org.owasp:dependency-check-maven:check
```

### Tests Manuels
1. **Test d'Authentification**
   - Tentatives de connexion sans token
   - Vérification des autorisations par rôle
   - Test des endpoints protégés

2. **Test WebSocket**
   - Connexion sans authentification
   - Accès aux topics non autorisés
   - Validation des origines

3. **Test d'Injection**
   - SQL injection dans les paramètres
   - XSS dans les champs texte
   - Validation des entrées

## 📊 MONITORING CONTINU

### Métriques de Sécurité
```yaml
# Prometheus metrics
management.metrics.export.prometheus.enabled=true
custom.security.metrics:
  - failed_login_attempts
  - unauthorized_access_attempts
  - websocket_connections
```

### Alertes Automatiques
```yaml
# Alertes pour activités suspectes
alerts:
  - name: "Multiple Failed Logins"
    condition: "failed_logins > 5 in 1m"
    action: "block_ip"
  
  - name: "Unauthorized Access"
    condition: "403_errors > 10 in 5m"
    action: "notify_admin"
```

## 🚀 COMMANDES DE DÉPLOIEMENT

### 1. Build Sécurisé
```bash
# Nettoyage et compilation avec profil production
mvn clean compile -Pprod

# Tests de sécurité
mvn verify -Psecurity-tests

# Package avec optimisations
mvn package -Pprod -DskipTests=false
```

### 2. Déploiement Docker
```dockerfile
FROM openjdk:17-jre-slim
COPY target/system-alert-*.jar app.jar
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 3. Configuration Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: system-alert
spec:
  template:
    spec:
      containers:
      - name: system-alert
        image: system-alert:latest
        ports:
        - containerPort: 8443
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
```

## ✅ VALIDATION POST-DÉPLOIEMENT

### Tests de Smoke
```bash
# Vérification des endpoints sécurisés
curl -k https://votre-domaine.com:8443/api/alertes -H "Authorization: Bearer $JWT_TOKEN"

# Test WebSocket sécurisé
wscat -c wss://votre-domaine.com:8443/ws/alertes
```

### Audit de Sécurité
1. Scan de vulnérabilités réseau
2. Test de pénétration des API
3. Vérification des certificats SSL
4. Audit des logs de sécurité

## 🚨 PLAN D'INCIDENT DE SÉCURITÉ

### En cas de Brèche
1. **Isolation immédiate**
   ```bash
   kubectl scale deployment system-alert --replicas=0
   ```

2. **Investigation**
   - Analyse des logs d'audit
   - Identification du vecteur d'attaque
   - Évaluation des données compromises

3. **Récupération**
   - Correction des vulnérabilités
   - Redéploiement sécurisé
   - Notification des utilisateurs affectés

## 📞 CONTACTS DE SÉCURITÉ

- **Équipe Sécurité :** security@votre-entreprise.com
- **Administrateur Système :** admin@votre-entreprise.com
- **Équipe DevOps :** devops@votre-entreprise.com

---

**⚠️ RAPPEL IMPORTANT :**
Ce guide doit être adapté selon l'infrastructure et les politiques de sécurité de votre organisation. Un audit de sécurité complet par un expert externe est recommandé avant la mise en production.
