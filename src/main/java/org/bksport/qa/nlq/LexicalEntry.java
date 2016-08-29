package org.bksport.qa.nlq;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * represent a word/phrase in a sentence
 * 
 * @author congnh
 * 
 */
public class LexicalEntry {

  /**
   * position of current lexical entry in sentence evaluate by number of
   * previous lexical entries in sentence
   */
  private int                 position;
  /**
   * string value of current lexical entry
   */
  private String              value;
  /**
   * POS tagger value of current lexical entry
   */
  private String              tagName;
  /**
   * possible SPARQL/Ontology entries represent current lexical entry
   */
  private List<ResourceEntry> slotEntryList;

  public LexicalEntry() {
    slotEntryList = new ArrayList<ResourceEntry>();
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  /*
   * Thêm một ResourceEntry vao slotEntryList
   * */
  public void addSlotEntry(ResourceEntry slotEntry) {
    slotEntryList.add(slotEntry);
  }

  public int getPosition() {
    return position;
  }

  public String getValue() {
    return value;
  }

  public String getTagName() {
    return tagName;
  }

  public ResourceEntry getSlotEntry(int index) {
    return slotEntryList.get(index);
  }

  public int getNumOfSlotEntry() {
    return slotEntryList.size();
  }

  public boolean isEmptySlotEntry() {
    return slotEntryList.isEmpty();
  }

  @Override
  public String toString() 
  {
    String slList = "";
    for (int i = 0; i < getNumOfSlotEntry(); i++) 
    {
      slList += "(" + getSlotEntry(i) + ")";
      if (i < getNumOfSlotEntry() - 1)
        slList += ",";
    }
    return value + "-" + position + "[" + tagName + "]:" + slList;
  }
}
