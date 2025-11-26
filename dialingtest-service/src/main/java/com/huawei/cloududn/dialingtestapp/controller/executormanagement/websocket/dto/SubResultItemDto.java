package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * 子结果项（多UE场景）
 * 用于TaskStart-Response消息的sub-result容器中
 * 
 * @author DialTestCenter
 * @version V3
 */
public class SubResultItemDto {
    
    /**
     * 手机序列号
     * Tag: 0x000A
     */
    private String serialNo;
    
    /**
     * 脚本执行结果：Success/Fail
     * Tag: 0x0006
     */
    private String result;
    
    /**
     * VPN阻塞结果（可选）
     * Tag: 0x002C
     */
    private String block;
    
    /**
     * 描述信息
     * Tag: 0x0007
     */
    private String description;
    
    public SubResultItemDto() {
    }
    
    public SubResultItemDto(String serialNo, String result, String description) {
        this.serialNo = serialNo;
        this.result = result;
        this.description = description;
    }
    
    public String getSerialNo() {
        return serialNo;
    }
    
    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public String getBlock() {
        return block;
    }
    
    public void setBlock(String block) {
        this.block = block;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "SubResultItemDto{" +
                "serialNo='" + serialNo + '\'' +
                ", result='" + result + '\'' +
                ", block='" + block + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

