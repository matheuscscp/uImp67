package org.unbiquitous.uImpala.engine.io;

import org.unbiquitous.uImpala.util.math.Rectangle;

/**
 * Class to hold data about mouse events.
 * @author Pimenta
 *
 */
public class MouseEvent extends InputEvent {
  /**
   * Assignment constructor.
   * @param t Type.
   * @param x Coordinate x.
   * @param y Coordinate y.
   * @param butt The related button. -1 for motion event.
   */
  public MouseEvent(int t, int x, int y, int butt) {
    super(t);
    this.x = x;
    this.y = y;
    button = butt;
  }
  
  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
  
  public int getButton() {
    return button;
  }
  
  public boolean isInside(Rectangle rect) {
    return rect.isPointInside(x, y);
  }
  
  protected int x, y, button;
}
