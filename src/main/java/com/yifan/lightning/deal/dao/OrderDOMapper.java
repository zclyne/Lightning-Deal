package com.yifan.lightning.deal.dao;

import com.yifan.lightning.deal.dataobject.OrderDO;

import java.util.List;

public interface OrderDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Sep 26 16:16:56 CST 2019
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Sep 26 16:16:56 CST 2019
     */
    int insert(OrderDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Sep 26 16:16:56 CST 2019
     */
    int insertSelective(OrderDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Sep 26 16:16:56 CST 2019
     */
    OrderDO selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Sep 26 16:16:56 CST 2019
     */
    int updateByPrimaryKeySelective(OrderDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Sep 26 16:16:56 CST 2019
     */
    int updateByPrimaryKey(OrderDO record);

    List<OrderDO> listOrdersByUserId(Integer userId);
}