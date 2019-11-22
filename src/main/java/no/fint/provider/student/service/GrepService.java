package no.fint.provider.student.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.timeplan.FagResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GrepService {

    @Autowired
    private RestTemplate restTemplate;

    public List<Map<String,String>> getGrepFagkoder() {
        String text = restTemplate.getForObject("http://data.udir.no/kl06/odata/Fagkode?%24format=json", String.class);
        Configuration conf = Configuration.builder().jsonProvider(new JacksonJsonProvider(new ObjectMapper())).build();
        return JsonPath.using(conf).parse(text).read(JsonPath.compile("$..results[*][\"Kode\",\"Uri\",\"Tittel\"]"));
    }

    public List<FagResource> getFag() {
        return getGrepFagkoder()
                .stream()
                .map(this::createFagResource)
                .collect(Collectors.toList());
    }

    private FagResource createFagResource(Map<String, String> item) {
        FagResource fag = new FagResource();
        fag.setNavn(item.get("Kode"));
        fag.setBeskrivelse(item.get("Tittel"));
        Identifikator psi = new Identifikator();
        psi.setIdentifikatorverdi(item.get("Kode"));
        fag.setSystemId(psi);
        fag.addGrepreferanse(Link.with(item.get("Uri")));
        return fag;
    }
}
