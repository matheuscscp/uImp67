package org.unbiquitous.ubiengine.engine.util;

import java.lang.reflect.Method;

import org.unbiquitous.ubiengine.engine.asset.SpriteOld;
import org.unbiquitous.ubiengine.engine.system.io.KeyboardDevice;
import org.unbiquitous.ubiengine.engine.system.io.MouseSource;
import org.unbiquitous.ubiengine.engine.system.io.KeyboardDevice.KeyDownEvent;
import org.unbiquitous.ubiengine.engine.system.io.MouseSource.MouseDownEvent;
import org.unbiquitous.ubiengine.engine.system.io.MouseSource.MouseUpEvent;
import org.unbiquitous.ubiengine.engine.system.time.Alarm;
import org.unbiquitous.ubiengine.util.mathematics.Rectangle;
import org.unbiquitous.ubiengine.util.observer.Event;
import org.unbiquitous.ubiengine.util.observer.Subject;
import org.unbiquitous.ubiengine.util.observer.Observations;

public class Button implements Subject {

  private static final long ENTER_DELAY = 200;
  
  protected KeyboardDevice keyboard_device;
  protected MouseSource mouse_device;
  protected SpriteOld spriteOld;
  protected Rectangle rect;
  protected int clip_y;
  protected boolean clicked;
  protected boolean enabled;
  protected boolean hover;
  protected boolean just_clicked;
  protected boolean just_hit;
  protected Alarm alarm;
  protected boolean toggle;
  protected boolean was_enabled;
  protected boolean center;
  
  public boolean hidden;
  public boolean selected;
  public boolean play_sounds;

  //FIXME protected Sound sound_hover;
  //FIXME protected Sound sound_clicked;

  public void setPos(int x, int y) {
    if (center) {
      rect.setX((float) (x - spriteOld.getWidth()/2));
      rect.setY((float) (y - spriteOld.getHeight()/8));
    }
    else {
      rect.setX((float) x);
      rect.setY((float) y);
    }
  }
  
  public Button(KeyboardDevice keyboard_device, MouseSource mouse_device, SpriteOld spriteOld) {
    this.keyboard_device = keyboard_device;
    this.mouse_device = mouse_device;
    this.spriteOld = spriteOld;
    clip_y = 0;
    clicked = false;
    hover = false;
    just_clicked = false;
    just_hit = false;
    toggle = false;
    was_enabled = true;
    selected = false;
    play_sounds = true;
    center = false;
    
    subject = new Observations(CLICKED);
    rect = new Rectangle(0, 0, (float) spriteOld.getWidth(), (float) spriteOld.getHeight()/4);
    enable(true);

    try {
      keyboard_device.connect(KeyboardDevice.KEYDOWN, this, Button.class.getDeclaredMethod("handleKeyDown", Event.class));
      mouse_device.connect(MouseSource.MOUSEDOWN, this, Button.class.getDeclaredMethod("handleMouseDown", Event.class));
      mouse_device.connect(MouseSource.MOUSEUP, this, Button.class.getDeclaredMethod("handleMouseUp", Event.class));
      
      alarm = new Alarm();
      alarm.connect(Alarm.TRRRIMM, this, Button.class.getDeclaredMethod("handleTimerDone", Event.class));
    } catch (NoSuchMethodException e1) {
    } catch (SecurityException e1) {
    }
  }
  
  public void delete() {
    keyboard_device.disconnect(this);
    mouse_device.disconnect(this);
  }

