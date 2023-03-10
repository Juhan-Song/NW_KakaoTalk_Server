package User;

public class User {
    private String name;
    private String gender;
    private String nickName;
    private String birth;
    private String phone;
    private String email;
    private String message;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }

    public void setGender(String gender) { this.gender = gender; }

    public String getNickName() { return nickName; }

    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getBirth() { return birth; }

    public void setBirth(String birth) { this.birth = birth; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }
}
