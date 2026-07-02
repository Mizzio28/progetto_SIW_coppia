package it.uniroma3.java.siw.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.java.siw.repository.ArmadiettoRepository;

@Service
public class ArmadiettoService {
    @Autowired
    private ArmadiettoRepository armadiettoRepository;
}
