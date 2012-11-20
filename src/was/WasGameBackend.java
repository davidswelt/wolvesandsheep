package was;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;

/**
 *
 * @author dr
 */
interface WasGameBackend {

    public void addActor(Actor actor,
            Location location);

    public void doRun();

    public void show();

    public void hide();

    public boolean removeActor(Actor actor);
}
