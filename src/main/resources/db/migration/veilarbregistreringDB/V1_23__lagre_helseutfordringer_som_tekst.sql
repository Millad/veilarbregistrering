ALTER TABLE BRUKER_REGISTRERING add HAR_HELSEUTFORDRINGER_TEKST varchar(30);

UPDATE BRUKER_REGISTRERING SET HAR_HELSEUTFORDRINGER_TEKST='NEI' WHERE HAR_HELSEUTFORDRINGER=0;
UPDATE BRUKER_REGISTRERING SET HAR_HELSEUTFORDRINGER_TEKST='JA' WHERE HAR_HELSEUTFORDRINGER=1;
UPDATE BRUKER_REGISTRERING SET HAR_HELSEUTFORDRINGER_TEKST='-1' WHERE HAR_HELSEUTFORDRINGER_TEKST=null;

DROP VIEW DVH_BRUKER_REGISTRERING;

ALTER TABLE BRUKER_REGISTRERING DROP COLUMN HAR_HELSEUTFORDRINGER;
ALTER TABLE BRUKER_REGISTRERING RENAME COLUMN HAR_HELSEUTFORDRINGER_TEKST TO HAR_HELSEUTFORDRINGER;

CREATE OR REPLACE VIEW DVH_BRUKER_REGISTRERING AS
  (
  SELECT
    BRUKER_REGISTRERING_ID,
    AKTOR_ID,
    OPPRETTET_DATO,
    YRKESPRAKSIS,
    NUS_KODE,
    YRKESBESKRIVELSE,
    KONSEPT_ID,
    case UTDANNING_BESTATT when 'JA' then 1 when 'NEI' then 0 else -1 end as UTDANNING_BESTATT,
    case UTDANNING_GODKJENT_NORGE when 'JA' then 1 when 'NEI' then 0 else -1 end as UTDANNING_GODKJENT_NORGE,
    case HAR_HELSEUTFORDRINGER when 'JA' then 1 when 'NEI' then 0 else -1 end as HELSE_UTFORDRINGER,
    case ANDRE_UTFORDRINGER when 'JA' then 1 when 'NEI' then 0 else -1 end as ANDRE_UTFORDRINGER,
    BEGRUNNELSE_FOR_REGISTRERING
  FROM BRUKER_REGISTRERING
);