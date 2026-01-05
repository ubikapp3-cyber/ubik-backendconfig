package com.ubik.usermanagement.infrastructure.security;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Utilidad para sanitizar input y prevenir ataques XSS
 * Escapa caracteres HTML potencialmente peligrosos
 */
@Component
public class XssSanitizer {

    /**
     * Sanitiza un string escapando caracteres HTML especiales
     * Previene ataques XSS (Cross-Site Scripting)
     * 
     * @param input String a sanitizar
     * @return String sanitizado con caracteres HTML escapados
     */
    @NonNull
    public String sanitize(String input) {
        if (input == null) {
            return "";
        }
        
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    /**
     * Sanitiza un string para uso seguro en mensajes del chatbot
     * Permite ciertos caracteres seguros mientras previene XSS
     * 
     * @param message Mensaje a sanitizar
     * @return Mensaje sanitizado
     */
    @NonNull
    public String sanitizeMessage(@NonNull String message) {
        // Primero sanitizamos completamente
        String sanitized = sanitize(message);
        
        // Removemos caracteres de control que podrían causar problemas
        sanitized = sanitized.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        
        // Limitamos espacios en blanco consecutivos
        sanitized = sanitized.replaceAll(" {3,}", "  ");
        
        return sanitized.trim();
    }

    /**
     * Verifica si un string contiene potenciales ataques XSS
     * 
     * @param input String a verificar
     * @return true si detecta patrones sospechosos
     */
    public boolean containsSuspiciousContent(@NonNull String input) {
        if (input == null) {
            return false;
        }
        
        String lowerInput = input.toLowerCase();
        
        // Patrones comunes de XSS
        return lowerInput.contains("<script") ||
               lowerInput.contains("javascript:") ||
               lowerInput.contains("onerror=") ||
               lowerInput.contains("onload=") ||
               lowerInput.contains("onclick=") ||
               lowerInput.contains("<iframe") ||
               lowerInput.contains("eval(") ||
               lowerInput.contains("expression(");
    }
}
