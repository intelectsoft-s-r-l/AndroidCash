package edi.md.androidcash.RealmHelper;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by Igor on 28.10.2019
 */

public class Shift extends RealmObject {
    @Required
    private String id;
    private long startDate;
    private long endDate;
    @Required
    private String workPlaceId;
    private String workPlaceName;
    @Required
    private String author;
    private String authorName;
    private String name;
    private String closedBy;
    private String closedByName;
    private boolean closed;
    private boolean isSended;
    private long needClose;
    private int billCounter;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getWorkPlaceId() {
        return workPlaceId;
    }

    public void setWorkPlaceId(String workPlaceId) {
        this.workPlaceId = workPlaceId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isSended() {
        return isSended;
    }

    public void setSended(boolean sended) {
        isSended = sended;
    }

    public long getNeedClose() {
        return needClose;
    }

    public void setNeedClose(long needClose) {
        this.needClose = needClose;
    }

    public int getBillCounter() {
        return billCounter;
    }

    public void setBillCounter(int billCounter) {
        this.billCounter = billCounter;
    }

    public String getWorkPlaceName() {
        return workPlaceName;
    }

    public void setWorkPlaceName(String workPlaceName) {
        this.workPlaceName = workPlaceName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getClosedByName() {
        return closedByName;
    }

    public void setClosedByName(String closedByName) {
        this.closedByName = closedByName;
    }
}
