package org.unbiquitous.uImp67.engine.io;

/**
 * Class to hold data about keyboard events.
 * @author Pimenta
 *
 */
public final class KeyboardEvent extends InputEvent {
  /**
   * Assignment constructor.
   * @param t Event type.
   * @param k Key.
   * @param c Character.
   */
  public KeyboardEvent(int t, int k, char c) {
    super(t);
    key = k;
    character = c;
  }
  
  public int getKey() {
    return key;
  }
  
  public char getCharacter() {
    return character;
  }
  
  protected int key;
  protected char character;
}
