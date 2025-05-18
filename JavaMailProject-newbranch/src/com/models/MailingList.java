package com.models;
import java.util.List;
import java.util.ArrayList;

public class MailingList {
    private String name;
    private List<String> members;

    public MailingList() {
        this.members = new ArrayList<>();
    }

    public MailingList(String name, List<String> members) {
        this.name = name;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMember(String email) {
        if (!members.contains(email)) {
            members.add(email);
        }
    }

    public void removeMember(String email) {
        members.remove(email);
    }
} 