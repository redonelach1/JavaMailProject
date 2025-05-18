package com.emailclient;

import com.models.MailingList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MailingListManager {
    private static final String FILE_PATH = "mailing_lists.json";
    private List<MailingList> mailingLists;
    private static MailingListManager instance;

    private MailingListManager() {
        this.mailingLists = loadMailingLists();
    }

    public static MailingListManager getInstance() {
        if (instance == null) {
            instance = new MailingListManager();
        }
        return instance;
    }

    public List<MailingList> getMailingLists() {
        return new ArrayList<>(mailingLists);
    }

    public void addMailingList(MailingList list) {
        mailingLists.add(list);
        saveMailingLists();
    }

    public void removeMailingList(String name) {
        mailingLists.removeIf(list -> list.getName().equalsIgnoreCase(name));
        saveMailingLists();
    }

    public void updateMailingList(String oldName, MailingList updatedList) {
        removeMailingList(oldName);
        addMailingList(updatedList);
    }

    public MailingList getMailingListByName(String name) {
        return mailingLists.stream()
                .filter(list -> list.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private void saveMailingLists() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            new Gson().toJson(mailingLists, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<MailingList> loadMailingLists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<ArrayList<MailingList>>(){}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
} 