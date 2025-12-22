package com.contestpredictor.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Contest {
    private String contestId;
    private String contestName;
    private LocalDateTime dateTime;
    private int duration; // in minutes
    private boolean isPast;
    private List<Participant> participants;

    public Contest(String contestId, String contestName, LocalDateTime dateTime, int duration, boolean isPast) {
        this.contestId = contestId;
        this.contestName = contestName;
        this.dateTime = dateTime;
        this.duration = duration;
        this.isPast = isPast;
        this.participants = new ArrayList<>();
    }

    public void addParticipant(Participant participant) {
        this.participants.add(participant);
    }

    // Getters and Setters
    public String getContestId() {
        return contestId;
    }

    public void setContestId(String contestId) {
        this.contestId = contestId;
    }

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isPast() {
        return isPast;
    }

    public void setPast(boolean past) {
        isPast = past;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }
}
