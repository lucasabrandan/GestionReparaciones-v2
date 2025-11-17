package com.example.gestionreparacionesapp.data.util;

import android.util.Patterns;

public class ValidatorUtils {

    public static boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }


}
