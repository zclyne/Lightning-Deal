package com.yifan.lightning.deal.dataobject;

import java.sql.Timestamp;
import java.util.Date;

public class NotificationDO {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column notification.id
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column notification.sender_id
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    private Integer senderId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column notification.content
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    private String content;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column notification.timestamp
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    private Timestamp timestamp;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column notification.id
     *
     * @return the value of notification.id
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column notification.id
     *
     * @param id the value for notification.id
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column notification.sender_id
     *
     * @return the value of notification.sender_id
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    public Integer getSenderId() {
        return senderId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column notification.sender_id
     *
     * @param senderId the value for notification.sender_id
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column notification.content
     *
     * @return the value of notification.content
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    public String getContent() {
        return content;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column notification.content
     *
     * @param content the value for notification.content
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column notification.timestamp
     *
     * @return the value of notification.timestamp
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column notification.timestamp
     *
     * @param timestamp the value for notification.timestamp
     *
     * @mbg.generated Wed Jul 01 22:46:00 CST 2020
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}