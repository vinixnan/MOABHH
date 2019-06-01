package br.usp.poli.pcs.lti.moabhh.core;

/**
 * The type Item.
 */
public class Item implements Comparable<Item> {

  private String name;
  private double quality;

  /**
   * Instantiates a new Item.
   *
   * @param name the name
   * @param quality the quality
   */
  public Item(String name, double quality) {
    this.name = name;
    this.quality = quality;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets quality.
   *
   * @return the quality
   */
  public double getQuality() {
    return quality;
  }

  /**
   * Sets quality.
   *
   * @param quality the quality
   */
  public void setQuality(double quality) {
    this.quality = quality;
  }

  @Override
  public int compareTo(Item o) {
    if (this.quality < o.quality) {
      return 1;
    }
    if (this.quality > o.quality) {
      return -1;
    }
    return 0;
  }
}
