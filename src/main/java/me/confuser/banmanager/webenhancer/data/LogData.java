package me.confuser.banmanager.webenhancer.data;

import java.util.concurrent.TimeUnit;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@DatabaseTable
@NoArgsConstructor
public class LogData {

    @Setter
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, dataType = DataType.LONG_STRING)
    private String message;

    @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
    private long created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

    public LogData(final String message, final long created) {
        this.message = message;
        this.created = created;
    }
}
