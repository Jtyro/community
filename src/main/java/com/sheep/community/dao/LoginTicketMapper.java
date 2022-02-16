package com.sheep.community.dao;

import com.sheep.community.pojo.LoginTicket;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author sheep
 */
@Mapper
@Deprecated
public interface LoginTicketMapper {

    int insertLoginTicket(LoginTicket loginTicket);

    LoginTicket selectByTicket(String ticket);

    int updateStatus(String ticket, int status);
}