  public void update() {
    if (!enabled)
      return;
    
    try {
      alarm.update();
    } catch (Exception e) {
    }
    
    // update
    if (clicked) {
      clicked = false;
      clip_y = 2*spriteOld.getHeight()/4;
      hover = false;
    }
    else if (!mouse_device.isMouseInside(rect)) {
      clip_y = 0;
      hover = false;
      just_hit = false;
    }
    else if (!mouse_device.isMousePressed(MouseSource.LEFT_BUTTON)) {
      clip_y = spriteOld.getHeight()/4;
      
      // play sound
      /*FIXME if ((sound_hover) && (!hover)) {
        if (!just_clicked) {
          if (play_sounds)
            sound_hover->play(1);
        }
        else
          just_clicked = false;
      }*/
      
      hover = true;
      just_hit = false;
    }
    else if (mouse_device.isMouseDownInside(rect)) {
      clip_y = 2*spriteOld.getHeight()/4;
      hover = false;
    }
    else {
      clip_y = 0;
      hover = false;
      just_hit = false;
    }
    
    // if selected and mouse not inside, clip hover
    if (selected) {
      if (alarm.time() != 0) {
        toggle = (!toggle);
        if (toggle)
          clip_y = spriteOld.getHeight()/4;
        else
          clip_y = 2*spriteOld.getHeight()/4;
      }
      else if (!mouse_device.isMouseInside(rect))
        clip_y = spriteOld.getHeight()/4;
    }
    
    spriteOld.clip(0, clip_y, spriteOld.getWidth(), spriteOld.getHeight()/4);
  }
  
  public void render() {
    spriteOld.render((int) rect.getX(), (int) rect.getY(), false);
  }

  public void renderByCenter(boolean center) {
    if (center && !this.center) {
      rect.setX((float) (rect.getX() - spriteOld.getWidth()/2));
      rect.setY((float) (rect.getY() - spriteOld.getHeight()/8));
    }
    else if (!center && this.center) {
      rect.setX((float) (rect.getX() + spriteOld.getWidth()/2));
      rect.setY((float) (rect.getY() + spriteOld.getHeight()/8));
    }
    this.center = center;
  }
  
  public void enable(boolean enable) {
    enabled = enable;
    if (!enable)
      spriteOld.clip(0, 3*spriteOld.getHeight()/4, spriteOld.getWidth(), spriteOld.getHeight()/4);
  }
  
  public boolean isEnabled() {
    return enabled;
  }
  
  protected void handleKeyDown(Event event) {
    if (((KeyDownEvent) event).getUnicodeChar() == java.awt.event.KeyEvent.VK_ENTER) {
      if ((selected) && (enabled)) {
        alarm.start(ENTER_DELAY);
        
        // play sound
        /*FIXME if ((sound_clicked) && (play_sounds))
          sound_clicked->play(1);*/
      }
    }
  }
  
  protected void handleMouseDown(Event event) {
    if (((MouseDownEvent) event).getButton() != MouseSource.LEFT_BUTTON)
      return;
    
    if (!mouse_device.isMouseDownInside(rect))
      return;
    
    was_enabled = true;
    
    if (!enabled) {
      was_enabled = false;
      return;
    }
    
    clicked = true;
    
    // play sound
    /*FIXME if (sound_clicked) {
      if (play_sounds)
        sound_clicked->play(1);
      just_hit = true;
    }*/
  }
  
  protected void handleMouseUp(Event event) {
    if (
      ((MouseUpEvent) event).getButton() == MouseSource.LEFT_BUTTON &&
      mouse_device.isMouseDownInside(rect) &&
      mouse_device.isMouseInside(rect) &&
      enabled &&
      was_enabled
    ) {
      clicked = true;
      just_clicked = true;
      
      // play sound
      /*FIXME if ((sound_clicked) && (!just_hit) && (play_sounds))
        sound_clicked->play(1);*/
      
      subject.broadcast(CLICKED);
    }
  }
  
  protected void handleTimerDone(Event event) {
    subject.broadcast(CLICKED);
  }
  
  protected Observations subject;
  
  public static final String CLICKED = "CLICKED";
  
  public void connect(String event_type, Method handler) {
    subject.connect(event_type, handler);
  }

  public void connect(String event_type, Object observer, Method handler) {
    subject.connect(event_type, observer, handler);
  }

  public void disconnect(Method handler) {
    subject.disconnect(handler);
  }

  public void disconnect(String event_type, Method handler) {
    subject.disconnect(event_type, handler);
  }

  public void disconnect(Object observer) {
    subject.disconnect(observer);
  }

  public void disconnect(String event_type, Object observer) {
    subject.disconnect(event_type, observer);
  }
}
