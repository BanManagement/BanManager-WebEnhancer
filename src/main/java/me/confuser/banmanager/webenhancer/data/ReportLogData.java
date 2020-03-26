package me.confuser.banmanager.webenhancer.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.confuser.banmanager.common.data.PlayerReportData;

@Getter
@DatabaseTable
@NoArgsConstructor
@RequiredArgsConstructor
public class ReportLogData {

  @Setter
  @DatabaseField(generatedId = true)
  private int id;

  @NonNull
  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true)
  private PlayerReportData report;

  @NonNull
  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true)
  private LogData log;
}
