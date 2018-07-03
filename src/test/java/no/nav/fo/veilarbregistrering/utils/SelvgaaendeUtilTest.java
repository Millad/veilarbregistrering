package no.nav.fo.veilarbregistrering.utils;

import no.nav.fo.veilarbregistrering.domain.BrukerRegistrering;
import no.nav.fo.veilarbregistrering.domain.StartRegistreringStatus;
import no.nav.fo.veilarbregistrering.domain.besvarelse.Besvarelse;
import no.nav.fo.veilarbregistrering.domain.besvarelse.HelseHinderSvar;
import no.nav.fo.veilarbregistrering.domain.besvarelse.Stilling;
import org.junit.jupiter.api.Test;

import static no.nav.fo.veilarbregistrering.service.Konstanter.*;
import static no.nav.fo.veilarbregistrering.utils.SelvgaaendeUtil.erBesvarelseneValidertSomSelvgaaende;
import static no.nav.fo.veilarbregistrering.utils.SelvgaaendeUtil.erSelvgaaende;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class SelvgaaendeUtilTest {

    @Test
    void skalValidereSelvgaaendeUnderoppfolging() {
        BrukerRegistrering bruker = getBrukerBesvarelse();
        StartRegistreringStatus startRegistreringStatus = new StartRegistreringStatus()
                .setUnderOppfolging(true);
        assertThat(erSelvgaaende(bruker, startRegistreringStatus )).isFalse();
    }

    @Test
    void brukerMedBesvarelseNus_Kode_0_SkalFeile() {
        BrukerRegistrering bruker = new BrukerRegistrering()
                .setNusKode(NUS_KODE_0)
                .setSisteStilling(new Stilling().setStyrk08(null))
                .setOpprettetDato(null)
                .setEnigIOppsummering(ENIG_I_OPPSUMMERING)
                .setOppsummering(OPPSUMMERING)
                .setBesvarelse(new Besvarelse().setHelseHinder(HelseHinderSvar.NEI));
        assertThat(erBesvarelseneValidertSomSelvgaaende(bruker)).isFalse();
    }

    @Test
    void brukerMedBesvarelseNus_Kode_2_SkalIkkeFeile() {
        BrukerRegistrering bruker = new BrukerRegistrering()
                .setNusKode(NUS_KODE_2)
                .setSisteStilling(new Stilling().setStyrk08(null))
                .setBesvarelse(new Besvarelse().setHelseHinder(HelseHinderSvar.NEI))
                .setOpprettetDato(null)
                .setEnigIOppsummering(ENIG_I_OPPSUMMERING)
                .setOppsummering(OPPSUMMERING);
        assertThat(erBesvarelseneValidertSomSelvgaaende(bruker)).isTrue();
    }

    private BrukerRegistrering getBrukerBesvarelse() {
        return  new BrukerRegistrering()
                .setNusKode(NUS_KODE_4)
                .setSisteStilling(new Stilling().setStyrk08(null))
                .setOpprettetDato(null)
                .setEnigIOppsummering(ENIG_I_OPPSUMMERING)
                .setOppsummering(OPPSUMMERING)
                .setBesvarelse(new Besvarelse().setHelseHinder(HelseHinderSvar.NEI));
    }

}