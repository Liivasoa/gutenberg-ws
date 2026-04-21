package mg.msys.gutenber_ws;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationModulesTest {

    @Test
    void modulesAreCompliant() {
        ApplicationModules.of(GutenberWsApplication.class).verify();
    }
}
