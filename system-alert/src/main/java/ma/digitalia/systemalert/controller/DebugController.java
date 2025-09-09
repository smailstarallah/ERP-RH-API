package ma.digitalia.systemalert.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Dans votre contrôleur principal ou un filtre
@RestController
public class DebugController {

    @RequestMapping("/ws/alertes/info")
    public ResponseEntity<?> debugSockJSInfo(HttpServletRequest request) {
        System.out.println("🔍 Requête SockJS Info reçue:");
        System.out.println("   Method: " + request.getMethod());
        System.out.println("   Headers: " + Collections.list(request.getHeaderNames()));
        System.out.println("   Origin: " + request.getHeader("Origin"));

        Map<String, Object> response = new HashMap<>();
        response.put("websocket", true);
        response.put("origins", Arrays.asList("*:*"));
        response.put("cookie_needed", false);
        response.put("entropy", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}