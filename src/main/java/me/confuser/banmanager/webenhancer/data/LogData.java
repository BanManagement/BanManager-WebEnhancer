package me.confuser.banmanager.webenhancer.data;

import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.internal.ormlite.field.DataType;
import me.confuser.banmanager.internal.ormlite.field.DatabaseField;
import me.confuser.banmanager.internal.ormlite.table.DatabaseTable;

@DatabaseTable
public class LogData {

  @Getter
  @Setter
  @DatabaseField(generatedId = true)
  private int id;

  @Getter
  @DatabaseField(canBeNull = false, dataType = DataType.LONG_STRING)
  private String message;

  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  LogData() {
  }

  public LogData(String message, long created) {
    this.message = message;
    this.created = created;
  }
}
