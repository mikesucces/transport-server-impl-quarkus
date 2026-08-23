package ci.transit.system.transport.server.impl;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;

/**
 * Cette classe represente un observateur des evenements du cycle de vie du processus.
 *
 * @author Transit
 *
 */
@Startup
@jakarta.enterprise.context.ApplicationScoped
public class ApplicationLifeCycleListener {

  void onStart(@Observes StartupEvent startupEvent) {

  }

  void onStop(@Observes ShutdownEvent shutdownEvent) {

  }
}
