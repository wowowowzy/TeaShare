package com.example.tea.mapper;

import com.example.tea.entity.pojo.ChatModel.History;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMapper {

    void add(History history);

    @Select("select * from sys_history where user_id=#{userId}")
    List<History> getHisoriesByUserId(Long userId);

    /** 某会话最近 limit 条消息（按入库顺序升序），即短期记忆窗口。 */
    List<History> listWindowBySessionId(@Param("sessionId") Long sessionId, @Param("limit") int limit);

    /** 所有会话ID。 */
    List<Long> listSessionIds();

    void deleteBySessionId(@Param("sessionId") Long sessionId);

    void batchInsert(@Param("list") List<History> list);

}