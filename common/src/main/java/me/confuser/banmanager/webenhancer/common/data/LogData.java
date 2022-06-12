package me.confuser.banmanager.webenhancer.common.data;

import me.confuser.banmanager.common.ormlite.field.DataType;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;

@DatabaseTable
public class LogData {

  @Getter
  @Setter
  @DatabaseField(generatedId = true)
  private int id;

  @Getter
  @DatabaseField(canBeNull = false, dataType = DataType.LONG_STRING)
  private String message;

  @DatabaseField(index = true, canBeNull = false, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  LogData() {
  }

  public LogData(String message, long created) {
    this.message = message;
    this.created = created;
  }
}
