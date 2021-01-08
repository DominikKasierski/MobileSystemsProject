package pl.ks.dk.covidapp.Model;

public class User {
    private String id;
    private String name;
    private String surname;
    private String username;
    private String pesel;
    private String phoneNumber;
    private String dateOfBirth;
    private String imageURL;
    private String status;
    private String search;
    private String role;
    private String waitingForDiagnosis;


    public User(String id, String name, String surname, String username, String pesel, String phoneNumber, String dateOfBirth, String imageURL, String status, String search, String role, String waitingForDiagnosis) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.pesel = pesel;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
        this.role = role;
        this.waitingForDiagnosis = waitingForDiagnosis;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getRole() {
        return role;
    }

    public String getWaitingForDiagnosis() {
        return waitingForDiagnosis;
    }

    public void setWaitingForDiagnosis(String waitingForDiagnosis) {
        this.waitingForDiagnosis = waitingForDiagnosis;
    }
}
