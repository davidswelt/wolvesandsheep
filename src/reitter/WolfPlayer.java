/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reitter;

import was.Move;

/**
 *
 * @author dr
 */
public class WolfPlayer extends was.WolfPlayer {

    @Override
    public Move move() {
        return new Move(-1,0);
    }
    
}
