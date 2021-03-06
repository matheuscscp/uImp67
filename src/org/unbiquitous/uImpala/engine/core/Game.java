package org.unbiquitous.uImpala.engine.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.unbiquitous.uImpala.engine.io.InputManager;
import org.unbiquitous.uImpala.engine.io.OutputManager;
import org.unbiquitous.uImpala.engine.time.DeltaTime;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.UosApplication;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDeploy;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyStart;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyUndeploy;

/**
 * The game class. Extend it only to implement getSettings().
 * @author Pimenta
 *
 */
public abstract class Game implements UosApplication {
	private static final java.util.logging.Logger LOGGER = UOSLogging.getLogger();

	/**
	 * Use this method in main() to start the game.
	 * 
	 * @param game
	 *            Class{@literal <}?{@literal >} that extends UosGame.
	 * @param args
	 *            Command line arguments.
	 */
  protected static void run(final Class<? extends UosApplication> gameClass, final GameSettings settings) {
	  String gameId = settings.getString("game_id");
	  settings.addApplication(gameClass,gameId);
	  if (settings.get("root_path") == null)
	      settings.put("root_path", ".");
	  new UOS().start(settings);
  }

	/**
	 * Method to initialize an engine implementation.
	 */
	protected abstract void initImpl();

	/**
	 * Call to change the current game scene.
	 * 
	 * @param scene
	 *            New game scene.
	 */
	public void change(GameScene scene) {
		if (scene == null)
			return;
		scene_change = scene;
		change_option = ChangeOption.CHANGE;
	}

	/**
	 * Call to push a game scene.
	 * 
	 * @param scene
	 *            Game scene to be pushed.
	 */
	public void push(GameScene scene) {
		if (scene == null)
			return;
		scene_change = scene;
		change_option = ChangeOption.PUSH;
	}

	/**
	 * Call to pop the current game scene.
	 * 
	 * @param args
	 *            Arguments to be passed to the new current game scene.
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

	// ==============================================================================
	// nothings else matters from here to below
	// ==============================================================================
	protected GameSettings settings;
	protected LinkedList<GameScene> scenes = new LinkedList<GameScene>();
	protected List<InputManager> inputs = new ArrayList<InputManager>();
	protected List<OutputManager> outputs = new ArrayList<OutputManager>();
	protected DeltaTime deltatime;

	private enum ChangeOption {
		NA, CHANGE, PUSH, POP, QUIT
	}

	private GameScene scene_change = null;
	private Object[] pop_args = null;
	private ChangeOption change_option = ChangeOption.NA;

	/**
	 * uOS's private use.
	 */
	public void start(Gateway gateway, OntologyStart ontology) {
		try {
			init(gateway);
			while (scenes.size() > 0) {
				update();
			}
			close();
		} catch (Error e) {
			LOGGER.log(Level.SEVERE,"Problems while running game.",e);
		}
		System.exit(0);
	}

	protected void update() {
		deltatime.update();
		updateInputs();
		updateScenes();
		deltatime.accumulate();
		renderScenes();
		updateOutput();
		updateStack();
	}

	protected void updateOutput() {
		for (OutputManager om : outputs)
			om.update();
	}

	protected void renderScenes() {
		for (GameScene gs : scenes) {
			if (!gs.isFrozen() || (gs.isFrozen() && gs.isVisible()))
				gs.render();
		}
	}

	protected void updateScenes() {
//		while (deltatime.dtReachedLimit()) {
		deltatime.sleepDt();
			for (GameScene gs : scenes) {
				if (!gs.isFrozen())
					gs.update();
			}
//		}
	}

	protected void updateInputs() {
		for (InputManager im : inputs)
			im.update();
	}

	/**
	 * uOS's private use.
	 */
	public void stop() {

	}

	/**
	 * uOS's private use.
	 */
	public void init(OntologyDeploy knowledgeBase,
			InitialProperties properties, String appId) {
		//TODO: why not just the use InitialProperties for this task?
		settings = (GameSettings) properties.get("uImpala.gameSettings");
	}

	/**
	 * uOS's private use.
	 */
	public void tearDown(OntologyUndeploy ontology) {

	}

	@SuppressWarnings("unchecked")
	protected void init(Gateway gateway) {
		initImpl();

		validateSettings();
		GameSingletons.put(GameSettings.class, settings);
		GameSingletons.put(Game.class, this);
		GameSingletons.put(Gateway.class, gateway);
		GameSingletons.put(DeltaTime.class, deltatime = new DeltaTime());

		try {
			List<Class<?>> ims = (List<Class<?>>) settings
					.get("input_managers");
			if (ims != null) {
				for (Class<?> imc : ims) {
					InputManager im = (InputManager) imc.newInstance();
					GameSingletons.put(imc, im);
					inputs.add(im);
				}
			}

			List<Class<?>> oms = (List<Class<?>>) settings
					.get("output_managers");
			if (oms != null) {
				for (Class<?> omc : oms) {
					OutputManager om = (OutputManager) omc.newInstance();
					GameSingletons.put(omc, om);
					outputs.add(om);
				}
			}

			scenes.add(((GameScene) ((Class<?>) settings.get("first_scene"))
					.newInstance()));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private void validateSettings() {
		if (settings == null)
			throw new Error("GameSettings not defined!");
		if (settings.get("first_scene") == null)
			throw new Error("First game scene not defined!");
		if (settings.get("output_managers") == null)
			throw new Error("Cannot start game with no output managers!");
	}

	protected void close() {
		for (InputManager im : inputs)
			im.close();
		for (OutputManager om : outputs)
			om.close();
	}

	protected void updateStack() {
		GameScene tmp;
		switch (change_option) {
		case NA:
			break;

		case CHANGE:
			tmp = scenes.removeLast();
			tmp.assets().destroy();
			tmp.destroy();
			scenes.add(scene_change);
			break;

		case PUSH:
			scenes.add(scene_change);
			break;

		case POP:
			tmp = scenes.removeLast();
			tmp.assets().destroy();
			tmp.destroy();
			if (scenes.size() > 0)
				scenes.getLast().wakeup(pop_args);
			break;

		case QUIT:
			scenes.clear();
			break;

		default:
			throw new Error("Invalid value for ChangeOption in UosGame!");
		}
		scene_change = null;
		pop_args = null;
		change_option = ChangeOption.NA;
	}
}
