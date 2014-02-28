package org.unbiquitous.ubiengine.engine.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListResourceBundle;

import org.unbiquitous.ubiengine.engine.system.io.InputManager;
import org.unbiquitous.ubiengine.engine.system.io.KeyboardReceptionDriver;
import org.unbiquitous.ubiengine.engine.system.io.OutputManager;
import org.unbiquitous.ubiengine.engine.system.time.DeltaTime;
import org.unbiquitous.ubiengine.util.Logger;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.UosApplication;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDeploy;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyStart;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyUndeploy;
import org.unbiquitous.uos.network.socket.connectionManager.TCPConnectionManager;
import org.unbiquitous.uos.network.socket.radar.PingRadar;

/**
 * The game class. Extend it only to implement getSettings().
 * @author Pimenta
 *
 */
public abstract class UbiGame implements UosApplication {
  /**
   * Must be implemented by the game class.
   * @return Reference to the game initial settings.
   */
  protected abstract GameSettings getSettings();
  
  /**
   * Use this method in main() to start the game.
   * @param game Class{@literal <}?{@literal >} that extends UosGame.
   */
  protected static void run(final Class<? extends UbiGame> game) {
    new UOS().init(new ListResourceBundle() {
      protected Object[][] getContents() {
        return new Object[][] {
          {"ubiquitos.connectionManager", TCPConnectionManager.class.getName()},
          {"ubiquitos.radar", PingRadar.class.getName()},
          {"ubiquitos.eth.tcp.port", "14984"},
          {"ubiquitos.eth.tcp.passivePortRange", "14985-15000"},
          //{"ubiquitos.uos.deviceName","compDevice"},FIXME
          {"ubiquitos.driver.deploylist", KeyboardReceptionDriver.class.getName()},
          {"ubiquitos.application.deploylist", game.getName()}
        };
      }
    });
  }
  
  /**
   * Call to change the current game state.
   * @param state New game state.
   */
  public void change(GameState state) {
    if (state == null)
      return;
    state_change = state;
    change_option = ChangeOption.CHANGE;
  }
  
  /**
   * Call to push a game state.
   * @param state Game state to be pushed.
   */
  public void push(GameState state) {
    if (state == null)
      return;
    state_change = state;
    change_option = ChangeOption.PUSH;
  }
  
  /**
   * Call to pop the current game state.
   * @param args Arguments to be passed to the new current game state.
   */
  public void pop(Object... args) {
    pop_args = args;
    change_option = ChangeOption.POP;
  }
  
  /**
   * Call to shutdown.
   */
  public void quit() {
    change_option = ChangeOption.QUIT;
  }
//==============================================================================
//nothings else matters from here to below
//==============================================================================
  private GameSettings settings;
  private LinkedList<GameState> states = new LinkedList<GameState>();
  private List<InputManager> inputs = new ArrayList<InputManager>();
  private List<OutputManager> outputs = new ArrayList<OutputManager>();
  private DeltaTime deltatime = new DeltaTime();
  
  private enum ChangeOption {
    NA, CHANGE, PUSH, POP, QUIT
  }
  
  private GameState state_change = null;
  private Object[] pop_args = null;
  private ChangeOption change_option = ChangeOption.NA;
  
  /**
   * uOS's private use.
   */
  public void start(Gateway gateway, OntologyStart ontology) {
    try {
      init(gateway);
      while (states.size() > 0) {
        deltatime.update();
        for (InputManager im : inputs)
          im.update();
        for (GameState gs : states)
          gs.update();
        for (GameState gs : states)
          gs.render();
        for (OutputManager om : outputs)
          om.update();
        updateStack();
        deltatime.sync();
      }
      close();
    } catch (Error e) {
      String root_path;
      try {
        root_path = (String)settings.get("root_path");
      } catch (Exception e1) {
        root_path = ".";
        Logger.log(new Error("Root path not reachable"), root_path + "/ErrorLog.txt");
      }
      Logger.log(e, root_path + "/ErrorLog.txt");
      throw e;
    }
  }
  
  /**
   * uOS's private use.
   */
  public void stop() {
    
  }
  
  /**
   * uOS's private use.
   */
  public void init(OntologyDeploy ontology, String appId) {
    
  }
  
  /**
   * uOS's private use.
   */
  public void tearDown(OntologyUndeploy ontology) {
    
  }
  
  @SuppressWarnings("unchecked")
  private void init(Gateway gateway) {
    GameComponents.put(GameSettings.class, settings = getSettings().validate());
    GameComponents.put(UbiGame.class, this);
    GameComponents.put(Gateway.class, gateway);
    GameComponents.put(DeltaTime.class, deltatime);
    
    try {
      List<Class<?>> ims = (List<Class<?>>)settings.get("input_managers");
      if (ims != null) {
        for (Class<?> imc : ims) {
          InputManager im = (InputManager)imc.newInstance();
          GameComponents.put(imc, im);
          inputs.add(im);
        }
      }
      
      List<Class<?>> oms = (List<Class<?>>)settings.get("output_managers");
      if (oms != null) {
        for (Class<?> omc : oms) {
          OutputManager om = (OutputManager)omc.newInstance();
          GameComponents.put(omc, om);
          outputs.add(om);
        }
      }
      
      states.add(((GameState)((Class<?>)settings.get("first_state")).newInstance()));
    } catch (Exception e) {
      throw new Error(e);
    }
  }
  
  private void close() {
    for (InputManager im : inputs)
      im.close();
    for (OutputManager om : outputs)
      om.close();
  }
  
  private void updateStack() {
    switch (change_option) {
      case NA:
        break;
        
      case CHANGE:
        states.removeLast().close();
        states.add(state_change);
        break;
        
      case PUSH:
        states.add(state_change);
        break;
        
      case POP:
        states.removeLast().close();
        if (states.size() > 0)
          states.getLast().wakeup(pop_args);
        break;
        
      case QUIT:
        states.clear();
        break;
        
      default:
        throw new Error("Invalid value for ChangeOption in UosGame!");
    }
    state_change = null;
    pop_args = null;
    change_option = ChangeOption.NA;
  }
}
