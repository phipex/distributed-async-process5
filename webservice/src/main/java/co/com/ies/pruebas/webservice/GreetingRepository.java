package co.com.ies.pruebas.webservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GreetingRepository extends JpaRepository<Greeting, Long> {

    List<Greeting> findByIpTramitedIsNull();

    Optional<Greeting> findFirstByIpTramitedIsNullOrderByIdAsc();



}

