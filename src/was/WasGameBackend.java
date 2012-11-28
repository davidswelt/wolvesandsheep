package was;

import ch.aplu.jgamegrid.Actor;

/**
 * This interface is used to update the display of the game board.
 * It is implemented by two classes - WasBlankGame (which does not visualize anything(,
 * and WasVideoGame, which uses the JGameGrid package for visualization.
 * @author dr
 */
interface WasGameBackend {

    public void addActor(Actor actor,
            GameLocation location);

    public void doRun();

    public void show();

    public void hide();
    public void doPause();

    public boolean removeActor(Actor actor);
}
