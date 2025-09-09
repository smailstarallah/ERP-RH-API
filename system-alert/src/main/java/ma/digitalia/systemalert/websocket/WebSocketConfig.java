package ma.digitalia.systemalert.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket pour les notifications d'alertes en temps réel
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Active un simple message broker en mémoire pour porter les messages
        // vers les clients sur les destinations préfixées par "/topic"
        config.enableSimpleBroker("/topic");

        // Définit le préfixe pour les messages destinés aux méthodes annotées @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Enregistre l'endpoint WebSocket "/ws/alertes"
        // La gestion CORS sera faite par le module gestion-utilisateur
        registry.addEndpoint("/ws/alertes")
                .setAllowedOrigins("http://localhost:5173")
                .withSockJS();
    }
}
