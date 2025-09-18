package com.huawei.cloududn.dialingtest.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * CreateDialUserRequest
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2025-09-18T16:05:08.624+08:00[Asia/Shanghai]")


public class CreateDialUserRequest   {
  @JsonProperty("username")
  private String username = null;

  @JsonProperty("password")
  private String password = null;

  public CreateDialUserRequest username(String username) {
    this.username = username;
    return this;
  }

  /**
   * 用户名
   * @return username
   **/
  @Schema(required = true, description = "用户名")
      @NotNull

  @Size(max=50)   public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public CreateDialUserRequest password(String password) {
    this.password = password;
    return this;
  }

  /**
   * 密码
   * @return password
   **/
  @Schema(required = true, description = "密码")
      @NotNull

  @Size(min=6)   public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateDialUserRequest createDialUserRequest = (CreateDialUserRequest) o;
    return Objects.equals(this.username, createDialUserRequest.username) &&
        Objects.equals(this.password, createDialUserRequest.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateDialUserRequest {\n");
    
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
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
