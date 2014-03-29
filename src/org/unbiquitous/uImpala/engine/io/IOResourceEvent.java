package org.unbiquitous.uImpala.engine.io;

import org.unbiquitous.uImpala.util.observer.Event;

/**
 * Data for IOResource events.
 * @author Pimenta
 *
 */
public final class IOResourceEvent extends Event {
  /**
   * Assignment constructor.
   * @param rsc The resource.
   */
  public IOResourceEvent(IOResource rsc) {
    resource = rsc;
  }
  
  /**
   * Access method to the resource that triggered the event.
   * @return The resource.
   */
  public IOResource resource() {
    return resource;
  }
  
  private IOResource resource;
}
