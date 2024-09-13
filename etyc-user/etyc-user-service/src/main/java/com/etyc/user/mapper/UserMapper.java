package com.etyc.user.mapper;

import com.etyc.user.domain.po.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etyc.user.domain.vo.UserLiteVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-19
 */
public interface UserMapper extends BaseMapper<User> {

	List<UserLiteVo> selectBatchUserLite(@Param("ids") List<Long> ids);

}
