package no.nav.fo.veilarbregistrering.sykemelding.adapter;

import com.google.common.net.MediaType;
import no.nav.common.oidc.SystemUserTokenProvider;
import no.nav.fo.veilarbregistrering.arbeidsforhold.ArbeidsforholdGateway;
import no.nav.fo.veilarbregistrering.bruker.AktorId;
import no.nav.fo.veilarbregistrering.bruker.Bruker;
import no.nav.fo.veilarbregistrering.bruker.Foedselsnummer;
import no.nav.fo.veilarbregistrering.bruker.PersonGateway;
import no.nav.fo.veilarbregistrering.oppfolging.adapter.OppfolgingClient;
import no.nav.fo.veilarbregistrering.oppfolging.adapter.OppfolgingGatewayImpl;
import no.nav.fo.veilarbregistrering.profilering.ProfileringRepository;
import no.nav.fo.veilarbregistrering.profilering.StartRegistreringUtils;
import no.nav.fo.veilarbregistrering.registrering.bruker.*;
import no.nav.fo.veilarbregistrering.registrering.manuell.ManuellRegistreringService;
import no.nav.fo.veilarbregistrering.registrering.resources.StartRegistreringStatusDto;
import no.nav.fo.veilarbregistrering.sykemelding.SykemeldingService;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import static no.nav.fo.veilarbregistrering.registrering.bruker.RegistreringType.SYKMELDT_REGISTRERING;
import static no.nav.fo.veilarbregistrering.registrering.bruker.SykmeldtRegistreringTestdataBuilder.gyldigSykmeldtRegistrering;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class SykmeldtInfoClientTest {
    private static final String MOCKSERVER_URL = "localhost";
    private static final int MOCKSERVER_PORT = 1082;

    private static final String IDENT = "10108000398"; //Aremark fiktivt fnr.";;
    private static final Bruker BRUKER = Bruker.of(Foedselsnummer.of(IDENT), AktorId.valueOf("AKTØRID"));

    private BrukerRegistreringService brukerRegistreringService;
    private OppfolgingClient oppfolgingClient;
    private SykmeldtInfoClient sykeforloepMetadataClient;
    private ClientAndServer mockServer;

    @AfterEach
    public void tearDown() {
        mockServer.stop();
    }

    @BeforeEach
    public void setup() {
        mockServer = ClientAndServer.startClientAndServer(MOCKSERVER_PORT);
        UnleashService unleashService = mock(UnleashService.class);
        oppfolgingClient = buildOppfolgingClient();
        PersonGateway personGateway = mock(PersonGateway.class);
        BrukerRegistreringRepository brukerRegistreringRepository = mock(BrukerRegistreringRepository.class);
        ProfileringRepository profileringRepository = mock(ProfileringRepository.class);
        ArbeidsforholdGateway arbeidsforholdGateway = mock(ArbeidsforholdGateway.class);
        sykeforloepMetadataClient = buildSykeForloepClient();
        StartRegistreringUtils startRegistreringUtils = mock(StartRegistreringUtils.class);
        ManuellRegistreringService manuellRegistreringService = mock(ManuellRegistreringService.class);
        ArbeidssokerRegistrertProducer arbeidssokerRegistrertProducer = (aktorId, brukersSituasjon, opprettetDato) -> {
        }; //Noop, vi trenger ikke kafka
        ArbeidssokerProfilertProducer arbeidssokerProfileringProducer = (aktorId, innsatsgruppe, profilertDato) -> {
        }; //Noop, vi trenger ikke kafka

        brukerRegistreringService =
                new BrukerRegistreringService(
                        brukerRegistreringRepository,
                        profileringRepository,
                        new OppfolgingGatewayImpl(oppfolgingClient),
                        personGateway,
                        new SykemeldingService(new SykemeldingGatewayImpl(sykeforloepMetadataClient)),
                        arbeidsforholdGateway,
                        manuellRegistreringService,
                        startRegistreringUtils,
                        unleashService,
                        arbeidssokerRegistrertProducer,
                        arbeidssokerProfileringProducer);


        when(startRegistreringUtils.harJobbetSammenhengendeSeksAvTolvSisteManeder(any(), any())).thenReturn(true);
        when(unleashService.isEnabled(any())).thenReturn(true);
    }

    private SykmeldtInfoClient buildSykeForloepClient() {
        Provider<HttpServletRequest> httpServletRequestProvider = new ConfigBuildClient().invoke();
        String baseUrl = "http://" + MOCKSERVER_URL + ":" + MOCKSERVER_PORT + "/";
        return sykeforloepMetadataClient = new SykmeldtInfoClient(baseUrl, httpServletRequestProvider);
    }

    private OppfolgingClient buildOppfolgingClient() {
        Provider<HttpServletRequest> httpServletRequestProvider = new ConfigBuildClient().invoke();
        String baseUrl = "http://" + MOCKSERVER_URL + ":" + MOCKSERVER_PORT;
        return oppfolgingClient = new OppfolgingClient(baseUrl, httpServletRequestProvider, null, null);
    }

    @Test
    @Disabled
    public void testAtRegistreringAvSykmeldtGirOk() {
        mockSykmeldtIArena();
        mockSykmeldtOver39u();
        SykmeldtRegistrering sykmeldtRegistrering = gyldigSykmeldtRegistrering();
        mockServer.when(request().withMethod("POST").withPath("/oppfolging/aktiverSykmeldt")).respond(response().withStatusCode(204));
        brukerRegistreringService.registrerSykmeldt(sykmeldtRegistrering, BRUKER);
    }

    @Test
    @Disabled
    public void testAtHentingAvSykeforloepMetadataGirOk() {
        mockSykmeldtIArena();
        mockSykmeldtOver39u();
        StartRegistreringStatusDto startRegistreringStatus = brukerRegistreringService.hentStartRegistreringStatus(Foedselsnummer.of(IDENT));
        assertSame(startRegistreringStatus.getRegistreringType(), SYKMELDT_REGISTRERING);
    }

    @Test
    public void testAtGirInternalServerErrorExceptionDersomRegistreringAvSykmeldtFeiler() {
        mockSykmeldtIArena();
        mockSykmeldtOver39u();
        SykmeldtRegistrering sykmeldtRegistrering = gyldigSykmeldtRegistrering();
        mockServer.when(request().withMethod("POST").withPath("/oppfolging/aktiverSykmeldt")).respond(response().withStatusCode(502));
        assertThrows(RuntimeException.class, () -> brukerRegistreringService.registrerSykmeldt(sykmeldtRegistrering, BRUKER));
    }

    private void mockSykmeldtOver39u() {
        mockServer.when(request().withMethod("GET").withPath("/sykeforloep/metadata"))
                .respond(response().withBody(sykmeldtOver39u(), MediaType.JSON_UTF_8).withStatusCode(200));
    }

    private void mockSykmeldtIArena() {
        mockServer.when(request().withMethod("GET").withPath("/oppfolging"))
                .respond(response().withBody(harIkkeOppfolgingsflaggOgErInaktivIArenaBody(), MediaType.JSON_UTF_8).withStatusCode(200));
    }

    private String sykmeldtOver39u() {
        return "{\n" +
                "\"erArbeidsrettetOppfolgingSykmeldtInngangAktiv\": true\n" +
                "}";
    }

    private String harIkkeOppfolgingsflaggOgErInaktivIArenaBody() {
        return "{\n" +
                "\"erSykmeldtMedArbeidsgiver\": true\n" +
                "}";
    }

    private static class ConfigBuildClient {
        public Provider<HttpServletRequest> invoke() {
            SystemUserTokenProvider systemUserTokenProvider = mock(SystemUserTokenProvider.class);
            Provider<HttpServletRequest> httpServletRequestProvider = mock(Provider.class);
            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequestProvider.get()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader(any())).thenReturn("");
            when(systemUserTokenProvider.getSystemUserAccessToken()).thenReturn("testToken");
            return httpServletRequestProvider;
        }
    }
}
