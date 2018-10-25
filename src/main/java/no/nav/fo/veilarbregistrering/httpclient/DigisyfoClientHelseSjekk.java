package no.nav.fo.veilarbregistrering.httpclient;

import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import no.nav.fo.veilarbregistrering.config.RemoteFeatureConfig;

import static no.nav.fo.veilarbregistrering.httpclient.DigisyfoClient.DIGISYFO_BASE_URL_PROPERTY_NAME;
import static no.nav.sbl.rest.RestUtils.withClient;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class DigisyfoClientHelseSjekk implements Helsesjekk {

    private String digiSyfoPingUrl = getRequiredProperty(DIGISYFO_BASE_URL_PROPERTY_NAME) + "/ping";
    private RemoteFeatureConfig.SykemeldtRegistreringFeature sykemeldtRegistreringFeature;

    public DigisyfoClientHelseSjekk(RemoteFeatureConfig.SykemeldtRegistreringFeature sykemeldtRegistreringFeature) {
        this.sykemeldtRegistreringFeature = sykemeldtRegistreringFeature;
    }

    @Override
    public void helsesjekk() throws Throwable {
        if (!sykemeldtRegistreringFeature.erSykemeldtRegistreringAktiv()) {
            return;
        }
        int status = withClient(c ->
                c.target(digiSyfoPingUrl)
                        .request()
                        .get()
                        .getStatus());
        if (!(status >= 200 && status < 300)) {
            throw new IllegalStateException("HTTP status " + status);
        }
    }

    @Override
    public HelsesjekkMetadata getMetadata() {
        return new HelsesjekkMetadata(
                "digisyfo",
                digiSyfoPingUrl,
                "Ping av digisyfo",
                true
        );
    }

}