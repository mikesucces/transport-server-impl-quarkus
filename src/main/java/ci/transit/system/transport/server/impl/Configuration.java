package ci.transit.system.transport.server.impl;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

import java.util.Optional;

/**
 * Cette interface represente les configurations du processus.
 *
 * @author Transit
 *
 */
@StaticInitSafe
@ConfigMapping(prefix = "transit.system.transport")
public interface Configuration {

  /** Nombre de jours avant echeance declenchant une alerte. */
  Optional<Integer> alertThresholdDays();
}
