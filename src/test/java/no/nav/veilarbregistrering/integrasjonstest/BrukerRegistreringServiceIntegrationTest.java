package no.nav.veilarbregistrering.integrasjonstest;

import io.vavr.control.Try;
import no.nav.apiapp.security.PepClient;
import no.nav.dialogarena.aktor.AktorService;
import no.nav.fo.veilarbregistrering.config.DatabaseConfig;
import no.nav.fo.veilarbregistrering.config.RemoteFeatureConfig;
import no.nav.fo.veilarbregistrering.db.ArbeidssokerregistreringRepository;
import no.nav.fo.veilarbregistrering.db.MigrationUtils;
import no.nav.fo.veilarbregistrering.domain.BrukerRegistrering;
import no.nav.fo.veilarbregistrering.domain.OppfolgingStatusData;
import no.nav.fo.veilarbregistrering.httpclient.OppfolgingClient;
import no.nav.fo.veilarbregistrering.httpclient.DigisyfoClient;
import no.nav.fo.veilarbregistrering.service.ArbeidsforholdService;
import no.nav.fo.veilarbregistrering.service.BrukerRegistreringService;
import no.nav.fo.veilarbregistrering.service.StartRegistreringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static no.nav.fo.veilarbregistrering.utils.TestUtils.gyldigBrukerRegistrering;
import static no.nav.fo.veilarbregistrering.utils.TestUtils.lagProfilering;
import static no.nav.veilarbregistrering.db.DatabaseTestContext.setupInMemoryDatabaseContext;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BrukerRegistreringServiceIntegrationTest {

    private static RemoteFeatureConfig.DigisyfoFeature digisyfoFeature;
    private static AnnotationConfigApplicationContext context;

    private static BrukerRegistreringService brukerRegistreringService;
    private static AktorService aktorService;
    private static OppfolgingClient oppfolgingClient;
    private static ArbeidssokerregistreringRepository arbeidssokerregistreringRepository;
    private static StartRegistreringUtils startRegistreringUtils;

    private static String ident = "10108000398"; //Aremark fiktivt fnr.";
    private static final BrukerRegistrering SELVGAENDE_BRUKER = gyldigBrukerRegistrering();

    @BeforeEach
    public void setup() {

        setupInMemoryDatabaseContext();

        context = new AnnotationConfigApplicationContext(
                DatabaseConfig.class,
                BrukerregistreringConfigTest.class
        );

        context.start();

        MigrationUtils.createTables((JdbcTemplate) context.getBean("jdbcTemplate"));
        arbeidssokerregistreringRepository = context.getBean(ArbeidssokerregistreringRepository.class);
        brukerRegistreringService = context.getBean(BrukerRegistreringService.class);
        oppfolgingClient = context.getBean(OppfolgingClient.class);
        aktorService = context.getBean(AktorService.class);
        startRegistreringUtils = context.getBean(StartRegistreringUtils.class);
        digisyfoFeature = context.getBean(RemoteFeatureConfig.DigisyfoFeature.class);
    }

    @AfterEach
    public void tearDown() {
        context.stop();
    }

    @Test
    public void skalRulleTilbakeDatabaseDersomKallTilArenaFeiler() {
        cofigureMocks();
        doThrow(new RuntimeException()).when(oppfolgingClient).aktiverBruker(any());

        Try<Void> run = Try.run(() -> brukerRegistreringService.registrerBruker(SELVGAENDE_BRUKER, ident));
        assertThat(run.isFailure()).isTrue();

        Optional<BrukerRegistrering> brukerRegistrering = ofNullable(arbeidssokerregistreringRepository.hentBrukerregistreringForId(1l));

        assertThat(brukerRegistrering.isPresent()).isFalse();
    }

    @Test
    public void skalLagreIDatabaseDersomKallTilArenaErOK() {
        cofigureMocks();

        BrukerRegistrering brukerRegistrering = brukerRegistreringService.registrerBruker(SELVGAENDE_BRUKER, ident);

        Optional<BrukerRegistrering> reg = ofNullable(arbeidssokerregistreringRepository.hentBrukerregistreringForId(brukerRegistrering.getId()));

        assertThat(reg.isPresent()).isTrue();
    }

    private void cofigureMocks() {
        when(digisyfoFeature.erAktiv()).thenReturn(true);
        when(oppfolgingClient.hentOppfolgingsstatus(any())).thenReturn(new OppfolgingStatusData().withUnderOppfolging(false).withKanReaktiveres(false));
        when(aktorService.getAktorId(any())).thenAnswer((invocation -> Optional.of(invocation.getArgument(0))));
        when(startRegistreringUtils.harJobbetSammenhengendeSeksAvTolvSisteManeder(any(), any())).thenReturn(true);
        when(startRegistreringUtils.profilerBruker(any(), anyInt(), any(), any())).thenReturn(lagProfilering());
    }


    @Configuration
    @ComponentScan
    public static class BrukerregistreringConfigTest {

        @Bean
        public RemoteFeatureConfig.DigisyfoFeature registreringFeature() {
            return mock(RemoteFeatureConfig.DigisyfoFeature.class);
        }

        @Bean
        public ArbeidssokerregistreringRepository arbeidssokerregistreringRepository(JdbcTemplate db) {

            return new ArbeidssokerregistreringRepository(db);
        }

        @Bean
        public AktorService aktoerService() {
            return mock(AktorService.class);
        }

        @Bean
        public OppfolgingClient oppfolgingClient() {
            return mock(OppfolgingClient.class);
        }

        @Bean
        public DigisyfoClient sykeforloepMetadataClient() {
            return mock(DigisyfoClient.class);
        }

        @Bean
        public ArbeidsforholdService arbeidsforholdService() {
            return mock(ArbeidsforholdService.class);
        }

        @Bean
        public StartRegistreringUtils startRegistreringUtils() {
            return mock(StartRegistreringUtils.class);
        }


        @Bean
        BrukerRegistreringService brukerRegistreringService(
                ArbeidssokerregistreringRepository arbeidssokerregistreringRepository,
                AktorService aktorService,
                OppfolgingClient oppfolgingClient,
                DigisyfoClient sykeforloepMetadataClient,
                ArbeidsforholdService arbeidsforholdService,
                StartRegistreringUtils startRegistreringUtils,
                RemoteFeatureConfig.DigisyfoFeature digiSyfoFeature) {
            return new BrukerRegistreringService(
                    arbeidssokerregistreringRepository,
                    aktorService,
                    oppfolgingClient,
                    sykeforloepMetadataClient,
                    arbeidsforholdService,
                    startRegistreringUtils,
                    digiSyfoFeature
            );
        }

        @Bean
        PepClient pepClient() {
            return mock(PepClient.class);
        }
    }


}