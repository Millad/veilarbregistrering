package no.nav.fo.veilarbregistrering.bruker.pdl;

import java.util.Base64;

public class AuthUtils {

    public static String basicCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes());
    }

}
