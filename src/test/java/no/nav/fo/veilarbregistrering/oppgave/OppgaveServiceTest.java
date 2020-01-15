package no.nav.fo.veilarbregistrering.oppgave;

import no.nav.fo.veilarbregistrering.bruker.Foedselsnummer;
import no.nav.fo.veilarbregistrering.bruker.GeografiskTilknytning;
import no.nav.fo.veilarbregistrering.bruker.PersonGateway;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OppgaveServiceTest {

    private OppgaveService oppgaveService;

    private OppgaveGateway oppgaveGateway;
    private PersonGateway personGateway;

    @Before
    public void setup() {
        oppgaveGateway = Mockito.mock(OppgaveGateway.class);
        personGateway = Mockito.mock(PersonGateway.class);

        oppgaveService = new OppgaveService(oppgaveGateway, personGateway);
    }

    @Test
    public void skal_tilordne_oppgave_til_H134912_naar_geografisk_tilknytning_ikke_er_satt() {
        when(personGateway.hentGeografiskTilknytning(any(Foedselsnummer.class))).thenReturn(Optional.empty());
        when(oppgaveGateway.opprettOppgave(
                "123",
                "H134912",
                "Denne oppgaven har bruker selv opprettet, og er en pilotering på NAV Grünerløkka." +
                " Brukeren får ikke registrert seg som arbeidssøker." +
                " Kontaktperson ved NAV Grünerløkka er Marthe Harsvik."))
                .thenReturn(new Oppgave() {
            @Override
            public long getId() {
                return 213L;
            }

            @Override
            public String getTildeltEnhetsnr() {
                return "3242";
            }
        });

        oppgaveService.opprettOppgave("123", Foedselsnummer.of("12345678910"));
    }

    @Test
    public void skal_tilordne_oppgave_til_H134912_naar_geografisk_tilknytning_er_030102() {
        when(personGateway.hentGeografiskTilknytning(any(Foedselsnummer.class))).thenReturn(Optional.empty());
        when(oppgaveGateway.opprettOppgave(
                "123",
                "H134912",
                "Denne oppgaven har bruker selv opprettet, og er en pilotering på NAV Grünerløkka." +
                        " Brukeren får ikke registrert seg som arbeidssøker." +
                        " Kontaktperson ved NAV Grünerløkka er Marthe Harsvik."))
                .thenReturn(new Oppgave() {
                    @Override
                    public long getId() {
                        return 213L;
                    }

                    @Override
                    public String getTildeltEnhetsnr() {
                        return "3242";
                    }
                });

        oppgaveService.opprettOppgave("123", Foedselsnummer.of("12345678910"));
    }

    @Test
    public void skal_tilordne_oppgave_til_H134912_naar_geografisk_tilknytning_ikke_er_mappet_opp() {
        when(personGateway.hentGeografiskTilknytning(any(Foedselsnummer.class))).thenReturn(Optional.of(GeografiskTilknytning.of("030105")));
        when(oppgaveGateway.opprettOppgave(
                "123",
                "H134912",
                "Denne oppgaven har bruker selv opprettet, og er en pilotering på NAV Grünerløkka." +
                        " Brukeren får ikke registrert seg som arbeidssøker." +
                        " Kontaktperson ved NAV Grünerløkka er Marthe Harsvik."))
                .thenReturn(new Oppgave() {
                    @Override
                    public long getId() {
                        return 213L;
                    }

                    @Override
                    public String getTildeltEnhetsnr() {
                        return "3242";
                    }
                });

        oppgaveService.opprettOppgave("123", Foedselsnummer.of("12345678910"));
    }

    @Test
    public void skal_tilordne_oppgave_til_B125772_naar_geografisk_tilknytning_er_0412() {
        when(personGateway.hentGeografiskTilknytning(any(Foedselsnummer.class))).thenReturn(Optional.of(GeografiskTilknytning.of("0412")));
        when(oppgaveGateway.opprettOppgave(
                "123",
                "B125772",
                "Denne oppgaven har bruker selv opprettet, og er en pilotering på NAV Ringsaker." +
                        " Brukeren får ikke registrert seg som arbeidssøker." +
                        " Kontaktperson ved NAV Ringsaker er Inger Johanne Bryn."))
                .thenReturn(new Oppgave() {
                    @Override
                    public long getId() {
                        return 213L;
                    }

                    @Override
                    public String getTildeltEnhetsnr() {
                        return "3242";
                    }
                });

        oppgaveService.opprettOppgave("123", Foedselsnummer.of("12345678910"));
    }
}