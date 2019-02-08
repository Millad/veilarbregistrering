package no.nav.fo.veilarbregistrering.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.dialogarena.aktor.AktorService;
import no.nav.fo.veilarbregistrering.db.ArbeidssokerregistreringRepository;
import no.nav.fo.veilarbregistrering.domain.AktorId;
import no.nav.fo.veilarbregistrering.domain.ManuellRegistrering;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import static no.nav.fo.veilarbregistrering.utils.FnrUtils.getAktorIdOrElseThrow;

@Slf4j
public class ManuellRegistreringService {

    private final AktorService aktorService;
    private final ArbeidssokerregistreringRepository arbeidssokerregistreringRepository;
    private final Provider<HttpServletRequest> requestProvider;

    public ManuellRegistreringService(AktorService aktorService,
                                      ArbeidssokerregistreringRepository arbeidssokerregistreringRepository,
                                      Provider<HttpServletRequest> requestProvider) {
        this.aktorService = aktorService;
        this.arbeidssokerregistreringRepository = arbeidssokerregistreringRepository;
        this.requestProvider = requestProvider;
    }

    public void lagreManuellRegistrering(String fnr, String veilederIdent, String veilederEnhetId){

        AktorId aktorId = getAktorIdOrElseThrow(aktorService, fnr);

        final ManuellRegistrering manuellRegistrering = new ManuellRegistrering()
                .setAktorId(aktorId.getAktorId())
                .setVeilederIdent(veilederIdent)
                .setVeilederEnhetId(veilederEnhetId);

        arbeidssokerregistreringRepository.lagreManuellRegistrering(manuellRegistrering);

    }

    public ManuellRegistrering hentManuellRegistrering(String fnr){
        AktorId aktorId = getAktorIdOrElseThrow(aktorService, fnr);
        return arbeidssokerregistreringRepository.hentManuellRegistreringForAktorId(aktorId);
    }

    public String getEnhetIdFromUrlOrThrow() {
        final String enhetId = requestProvider.get().getParameter("enhetId");

        if (enhetId == null) {
            throw new RuntimeException("Mangler enhetId");
        }

        return enhetId;
    }

}