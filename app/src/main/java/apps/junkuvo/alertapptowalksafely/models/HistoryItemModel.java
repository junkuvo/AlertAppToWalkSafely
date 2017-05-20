package apps.junkuvo.alertapptowalksafely.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class HistoryItemModel extends RealmObject {

    @PrimaryKey
    private long id;
    @Index
    @Required
    private Date startDateTime;
    @Index
    private Date endDateTime;
    @Required
    private String stepCountAlert;
    @Required
    private String stepCount;

    private String memo;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getStepCount() {
        return stepCount;
    }

    public void setStepCount(String stepCount) {
        this.stepCount = stepCount;
    }

    public String getStepCountAlert() {
        return stepCountAlert;
    }

    public void setStepCountAlert(String stepCountAlert) {
        this.stepCountAlert = stepCountAlert;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
