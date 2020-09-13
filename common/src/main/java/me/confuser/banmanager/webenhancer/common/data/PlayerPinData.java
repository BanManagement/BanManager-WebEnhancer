package me.confuser.banmanager.webenhancer.common.data;

import lombok.Setter;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@DatabaseTable
public class PlayerPinData {

  private static Argon2 argon2 = Argon2Factory.create();

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, uniqueIndex = false, persisterClass =
      ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;

  @DatabaseField(index = true, canBeNull = false, columnDefinition = "VARCHAR(255) NOT NULL")
  @Getter
  private String pin;

  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long expires;

  @Getter
  @Setter
  private int generatedPin;

  PlayerPinData() {

  }

  public PlayerPinData(PlayerData player) throws NoSuchAlgorithmException {
    this.player = player;

    this.generatedPin = SecureRandom.getInstance("SHA1PRNG").nextInt(900000) + 100000;

    this.pin = argon2.hash(3, 4096, 1, String.valueOf(this.generatedPin));
    this.expires = (System.currentTimeMillis() / 1000L) + 300; // Valid for 5 minutes
  }

}
