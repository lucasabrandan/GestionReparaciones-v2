// Código CORRECTO:
package com.example.gestionreparacionesapp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase de utilidad para manejar el hashing y verificación de contraseñas.
 * Utiliza SHA-256 para cumplir con el requisito de seguridad del TP.
 */
public class PasswordUtils {

    private static final String HASH_ALGORITHM = "SHA-256";

    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(plainPassword.getBytes());
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error en el algoritmo de hashing: " + HASH_ALGORITHM, e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        // Hasheamos la contraseña de entrada y comparamos el resultado con el hash almacenado.
        String newHash = hashPassword(rawPassword);
        return newHash != null && newHash.equals(hashedPassword);
    }
}