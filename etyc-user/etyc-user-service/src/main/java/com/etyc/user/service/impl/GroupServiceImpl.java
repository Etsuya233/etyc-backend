package com.etyc.user.service.impl;

import com.etyc.user.domain.po.Group;
import com.etyc.user.mapper.GroupMapper;
import com.etyc.user.service.IGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-20
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {

}
