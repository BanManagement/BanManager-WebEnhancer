package me.confuser.banmanager.webenhancer.common.data;

import me.confuser.banmanager.common.ormlite.field.DataType;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

  @DatabaseField(columnDefinition = "CHAR(64)")
  @Getter
  @Setter
  private String messageHash;

  LogData() {
  }

  public LogData(String message, long created) {
    this.message = message;
    this.created = created;
    this.messageHash = computeHash(message);
  }

  public static String computeHash(String message) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
