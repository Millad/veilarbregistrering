package no.nav.fo.veilarbregistrering.registrering.resources;

import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.dialogarena.aktor.AktorService;
import no.nav.fo.veilarbregistrering.bruker.UserService;
import no.nav.fo.veilarbregistrering.config.RemoteFeatureConfig;
import no.nav.fo.veilarbregistrering.registrering.bruker.BrukerRegistreringService;
import no.nav.fo.veilarbregistrering.registrering.bruker.OrdinaerBrukerRegistrering;
import no.nav.fo.veilarbregistrering.registrering.bruker.StartRegistreringStatus;
import no.nav.fo.veilarbregistrering.registrering.bruker.SykmeldtRegistrering;
import no.nav.fo.veilarbregistrering.registrering.bruker.besvarelse.Besvarelse;
import no.nav.fo.veilarbregistrering.registrering.bruker.besvarelse.FremtidigSituasjonSvar;
import no.nav.fo.veilarbregistrering.registrering.bruker.besvarelse.HelseHinderSvar;
import no.nav.fo.veilarbregistrering.registrering.bruker.besvarelse.TilbakeIArbeidSvar;
import no.nav.fo.veilarbregistrering.registrering.manuell.ManuellRegistreringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistreringResourceTest {

    private VeilarbAbacPepClient pepClient;
    private RegistreringResource registreringResource;
    private UserService userService;
    private ManuellRegistreringService manuellRegistreringService;
    private BrukerRegistreringService brukerRegistreringService;
    private AktorService aktorService;
    private RemoteFeatureConfig.TjenesteNedeFeature tjenesteNedeFeature;
    private RemoteFeatureConfig.ManuellRegistreringFeature manuellRegistreringFeature;

    private static String IDENT = "10108000398"; //Aremark fiktivt fnr.";

    @BeforeEach
    public void setup() {
        pepClient = mock(VeilarbAbacPepClient.class);
        userService = mock(UserService.class);
        manuellRegistreringService = mock(ManuellRegistreringService.class);
        brukerRegistreringService = mock(BrukerRegistreringService.class);
        aktorService = mock(AktorService.class);
        tjenesteNedeFeature = mock(RemoteFeatureConfig.TjenesteNedeFeature.class);
        manuellRegistreringFeature = mock(RemoteFeatureConfig.ManuellRegistreringFeature.class);

        registreringResource = new RegistreringResource(
                pepClient,
                userService,
                manuellRegistreringService,
                brukerRegistreringService,
                aktorService,
                tjenesteNedeFeature,
                manuellRegistreringFeature
        );
    }

    @Test
    public void skalSjekkeTilgangTilBrukerVedHentingAvStartRegistreringsstatus() {
        when(brukerRegistreringService.hentStartRegistreringStatus(any())).thenReturn(new StartRegistreringStatus());
        when(userService.hentFnrFraUrlEllerToken()).thenReturn(IDENT);
        registreringResource.hentStartRegistreringStatus();
        verify(pepClient, times(1)).sjekkLesetilgangTilBruker(any());
    }

    @Test
    public void skalFeileVedHentingAvStartRegistreringsstatusMedUgyldigFnr() {
        when(brukerRegistreringService.hentStartRegistreringStatus(any())).thenReturn(new StartRegistreringStatus());
        when(userService.hentFnrFraUrlEllerToken()).thenCallRealMethod();
        when(userService.getFnrFromUrl()).thenReturn("ugyldigFnr");
        assertThrows(RuntimeException.class, () -> registreringResource.hentRegistrering());
        verify(pepClient, times(0)).sjekkLesetilgangTilBruker(any());
    }

    @Test
    public void skalSjekkeTilgangTilBrukerVedHentingAvRegistrering() {
        when(userService.hentFnrFraUrlEllerToken()).thenReturn(IDENT);
        registreringResource.hentRegistrering();
        verify(pepClient, times(1)).sjekkLesetilgangTilBruker(any());
    }

    @Test
    public void skalSjekkeTilgangTilBrukerVedRegistreringSykmeldt() {
        SykmeldtRegistrering sykmeldtRegistrering = new SykmeldtRegistrering()
                .setBesvarelse(new Besvarelse()
                        .setFremtidigSituasjon(FremtidigSituasjonSvar.SAMME_ARBEIDSGIVER)
                        .setTilbakeIArbeid(TilbakeIArbeidSvar.JA_FULL_STILLING));
        when(userService.hentFnrFraUrlEllerToken()).thenReturn(IDENT);
        registreringResource.registrerSykmeldt(sykmeldtRegistrering);
        verify(pepClient, times(1)).sjekkSkrivetilgangTilBruker(any());
    }

    @Test
    public void skalSjekkeTilgangTilBrukerVedRegistreringAvBruker() {
        OrdinaerBrukerRegistrering ordinaerBrukerRegistrering = new OrdinaerBrukerRegistrering()
                .setBesvarelse(new Besvarelse().setHelseHinder(HelseHinderSvar.NEI));

        when(userService.hentFnrFraUrlEllerToken()).thenCallRealMethod();
        when(userService.getFnr()).thenReturn(IDENT);
        registreringResource.registrerBruker(ordinaerBrukerRegistrering);
        verify(pepClient, times(1)).sjekkSkrivetilgangTilBruker(any());
    }
}