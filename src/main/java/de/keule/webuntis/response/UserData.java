package de.keule.webuntis.response;

import org.json.JSONObject;
import java.time.LocalDateTime;
import java.util.List;

public class UserData {
    private JSONObject json;
    private SchoolYear schoolYear;
    private User user;

    public UserData(JSONObject json) {
        this.json = json;
        JSONObject schoolYearJson = json.getJSONObject("schoolYear");
        JSONObject dateRangeJson = schoolYearJson.getJSONObject("dateRange");
        this.schoolYear = new SchoolYear(
                new DateRange(LocalDateTime.parse(dateRangeJson.getString("start")), LocalDateTime.parse(dateRangeJson.getString("end"))),
                schoolYearJson.getInt("id"),
                schoolYearJson.getString("name")
        );

        JSONObject userJson = json.getJSONObject("user");
        JSONObject personJson = userJson.getJSONObject("person");
        this.user = new User(
                userJson.getInt("id"),
                userJson.getString("username"),
                new Person(personJson.getInt("id"), personJson.getString("displayName"), personJson.getString("imageUrl")),
                userJson.getJSONArray("referencedStudents").toList(),
                userJson.getString("locale"),
                userJson.getInt("departmentId"),
                userJson.getString("role"),
                userJson.getJSONArray("permissions").toList()
        );
    }

    public JSONObject getJson() {
        return json;
    }

    public User getUser() {
        return user;
    }

    public SchoolYear getSchoolYear() {
        return schoolYear;
    }

    public static class SchoolYear {
        private DateRange dateRange;
        private int id;
        private String name;

        public SchoolYear(DateRange dateRange, int id, String name) {
            this.dateRange = dateRange;
            this.id = id;
            this.name = name;
        }
    }
    public static class DateRange {
        private LocalDateTime start;
        private LocalDateTime end;

        public DateRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }
    public static class User {
        private final int id;
        private final String username;
        private final Person person;
        private final List<Object> referencedStudents;
        private final String locale;
        private final int departmentId;
        private final String role;
        private final List<Object> permissions;

        public User(int id, String username, Person person, List<Object> referencedStudents, String locale, int departmentId, String role, List<Object> permissions) {
            this.id = id;
            this.username = username;
            this.person = person;
            this.referencedStudents = referencedStudents;
            this.locale = locale;
            this.departmentId = departmentId;
            this.role = role;
            this.permissions = permissions;
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public Person getPerson() {
            return person;
        }

        public List<Object> getReferencedStudents() {
            return referencedStudents;
        }

        public String getLocale() {
            return locale;
        }

        public int getDepartmentId() {
            return departmentId;
        }

        public String getRole() {
            return role;
        }

        public List<Object> getPermissions() {
            return permissions;
        }
    }
    public static class Person {
        private final int id;
        private final String displayName;
        private final String imageUrl;

        public Person(int id, String displayName, String imageUrl) {
            this.id = id;
            this.displayName = displayName;
            this.imageUrl = imageUrl;
        }

        public int getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}
