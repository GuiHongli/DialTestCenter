package com.huawei.cloududn.dialingtest.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * DialUser
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2025-09-18T16:05:08.624+08:00[Asia/Shanghai]")


public class DialUser   {
  @JsonProperty("id")
  private Integer id = null;

  @JsonProperty("username")
  private String username = null;

  @JsonProperty("password")
  private String password = null;

  @JsonProperty("lastLoginTime")
  private OffsetDateTime lastLoginTime = null;

  public DialUser id(Integer id) {
    this.id = id;
    return this;
  }

  /**
   * 用户ID
   * @return id
   **/
  @Schema(description = "用户ID")
  
    public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public DialUser username(String username) {
    this.username = username;
    return this;
  }

  /**
   * 用户名
   * @return username
   **/
  @Schema(description = "用户名")
  
    public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public DialUser password(String password) {
    this.password = password;
    return this;
  }

  /**
   * 密码（加密后）
   * @return password
   **/
  @Schema(description = "密码（加密后）")
  
    public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public DialUser lastLoginTime(OffsetDateTime lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
    return this;
  }

  /**
   * 最后登录时间
   * @return lastLoginTime
   **/
  @Schema(description = "最后登录时间")
  
    @Valid
    public OffsetDateTime getLastLoginTime() {
    return lastLoginTime;
  }

  public void setLastLoginTime(OffsetDateTime lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DialUser dialUser = (DialUser) o;
    return Objects.equals(this.id, dialUser.id) &&
        Objects.equals(this.username, dialUser.username) &&
        Objects.equals(this.password, dialUser.password) &&
        Objects.equals(this.lastLoginTime, dialUser.lastLoginTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username, password, lastLoginTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DialUser {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    lastLoginTime: ").append(toIndentedString(lastLoginTime)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}