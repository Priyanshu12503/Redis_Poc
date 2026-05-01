package com.ttn.redish.common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class HeavyPayload implements Serializable {

    private Profile profile;
    private List<Activity> activities;
    private List<Address> addresses;
    private Map<String, String> preferences;
    private Map<String, List<Metric>> metrics;
    private List<List<String>> tagsGrid;

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public Map<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }

    public Map<String, List<Metric>> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, List<Metric>> metrics) {
        this.metrics = metrics;
    }

    public List<List<String>> getTagsGrid() {
        return tagsGrid;
    }

    public void setTagsGrid(List<List<String>> tagsGrid) {
        this.tagsGrid = tagsGrid;
    }

    public static class Profile implements Serializable {
        private String summary;
        private List<String> skills;
        private List<WorkHistory> history;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<String> getSkills() {
            return skills;
        }

        public void setSkills(List<String> skills) {
            this.skills = skills;
        }

        public List<WorkHistory> getHistory() {
            return history;
        }

        public void setHistory(List<WorkHistory> history) {
            this.history = history;
        }
    }

    public static class WorkHistory implements Serializable {
        private String company;
        private String role;
        private Integer years;
        private String notes;

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Integer getYears() {
            return years;
        }

        public void setYears(Integer years) {
            this.years = years;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class Activity implements Serializable {
        private String type;
        private Long timestamp;
        private Map<String, String> attributes;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }
    }

    public static class Address implements Serializable {
        private String city;
        private String country;
        private List<String> lines;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public List<String> getLines() {
            return lines;
        }

        public void setLines(List<String> lines) {
            this.lines = lines;
        }
    }

    public static class Metric implements Serializable {
        private String name;
        private Double value;
        private List<Integer> samples;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public List<Integer> getSamples() {
            return samples;
        }

        public void setSamples(List<Integer> samples) {
            this.samples = samples;
        }
    }
}
