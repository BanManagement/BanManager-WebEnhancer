package me.confuser.banmanager.webenhancer.data;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

@DatabaseTable
@NoArgsConstructor
@Getter
public class PlayerPinData {
    /** TODO: here we need better variable name */
    private static final int DEFAULT_EXPIRES_IN_MIN = 5;
    /** Using default page size for optimization reasons */
    private static final int HASHING_MEMORY_IN_KB = 4096;

    private static Argon2 argon2 = Argon2Factory.create();

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(
        index = true,
        canBeNull = false,
        foreign = true,
        foreignAutoRefresh = true,
        uniqueIndex = false,
        persisterClass = ByteArray.class,
        columnDefinition = "BINARY(16) NOT NULL"
    )
    private PlayerData player;

    @DatabaseField(index = true, canBeNull = false, columnDefinition = "VARCHAR(255) NOT NULL")
    private String pin;

    @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
    private long expires;

    private int generatedPin;

    public PlayerPinData(final PlayerData player) throws NoSuchAlgorithmException {
        this.player = player;

        generatedPin = SecureRandom.getInstance("SHA1PRNG").nextInt(900_000) + 100_000;

        pin = argon2.hash(
            3, /** 3 iterations of hashing algorithm */
            HASHING_MEMORY_IN_KB,
            1, /** single threaded hashing */
            String.valueOf(generatedPin)
        );
        // expires in 5 minutes from this moment
        expires = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                + TimeUnit.MINUTES.toSeconds(DEFAULT_EXPIRES_IN_MIN);
    }
}
