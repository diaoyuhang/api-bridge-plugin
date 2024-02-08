package com.itangcent.api.jaxrs;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * user info
 */
public class UserDTO {

    @QueryParam
    private Long id = 0;//user id

    /**
     * @see com.itangcent.constant.UserType
     */
    @QueryParam
    private int type;//user type

    /**
     * @default tangcent
     * @mock tangcent
     */
    @NotBlank
    @QueryParam
    private String name;//user name

    /**
     * user age
     *
     * @mock 1${digit}
     */
    @NotNull
    @QueryParam
    private Integer age;

    /**
     * @demo 1
     * @deprecated It's a secret
     */
    @QueryParam
    private Integer sex;

    //user birthDay
    @QueryParam
    private LocalDate birthDay;

    //user regtime
    @QueryParam
    private LocalDateTime regtime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public LocalDate getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(LocalDate birthDay) {
        this.birthDay = birthDay;
    }

    public LocalDateTime getRegtime() {
        return regtime;
    }

    public void setRegtime(LocalDateTime regtime) {
        this.regtime = regtime;
    }
}
