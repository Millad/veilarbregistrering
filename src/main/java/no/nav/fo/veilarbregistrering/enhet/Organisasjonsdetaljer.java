package no.nav.fo.veilarbregistrering.enhet;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

public class Organisasjonsdetaljer {

    private final List<Forretningsadresse> forretningsadresser;
    private final List<Postadresse> postadresser;

    public static Organisasjonsdetaljer of(
            List<Forretningsadresse> forretningsadresser,
            List<Postadresse> postadresser) {

        return new Organisasjonsdetaljer(forretningsadresser, postadresser);
    }

    private Organisasjonsdetaljer(
            List<Forretningsadresse> forretningsadresser,
            List<Postadresse> postadresser) {

        this.forretningsadresser = forretningsadresser != null ? forretningsadresser : emptyList();
        this.postadresser = postadresser != null ? postadresser : emptyList();
    }

    public Optional<Kommunenummer> kommunenummer() {
        Optional<Kommunenummer> kommunenummerFraForretningsadresse = kommunenummerFraFoersteGyldigeAdresse(forretningsadresser);
        if (kommunenummerFraForretningsadresse.isPresent()) {
            return kommunenummerFraForretningsadresse;
        }

        Optional<Kommunenummer> kommunenummerFraPostadresse = kommunenummerFraFoersteGyldigeAdresse(postadresser);
        if (kommunenummerFraPostadresse.isPresent()) {
            return kommunenummerFraPostadresse;
        }

        return Optional.empty();
    }

    private Optional<Kommunenummer> kommunenummerFraFoersteGyldigeAdresse(List<? extends Adresse> adresse) {
        return adresse.stream()
                .filter(a -> a.erGyldig())
                .findFirst()
                .map(a -> a.getKommunenummer());
    }

}
