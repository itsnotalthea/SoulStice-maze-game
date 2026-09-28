package com.soulstice.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/*
    This class is responsible for:
        Polling keyboard state for movement input.
        Since there is no diagonal movement, if multiple keys are pressed,
            priority order below picks a single direction.
*/

public class InputHandler {


     // This Returns the direction pressed this check or null if no relevant key is currently down.
    public Direction pollDirection() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            return Direction.UP;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            return Direction.DOWN;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            return Direction.LEFT;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            return Direction.RIGHT;
        }
        return null;
    }
}
