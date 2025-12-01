package com.careverify.adapter.npi;

import com.careverify.adapter.npi.model.NpiNormalized;
import reactor.core.publisher.Mono;

public interface NpiAdapter {
    Mono<NpiNormalized> fetchByNpi(String npi);
}

