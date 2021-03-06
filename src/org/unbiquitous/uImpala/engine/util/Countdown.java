package org.unbiquitous.uImpala.engine.util;

import org.unbiquitous.uImpala.engine.core.GameObject;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.time.Time;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Observations;
import org.unbiquitous.uImpala.util.observer.Subject;

/**
 * Timer class with event approach.
 * @author Pimenta
 *
 */
public class Countdown extends GameObject implements Subject {
  /**
   * Starts the countdown. If already counting, starts over.
   * @param ms Time to countdown in milliseconds.
   */
  public void start(long ms) {
    if (ms > 0) {
      started = true;
      paused = false;
      finalTime = Time.get() + ms;
    }
  }
  
  /**
   * Pauses the countdown if not already paused.
   */
  public void pause() {
    if (!paused) {
      paused = true;
      pauseTime = Time.get();
    }
  }
  
  /**
   * Resumes the countdown if it is paused.
   */
  public void resume() {
    if (paused) {
      paused = false;
      finalTime += Time.get() - pauseTime;
    }
  }
  
  /**
   * Stops and sets the counting to zero.
   */
  public void reset() {
    started = false;
  }
  
  /**
   * Query the time remaining.
   * @return Time in milliseconds.
   */
  public long time() {
    if (!started)
      return 0;
    if (paused)
      return finalTime - pauseTime;
    return finalTime - Time.get();
  }
  
  /**
   * Broadcasted when the remaining time reaches zero.
   */
  public static final int EVENT_COMPLETE = 0;
  
  /**
   * Call this method to notify observers.
   */
  public void update() {
    if (started && !paused && Time.get() >= finalTime) {
      started = false;
      observations.broadcast(EVENT_COMPLETE);
    }
  }
  
  public void connect(Integer eventType, Observation obs) {
    observations.connect(eventType, obs);
  }
  
  public void disconnect(Integer eventType, Observation obs) {
    observations.disconnect(eventType, obs);
  }
  
  private Observations observations = new Observations(this, EVENT_COMPLETE);
  private boolean started = false, paused = false;
  private long finalTime = 0, pauseTime = 0;
  
  protected void render(GameRenderers renderers) {
    
  }
  
  protected void wakeup(Object... args) {
    
  }
  
  protected void destroy() {
    
  }
}
