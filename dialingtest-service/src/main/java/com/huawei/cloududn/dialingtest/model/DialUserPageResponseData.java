package com.huawei.cloududn.dialingtest.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.huawei.cloududn.dialingtest.model.DialUser;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * DialUserPageResponseData
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2025-09-18T16:05:08.624+08:00[Asia/Shanghai]")


public class DialUserPageResponseData   {
  @JsonProperty("content")
  @Valid
  private List<DialUser> content = null;

  @JsonProperty("totalElements")
  private Integer totalElements = null;

  @JsonProperty("totalPages")
  private Integer totalPages = null;

  @JsonProperty("size")
  private Integer size = null;

  @JsonProperty("number")
  private Integer number = null;

  public DialUserPageResponseData content(List<DialUser> content) {
    this.content = content;
    return this;
  }

  public DialUserPageResponseData addContentItem(DialUser contentItem) {
    if (this.content == null) {
      this.content = new ArrayList<>();
    }
    this.content.add(contentItem);
    return this;
  }

  /**
   * Get content
   * @return content
   **/
  @Schema(description = "")
      @Valid
    public List<DialUser> getContent() {
    return content;
  }

  public void setContent(List<DialUser> content) {
    this.content = content;
  }

  public DialUserPageResponseData totalElements(Integer totalElements) {
    this.totalElements = totalElements;
    return this;
  }

  /**
   * 总记录数
   * @return totalElements
   **/
  @Schema(description = "总记录数")
  
    public Integer getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(Integer totalElements) {
    this.totalElements = totalElements;
  }

  public DialUserPageResponseData totalPages(Integer totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  /**
   * 总页数
   * @return totalPages
   **/
  @Schema(description = "总页数")
  
    public Integer getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }

  public DialUserPageResponseData size(Integer size) {
    this.size = size;
    return this;
  }

  /**
   * 每页大小
   * @return size
   **/
  @Schema(description = "每页大小")
  
    public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public DialUserPageResponseData number(Integer number) {
    this.number = number;
    return this;
  }

  /**
   * 当前页码
   * @return number
   **/
  @Schema(description = "当前页码")
  
    public Integer getNumber() {
    return number;
  }

  public void setNumber(Integer number) {
    this.number = number;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DialUserPageResponseData dialUserPageResponseData = (DialUserPageResponseData) o;
    return Objects.equals(this.content, dialUserPageResponseData.content) &&
        Objects.equals(this.totalElements, dialUserPageResponseData.totalElements) &&
        Objects.equals(this.totalPages, dialUserPageResponseData.totalPages) &&
        Objects.equals(this.size, dialUserPageResponseData.size) &&
        Objects.equals(this.number, dialUserPageResponseData.number);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, totalElements, totalPages, size, number);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DialUserPageResponseData {\n");
    
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    totalElements: ").append(toIndentedString(totalElements)).append("\n");
    sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    number: ").append(toIndentedString(number)).append("\n");
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
