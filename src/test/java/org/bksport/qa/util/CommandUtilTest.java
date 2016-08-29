package org.bksport.qa.util;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.bksport.qa.util.CommandUtil;
import org.junit.Test;

public class CommandUtilTest {

  @Test
  public void test() {
    assertEquals(
        CommandUtil.truncate("Give me latest result of Barcenola - MU"),
        "latest result of Barcenola - MU");
    assertEquals(
        CommandUtil
            .truncate("Show me how Lionel Messi \t made a hattrick yesterday"),
        "how Lionel Messi \t made a hattrick yesterday");
    System.out.println(StringUtils.getLevenshteinDistance("Lionel Messi",
        "Lionen Messi"));
  }

}
