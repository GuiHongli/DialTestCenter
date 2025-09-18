package com.huawei.cloududn.dialingtest.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.huawei.cloududn.dialingtest.model.DialUser;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * DialUserResponse
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2025-09-18T16:05:08.624+08:00[Asia/Shanghai]")


public class DialUserResponse   {
  @JsonProperty("success")
  private Boolean success = null;

  @JsonProperty("data")
  private DialUser data = null;

  @JsonProperty("message")
  private String message = null;

  public DialUserResponse success(Boolean success) {
    this.success = success;
    return this;
  }

  /**
   * 操作是否成功
   * @return success
   **/
  @Schema(description = "操作是否成功")
  
    public Boolean isSuccess() {
    return success;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }

  public DialUserResponse data(DialUser data) {
    this.data = data;
    return this;
  }

  /**
   * Get data
   * @return data
   **/
  @Schema(description = "")
  
    @Valid
    public DialUser getData() {
    return data;
  }

  public void setData(DialUser data) {
    this.data = data;
  }

  public DialUserResponse message(String message) {
    this.message = message;
    return this;
  }

  /**
   * 响应消息
   * @return message
   **/
  @Schema(description = "响应消息")
  
    public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DialUserResponse dialUserResponse = (DialUserResponse) o;
    return Objects.equals(this.success, dialUserResponse.success) &&
        Objects.equals(this.data, dialUserResponse.data) &&
        Objects.equals(this.message, dialUserResponse.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, data, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DialUserResponse {\n");
    
    sb.append("    success: ").append(toIndentedString(success)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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
