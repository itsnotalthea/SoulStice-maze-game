package com.soulstice.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/*
    This class is responsible for:
        Polling keyboard state for movement input.

    Movement is continuous: while a direction key is held down, that direction
    keeps being reported every frame, so BODY (and SOUL) keep stepping until
    the key is released or a wall blocks them.

    A single tap still results in a single step, because MazeScreen only starts
    a new move once the previous move animation has finished. Each reported
    direction therefore still equals exactly one grid step guarded by the exact
    same wall-collision / win checks as before.

    If several direction keys are held at once, the most recently pressed one
    wins; otherwise we fall back to the priority order UP, DOWN, LEFT, RIGHT.
    Pressing a new key while another is held lets you turn immediately.
*/
public class InputHandler {

    private Direction lastPressed = null;

    // Returns the direction to move this frame, or null if no movement key is held.
    public Direction pollDirection() {
        if (isJustPressed(Direction.UP))    lastPressed = Direction.UP;
        if (isJustPressed(Direction.DOWN))  lastPressed = Direction.DOWN;
        if (isJustPressed(Direction.LEFT))  lastPressed = Direction.LEFT;
        if (isJustPressed(Direction.RIGHT)) lastPressed = Direction.RIGHT;

        // Keep walking in the most recently chosen direction while it is held.
        if (lastPressed != null && isHeld(lastPressed)) {
            return lastPressed;
        }

        // Otherwise use any other held key (priority order).
        if (isHeld(Direction.UP))    return Direction.UP;
        if (isHeld(Direction.DOWN))  return Direction.DOWN;
        if (isHeld(Direction.LEFT))  return Direction.LEFT;
        if (isHeld(Direction.RIGHT)) return Direction.RIGHT;

        lastPressed = null;
        return null;
    }

    private static boolean isJustPressed(Direction direction) {
        switch (direction) {
            case UP:    return Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W);
            case DOWN:  return Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S);
            case LEFT:  return Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A);
            case RIGHT: return Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D);
            default:    return false;
        }
    }

    private static boolean isHeld(Direction direction) {
        switch (direction) {
            case UP:    return Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
            case DOWN:  return Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
            case LEFT:  return Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
            case RIGHT: return Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
            default:    return false;
        }
    }
}
