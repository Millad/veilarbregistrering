package no.nav.fo.veilarbregistrering.oppfolging.adapter;

import no.nav.fo.veilarbregistrering.oppfolging.OppfolgingGateway;
import no.nav.fo.veilarbregistrering.oppfolging.Oppfolgingsstatus;
import no.nav.fo.veilarbregistrering.profilering.Innsatsgruppe;
import no.nav.fo.veilarbregistrering.besvarelse.Besvarelse;
import no.nav.fo.veilarbregistrering.oppfolging.Formidlingsgruppe;
import no.nav.fo.veilarbregistrering.oppfolging.Rettighetsgruppe;
import no.nav.fo.veilarbregistrering.oppfolging.Servicegruppe;

public class OppfolgingGatewayImpl implements OppfolgingGateway {

    private final OppfolgingClient oppfolgingClient;

    public OppfolgingGatewayImpl(OppfolgingClient oppfolgingClient) {
        this.oppfolgingClient = oppfolgingClient;
    }

    @Override
    public Oppfolgingsstatus hentOppfolgingsstatus(String fodselsnummer) {
        OppfolgingStatusData oppfolgingStatusData = oppfolgingClient.hentOppfolgingsstatus(fodselsnummer);

        return map(oppfolgingStatusData);
    }

    private static Oppfolgingsstatus map(OppfolgingStatusData oppfolgingStatusData) {
        return new Oppfolgingsstatus(
                oppfolgingStatusData.isUnderOppfolging(),
                oppfolgingStatusData.getKanReaktiveres(),
                oppfolgingStatusData.getErSykmeldtMedArbeidsgiver(),
                oppfolgingStatusData.getFormidlingsgruppe() != null ?
                        Formidlingsgruppe.of(oppfolgingStatusData.getFormidlingsgruppe()) : null,
                oppfolgingStatusData.getServicegruppe() != null ?
                        Servicegruppe.of(oppfolgingStatusData.getServicegruppe()) : null,
                oppfolgingStatusData.getRettighetsgruppe() != null ?
                        Rettighetsgruppe.of(oppfolgingStatusData.getRettighetsgruppe()) : null);
    }

    @Override
    public void aktiverBruker(String foedselsnummer, Innsatsgruppe innsatsgruppe) {
        oppfolgingClient.aktiverBruker(new AktiverBrukerData(new Fnr(foedselsnummer), innsatsgruppe));
    }

    @Override
    public void reaktiverBruker(String fodselsnummer) {
        oppfolgingClient.reaktiverBruker(fodselsnummer);
    }

    @Override
    public void settOppfolgingSykmeldt(String fodselsnummer, Besvarelse besvarelse) {
        oppfolgingClient.settOppfolgingSykmeldt(SykmeldtBrukerType.of(besvarelse), fodselsnummer);
    }

}
