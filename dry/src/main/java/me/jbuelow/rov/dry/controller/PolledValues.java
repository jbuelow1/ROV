package me.jbuelow.rov.dry.controller;

import net.java.games.input.Component.Identifier.Axis;
import net.java.games.input.Controller;

public class PolledValues {

  public int x = 0;
  public int y = 0;
  public int z = 0;
  public int t = 0;
  public float hat = 0;
  public String hatS = "REEE";

  private String[] directions = {"NW", "N", "NE", "E", "SE", "S", "SW", "W"};
  private int joyPrecision = 10000;

  public PolledValues(Controller controller) {
    Controller c = controller;
    try {
      x = (int) (c.getComponent(Axis.X).getPollData() * joyPrecision);
    } catch (NullPointerException ignored) {
    }
    try {
      y = (int) (c.getComponent(Axis.Y).getPollData() * joyPrecision);
    } catch (NullPointerException ignored) {
    }
    try {
      z = (int) (c.getComponent(Axis.RZ).getPollData() * joyPrecision);
    } catch (NullPointerException ignored) {
    }
    try {
      t = (int) (c.getComponent(Axis.SLIDER).getPollData() * joyPrecision);
    } catch (NullPointerException ignored) {
    }
    try {
      hat = c.getComponent(Axis.POV).getPollData();
    } catch (NullPointerException ignored) {
    }
    resolveHatPosition();
  }

  public PolledValues(int x, int y, int z, int t, float hat) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.t = t;
    this.hat = hat;
    resolveHatPosition();
  }

  private void resolveHatPosition() {
    float v = hat * 8;
    if (v != 0f) {
      hatS = directions[(int) v - 1];
    } else {
      hatS = "YEET";
    }
  }

  public int[] getArray() {
    return new int[]{x, y, z};
  }
}
